package com.br.cfi.service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.br.cfi.config.R2Props;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
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

  private static final Pattern KEY_PATTERN =
      Pattern.compile("^imoveis/([^/]+)/([^/]+)/(.+)$");

  // key: imoveis/{imovelId}/{tipo}/uuid.ext
  public String buildKey(String imovelId, String tipoPasta, String originalFilename) {
    String ext = "";
    if (originalFilename != null && originalFilename.lastIndexOf('.') >= 0) {
      ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
    return "imoveis/%s/%s/%s%s".formatted(imovelId, tipoPasta, UUID.randomUUID(), ext);
  }

  // upload via backend
  public String upload(String key, String contentType, byte[] bytes) {
    PutObjectRequest put = PutObjectRequest.builder()
        .bucket(props.bucket())
        .key(key)
        .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    s3.putObject(put, RequestBody.fromBytes(bytes));
    return publicUrl(key);
  }

  // presign para upload direto
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

  // lista por prefixo
  public List<S3Object> listPrefix(String prefix) {
    ListObjectsV2Response resp = s3.listObjectsV2(ListObjectsV2Request.builder()
        .bucket(props.bucket())
        .prefix(prefix.endsWith("/") ? prefix : prefix + "/")
        .build());
    return resp.contents();
  }

  // apaga tudo de um prefixo
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

  // monta url pública
  public String publicUrl(String key) {
    if (props.publicBaseUrl() != null && !props.publicBaseUrl().isBlank()) {
      return props.publicBaseUrl().replaceAll("/+$", "") + "/" + key;
    }
    return props.endpoint().replaceAll("/+$", "") + "/" + props.bucket() + "/" + key;
  }

  // grava marcador de capa
  public void setCover(String imovelId, String tipo, String imageKey) {
    String coverKey = "imoveis/%s/%s/_cover".formatted(imovelId, tipo);
    PutObjectRequest put = PutObjectRequest.builder()
        .bucket(props.bucket())
        .key(coverKey)
        .contentType(MediaType.TEXT_PLAIN_VALUE)
        .build();
    s3.putObject(put, RequestBody.fromBytes(imageKey.getBytes(StandardCharsets.UTF_8)));
  }

  // pega capa (ou primeira imagem)
  public String resolveCoverUrl(String imovelId, String tipo) {
    String prefix = "imoveis/%s/%s/".formatted(imovelId, tipo);
    String coverMarker = prefix + "_cover";

    try {
      String imageKey = new String(
          s3.getObject(GetObjectRequest.builder().bucket(props.bucket()).key(coverMarker).build(),
              software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()
          ).asByteArray(), StandardCharsets.UTF_8
      ).trim();

      if (!imageKey.isBlank()) {
        return publicUrl(imageKey);
      }
    } catch (Exception ignore) {}

    return listPrefix(prefix).stream()
        .filter(o -> !o.key().endsWith("/_cover"))
        .sorted(Comparator.comparing(S3Object::lastModified))
        .findFirst()
        .map(o -> publicUrl(o.key()))
        .orElse(null);
  }

  // upload já como capa
  public String uploadCover(String imovelId, String tipo, String originalFilename, String contentType, byte[] bytes) {
    String key = buildKey(imovelId, tipo, originalFilename);
    upload(key, contentType, bytes);
    setCover(imovelId, tipo, key);
    return key;
  }

  // apaga tudo de um imóvel
  public void deleteAllFromImovel(String imovelId) {
    String prefix = "imoveis/%s".formatted(imovelId);
    deletePrefix(prefix);
  }

  // apaga um objeto
  public void deleteObject(String key) {
    s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(props.bucket())
        .key(key)
        .build());
  }

  // seta capa usando só a key
  public void setCoverByImageKey(String imageKey) {
    ParsedKey parsed = parseKey(imageKey);
    if (parsed == null) {
      throw new IllegalArgumentException("Key inválida para capa: " + imageKey);
    }
    setCover(parsed.imovelId(), parsed.tipo(), imageKey);
  }

  // apaga imagem; se era capa, recalcula
  public void deleteImageByKey(String imageKey) {
    ParsedKey parsed = parseKey(imageKey);
    if (parsed == null) {
      deleteObject(imageKey);
      return;
    }

    String coverKey = "imoveis/%s/%s/_cover".formatted(parsed.imovelId(), parsed.tipo());
    boolean isCover = false;

    try {
      String currentCoverImageKey = new String(
          s3.getObject(GetObjectRequest.builder()
              .bucket(props.bucket())
              .key(coverKey)
              .build(),
              software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()
          ).asByteArray(), StandardCharsets.UTF_8
      ).trim();

      if (imageKey.equals(currentCoverImageKey)) {
        isCover = true;
      }
    } catch (Exception ignore) {}

    deleteObject(imageKey);

    if (isCover) {
      String prefix = "imoveis/%s/%s/".formatted(parsed.imovelId(), parsed.tipo());
      List<S3Object> remaining = listPrefix(prefix).stream()
          .filter(o -> !o.key().endsWith("/_cover"))
          .sorted(Comparator.comparing(S3Object::lastModified))
          .toList();

      if (!remaining.isEmpty()) {
        String newImageKey = remaining.get(0).key();
        setCover(parsed.imovelId(), parsed.tipo(), newImageKey);
      } else {
        try {
          deleteObject(coverKey);
        } catch (Exception ignore) {}
      }
    }
  }

  // helpers
  private ParsedKey parseKey(String key) {
    Matcher m = KEY_PATTERN.matcher(key);
    if (!m.matches()) return null;
    return new ParsedKey(m.group(1), m.group(2), m.group(3));
  }

  private record ParsedKey(String imovelId, String tipo, String filename) {}
}
