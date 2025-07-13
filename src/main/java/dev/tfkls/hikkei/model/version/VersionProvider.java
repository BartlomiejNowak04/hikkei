package dev.tfkls.hikkei.model.version;

import java.util.UUID;

public interface VersionProvider {
  String getContent(UUID id);

  UUID storeContent(String content);
}
