package dev.tfkls.hikkei.model.page;

import java.time.LocalDateTime;

public record CommentData(
    Integer id, Integer pageId, String content, LocalDateTime date, Boolean deleted) {}
