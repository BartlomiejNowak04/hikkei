package dev.tfkls.hikkei.model.comment;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface CommentDataProvider {
  Integer addComment(CommentData data) throws BadDataException, InternalErrorException;

  void deleteComment(int pageId, int commentId) throws BadDataException, InternalErrorException;
}
