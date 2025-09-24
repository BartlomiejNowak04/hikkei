package dev.tfkls.hikkei.model.blob;

import java.time.LocalDateTime;

public record BlobData(
    Integer id, Integer userId, LocalDateTime created, String mime, byte[] file, Integer size) {

  public static final BlobData DELETED = new BlobData(null, null, null, null, null, null);
}
