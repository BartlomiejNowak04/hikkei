package dev.tfkls.hikkei.model;

import java.util.Optional;

public record AuthData(String type, Optional<String> token) {
}
