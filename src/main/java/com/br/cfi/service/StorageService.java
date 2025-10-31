// src/main/java/com/br/cfi/service/StorageService.java
package com.br.cfi.service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.br.cfi.config.R2Props;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class StorageService {

  @Autowired private S3Client s3;
  @Autowired private S3Presigner presigner;
  @Autowired private R2Props props;

  /** Gera a key no formato: imoveis/{imovelId}/{tipo}/uuid.ext */
  public String buildKey(String imovelId, String tipoPasta, String originalFilename) {
    String ext = "";
    if (originalFilename != null && originalFilename.lastIndexOf('.') >= 0) {
      ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
    return "imoveis/%s/%s/%s%s".formatted(imovelId, tipoPasta, UUID.randomUUID(), ext);
  }

  /** Upload pelo backend (bytes no servidor) */
  public String upload(String key, String contentType, byte[] bytes) {
    PutObjectRequest put = PutObjectRequest.builder()
        .bucket(props.bucket())
        .key(key)
        .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    s3.putObject(put, RequestBody.fromBytes(bytes));
    return publicUrl(key);
  }

  /** Presigned PUT para upload direto do front */
  public URL presignPut(String key, String contentType, Duration ttl) {
    PutObjectRequest req = PutObjectRequest.builder()
        .bucket(props.bucket())
        .key(key)
        .contentType(contentType)
        .build();

    PutObjectPresignRequest preReq = PutObjectPresignRequest.builder()
        .signatureDuration(ttl)
        .putObjectRequest(req)
        .build();

    return presigner.presignPutObject(preReq).url();
  }

  /** Lista “pasta”: todos os objetos com prefixo */
  public List<S3Object> listPrefix(String prefix) {
    ListObjectsV2Response resp = s3.listObjectsV2(ListObjectsV2Request.builder()
        .bucket(props.bucket())
        .prefix(prefix.endsWith("/") ? prefix : prefix + "/")
        .build());
    return resp.contents();
  }

  /** Apaga todos os objetos sob um prefixo */
  public void deletePrefix(String prefix) {
    List<S3Object> objects = listPrefix(prefix);
    if (objects.isEmpty()) return;

    List<ObjectIdentifier> toDelete = objects.stream()
        .map(o -> ObjectIdentifier.builder().key(o.key()).build())
        .toList();

    s3.deleteObjects(DeleteObjectsRequest.builder()
        .bucket(props.bucket())
        .delete(Delete.builder().objects(toDelete).build())
        .build());
  }

  /** Monta URL pública para acesso */
  public String publicUrl(String key) {
    if (props.publicBaseUrl() != null && !props.publicBaseUrl().isBlank()) {
      return props.publicBaseUrl().replaceAll("/+$", "") + "/" + key;
    }
    return props.endpoint().replaceAll("/+$", "") + "/" + props.bucket() + "/" + key;
  }

  /* =========================
     CAPA NO BUCKET (marcador)
     ========================= */

  /** Grava (ou atualiza) o marcador de capa no objeto imoveis/{id}/{tipo}/_cover contendo a key da imagem. */
  public void setCover(String imovelId, String tipo, String imageKey) {
    String coverKey = "imoveis/%s/%s/_cover".formatted(imovelId, tipo);
    PutObjectRequest put = PutObjectRequest.builder()
        .bucket(props.bucket())
        .key(coverKey)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build();
    s3.putObject(put, RequestBody.fromBytes(imageKey.getBytes(StandardCharsets.UTF_8)));
  }

  /** Lê o marcador _cover e retorna a URL pública da capa; se não existir, usa a primeira imagem do prefixo. */
  public String resolveCoverUrl(String imovelId, String tipo) {
    String prefix = "imoveis/%s/%s/".formatted(imovelId, tipo);
    String coverMarker = prefix + "_cover";

    try {
      GetObjectResponse head =
          s3.getObject(GetObjectRequest.builder().bucket(props.bucket()).key(coverMarker).build(),
              software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()).response();

      // Lê o conteúdo (key da imagem) do marcador:
      String imageKey = new String(
          s3.getObject(GetObjectRequest.builder().bucket(props.bucket()).key(coverMarker).build(),
              software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()
          ).asByteArray(), StandardCharsets.UTF_8
      ).trim();

      if (!imageKey.isBlank()) {
        return publicUrl(imageKey);
      }
    } catch (Exception ignore) {
      // Se não existir o _cover, caímos no fallback.
    }

    // Fallback: primeira imagem (ignora o _cover)
    return listPrefix(prefix).stream()
        .filter(o -> !o.key().endsWith("/_cover"))
        .sorted(Comparator.comparing(S3Object::lastModified))
        .findFirst()
        .map(o -> publicUrl(o.key()))
        .orElse(null);
  }

  /** Faz upload do arquivo e já define como capa. Retorna a key da imagem. */
  public String uploadCover(String imovelId, String tipo, String originalFilename, String contentType, byte[] bytes) {
    String key = buildKey(imovelId, tipo, originalFilename);
    upload(key, contentType, bytes);
    setCover(imovelId, tipo, key);
    return key;
  }
}
