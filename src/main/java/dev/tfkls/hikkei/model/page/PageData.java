package dev.tfkls.hikkei.model.page;

import java.time.LocalDateTime;

public record PageData(
    Integer id,
    Integer userId,
    String title,
    Integer category,
    Boolean archived,
    LocalDateTime created) {
  public static final PageData DELETED = new PageData(null, null, null, null, null, null);
}
