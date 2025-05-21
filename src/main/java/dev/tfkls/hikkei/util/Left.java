package dev.tfkls.hikkei.util;

public record Left<A, B>(A value) implements Either<A, B> {}
