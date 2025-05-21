package dev.tfkls.hikkei.util;

public record Right<A, B>(B value) implements Either<A, B> {}
