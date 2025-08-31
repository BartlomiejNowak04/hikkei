package dev.tfkls.hikkei.model.revision;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface RevisionDataProvider {
  int createRevision(RevisionData data) throws BadDataException, InternalErrorException;

  void changeState(int revId, int pageId, Boolean state)
      throws BadDataException, InternalErrorException;
}
