package dev.tfkls.hikkei.model.page;

import java.util.UUID;

public class RevisionManager {
  private final PageDataProvider provider;
  private final VersionProvider version;

  public RevisionManager(PageDataProvider provider, VersionProvider version) {
    this.provider = provider;
    this.version = version;
  }

  public boolean createRevision(int pageId, String content) {
    UUID id = version.storeContent(content);
    if (id == null) return false;
    return provider.createRevision(new RevisionData(pageId, id, null, null)) != null;
  }
}
