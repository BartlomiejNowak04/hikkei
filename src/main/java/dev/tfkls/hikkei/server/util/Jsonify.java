package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.user.UserData;
import io.vertx.core.json.JsonObject;
import java.util.Optional;

public class Jsonify {
  public static JsonObject pageToJson(PageData page) {
    return new JsonObject()
        .put("id", page.id())
        .put("user_id", page.userId())
        .put("title", page.title())
        .put("category", page.category())
        .put("archived", page.archived())
        .put("created", page.created() == null ? null : page.created().toString());
  }

  public static JsonObject revisionToJson(RevisionData revision) {
    return new JsonObject()
        .put("id", revision.id())
        .put("page_id", revision.pageId())
        .put("user_id", revision.userId())
        .put("filename", Optional.ofNullable(revision.file()).map(String::new).orElse(null))
        .put("date", revision.date() == null ? null : revision.date().toString())
        .put("approved", revision.approved());
  }

  public static JsonObject commentToJson(CommentData comment) {
    return new JsonObject()
        .put("id", comment.id())
        .put("page_id", comment.pageId())
        .put("user_id", comment.userId())
        .put("content", comment.content())
        .put("date", comment.date() == null ? null : comment.date().toString());
  }

  public static JsonObject userToJson(UserData user) {
    return new JsonObject()
        .put("id", user.id())
        .put("username", user.username())
        .put("picture", user.picture())
        .put("joined", user.joinedAt() == null ? null : user.joinedAt().toString())
        .put("minimal_permission", user.minimalPermission().name());
  }

  public static JsonObject categoryToJson(CategoryData category) {
    return new JsonObject()
        .put("id", category.id())
        .put("name", category.name())
        .put("created", category.created() == null ? null : category.created().toString())
        .put("default_permission", category.defaultPermission().name());
  }
}
