package dev.tfkls.hikkei.util;

import java.time.LocalDateTime;

@FunctionalInterface
public interface ClockView {
  LocalDateTime getCurrentTime();
}
