package dev.tfkls.hikkei.model.comment;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface CommentDataProvider {
  int addComment(CommentData data) throws BadDataException, InternalErrorException;

  void deleteComment(int id) throws InternalErrorException;

  CommentData getById(int id) throws InternalErrorException;

  List<CommentData> getByPage(int pageId) throws InternalErrorException;

  List<CommentData> getByUser(Integer userId) throws InternalErrorException;
}
