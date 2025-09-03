package dev.tfkls.hikkei.model.category;

import java.time.LocalDateTime;

public record CategoryData(Integer id, String name, LocalDateTime created) {
  public static final CategoryData DELETED = new CategoryData(null, null, null);
}
