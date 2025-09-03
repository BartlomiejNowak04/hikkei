package dev.tfkls.hikkei.model.revision;

import java.time.LocalDateTime;

public record RevisionData(
    Integer id, Integer pageId, Integer userId, byte[] file, LocalDateTime date, Boolean approved) {
  public static final RevisionData DELETED = new RevisionData(null, null, null, null, null, null);
}
