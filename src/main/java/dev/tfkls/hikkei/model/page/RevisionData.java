package dev.tfkls.hikkei.model.page;

import java.time.LocalDateTime;
import java.util.UUID;

public record RevisionData(Integer pageId, UUID filename, LocalDateTime date, Boolean approved) {}
