package dev.tfkls.hikkei.model.page;

import java.time.LocalDateTime;

public record PageData(
    Integer id,
    Integer userId,
    String title,
    Integer category,
    Boolean archived,
    LocalDateTime created) {
  public PageData(Integer userId, String title, Integer category, Boolean archived) {
    this(null, userId, title, category, archived, null);
  }

  public PageData(Integer userId, String title, Integer category) {
    this(userId, title, category, null);
  }

  public static final PageData DELETED = new PageData(null, null, null, null, null, null);
}
