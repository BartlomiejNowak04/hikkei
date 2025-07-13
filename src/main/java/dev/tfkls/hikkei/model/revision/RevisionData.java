package dev.tfkls.hikkei.model.revision;

import java.time.LocalDateTime;
import java.util.UUID;

public record RevisionData(
    Integer id, Integer pageId, UUID filename, LocalDateTime date, Boolean approved) {}
