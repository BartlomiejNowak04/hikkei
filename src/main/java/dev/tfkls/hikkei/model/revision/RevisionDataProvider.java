package dev.tfkls.hikkei.model.revision;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface RevisionDataProvider {
  int createRevision(RevisionData data) throws BadDataException, InternalErrorException;

  void changeState(int id, boolean state) throws BadDataException, InternalErrorException;

  List<RevisionData> getByPage(int pageId) throws InternalErrorException;

  List<RevisionData> getByUser(int userId) throws InternalErrorException;

  RevisionData getById(int id) throws InternalErrorException;

  byte[] getDraft(int userId, int pageId) throws InternalErrorException;

  void saveDraft(int userId, int pageId, byte[] file)
      throws InternalErrorException, BadDataException;

  byte[] deleteDraft(int userId, int pageId) throws InternalErrorException, BadDataException;

  void deleteRevision(int id) throws InternalErrorException;
}
