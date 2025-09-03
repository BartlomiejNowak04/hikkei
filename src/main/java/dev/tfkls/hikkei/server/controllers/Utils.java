package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import io.vertx.core.json.JsonObject;
import java.util.Map;

public class Utils {
  public static int exceptionToCode(Exception e) {
    if (e == null) return 500;
    return switch (e.getClass().getSimpleName()) {
      case "BadDataException" -> 400;
      case "InternalErrorException" -> 500;
      default -> 500;
    };
  }

  public static void checkBody(JsonObject body, Map<String, Class<?>> arguments)
      throws BadDataException {
    if (body == null) throw new BadDataException("No data provided");

    for (Map.Entry<String, Class<?>> pair : arguments.entrySet()) {
      String argument = pair.getKey();
      Class<?> type = pair.getValue();
      if (!body.containsKey(argument))
        throw new BadDataException("Missing required parameter: " + argument);

      Object arg = body.getValue(argument);
      if (arg == null) throw new BadDataException("Incorrect parameter: " + argument);
      try {
        type.cast(arg);
      } catch (ClassCastException e) {
        throw new BadDataException("Incorrect parameter: " + argument);
      }
    }
  }

  public static JsonObject pageToJson(PageData page) {
    return new JsonObject()
        .put("id", page.id())
        .put("user_id", page.userId())
        .put("title", page.title())
        .put("category", page.category())
        .put("archived", page.archived())
        .put("created", page.created());
  }

  public static JsonObject revisionToJson(RevisionData revision) {
    return new JsonObject()
        .put("id", revision.id())
        .put("page_id", revision.pageId())
        .put("user_id", revision.userId())
        .put("filename", revision.file())
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
}
