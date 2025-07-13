package dev.tfkls.hikkei.model.revision;

import dev.tfkls.hikkei.model.version.VersionProvider;
import java.util.UUID;

public class RevisionManager {
  private final RevisionDataProvider provider;
  private final VersionProvider version;

  public RevisionManager(RevisionDataProvider provider, VersionProvider version) {
    this.provider = provider;
    this.version = version;
  }

  public boolean createRevision(int pageId, String content) {
    UUID id = version.storeContent(content);
    if (id == null) return false;
    return provider.createRevision(new RevisionData(null, pageId, id, null, null)) != null;
  }

  public boolean changeStatus(int revId, Boolean status) {
    return provider.changeState(revId, status);
  }

  public Integer getPage(int revId) {
    return provider.getPage(revId);
  }
}
