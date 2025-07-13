package dev.tfkls.hikkei.model.revision;

public interface RevisionDataProvider {
  Integer createRevision(RevisionData data);

  Integer getPage(int revId);
}
