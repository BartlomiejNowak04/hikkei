package dev.tfkls.hikkei.util;

public sealed interface Either<A, B> permits Left, Right {}
