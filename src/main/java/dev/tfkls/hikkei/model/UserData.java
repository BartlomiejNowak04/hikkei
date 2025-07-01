package dev.tfkls.hikkei.model;

import java.time.LocalDateTime;

public record UserData(String username, LocalDateTime joinedAt, boolean deleted) {}
