// src/main/java/com/br/cfi/controller/StorageController.java
package com.br.cfi.controller;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.br.cfi.service.StorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
public class StorageController {

  private final StorageService storage;

  @PostMapping("/presign")
  public Map<String, Object> presign(
      @RequestParam String imovelId,
      @RequestParam String tipo,
      @RequestParam String filename,
      @RequestParam(required = false, defaultValue = MediaType.APPLICATION_OCTET_STREAM_VALUE) String contentType
  ) {
    String key = storage.buildKey(imovelId, tipo, filename);
    URL uploadUrl = storage.presignPut(key, contentType, Duration.ofMinutes(15));
    String publicUrl = storage.publicUrl(key);
    return Map.of(
        "key", key,
        "uploadUrl", uploadUrl.toString(),
        "publicUrl", publicUrl,
        "contentType", contentType,
        "expiresInSeconds", 15 * 60
    );
  }

  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> uploadDireto(
      @RequestParam String imovelId,
      @RequestParam String tipo, // "imagens" | "plantas"
      @RequestPart("file") MultipartFile file
  ) throws IOException {
    String key = storage.buildKey(imovelId, tipo, file.getOriginalFilename());
    String url = storage.upload(key, file.getContentType(), file.getBytes());
    return Map.of(
        "key", key,
        "publicUrl", url,
        "size", file.getSize(),
        "contentType", file.getContentType()
    );
  }

  @GetMapping("/list")
  public Object list(
      @RequestParam String imovelId,
      @RequestParam String tipo
  ) {
    String prefix = "imoveis/%s/%s/".formatted(imovelId, tipo);
    return storage.listPrefix(prefix).stream()
        .filter(o -> !o.key().endsWith("/_cover"))
        .map(o -> Map.of(
            "key", o.key(),
            "size", o.size(),
            "lastModified", o.lastModified(),
            "url", storage.publicUrl(o.key())
        ));
  }

  @DeleteMapping("/prefix")
  public Map<String, Object> deletePrefix(
      @RequestParam String imovelId,
      @RequestParam String tipo
  ) {
    String prefix = "imoveis/%s/%s/".formatted(imovelId, tipo);
    storage.deletePrefix(prefix);
    return Map.of("deletedPrefix", prefix);
  }

  /* =============================
     CAPA via UPLOAD DE ARQUIVO
     ============================= */

  /** Sobe um arquivo e já define como capa no bucket. */
  @PostMapping(path = "/cover/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> uploadCover(
      @RequestParam String imovelId,
      @RequestParam(defaultValue = "imagens") String tipo,
      @RequestPart("file") MultipartFile file
  ) throws IOException {
    String key = storage.uploadCover(imovelId, tipo,
        file.getOriginalFilename(), file.getContentType(), file.getBytes());
    return Map.of(
        "imovelId", imovelId,
        "tipo", tipo,
        "coverKey", key,
        "coverUrl", storage.publicUrl(key)
    );
  }

  /** Retorna a URL pública da capa (marcador _cover; fallback para primeira imagem). */
  @GetMapping("/cover")
  public Map<String, Object> getCover(
      @RequestParam String imovelId,
      @RequestParam(defaultValue = "imagens") String tipo
  ) {
    String url = storage.resolveCoverUrl(imovelId, tipo);
    return Map.of(
        "imovelId", imovelId,
        "tipo", tipo,
        "coverUrl", url
    );
  }
}
