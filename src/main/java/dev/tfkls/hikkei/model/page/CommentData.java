package dev.tfkls.hikkei.model.page;

import java.time.LocalDateTime;

public record CommentData(Integer pageId, String content, LocalDateTime date) {}
