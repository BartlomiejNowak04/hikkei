package dev.tfkls.hikkei.model.comment;

import java.time.LocalDateTime;

public record CommentData(
    Integer id,
    Integer pageId,
    Integer userId,
    String content,
    LocalDateTime date,
    Boolean deleted) {}
