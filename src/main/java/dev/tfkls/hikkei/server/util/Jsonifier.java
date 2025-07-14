package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.page.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import io.vertx.core.json.JsonObject;

public class Jsonifier {
  public static JsonObject pageToJson(PageData page) {
    return new JsonObject()
        .put("id", page.id())
        .put("title", page.title())
        .put("category", page.category())
        .put("archived", page.archived());
  }

  public static JsonObject revisionToJson(RevisionData revision) {
    return new JsonObject()
        .put("id", revision.id())
        .put("page_id", revision.pageId())
        .put("filename", revision.filename().toString())
        .put("date", revision.date().toString())
        .put("approved", revision.approved());
  }

  public static JsonObject commentToJson(CommentData comment) {
    return new JsonObject()
        .put("id", comment.id())
        .put("page_id", comment.pageId())
        .put("content", comment.content())
        .put("date", comment.date().toString())
        .put("deleted", comment.deleted());
  }
}
