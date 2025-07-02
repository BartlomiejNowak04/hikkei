package dev.tfkls.hikkei.model.user;

import java.time.LocalDateTime;
import java.util.Optional;

public record UserData(
    String username, Optional<byte[]> picture, LocalDateTime joinedAt, boolean deleted) {}
