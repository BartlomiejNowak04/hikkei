package dev.tfkls.hikkei.server;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.handlers.FailureHandler;
import dev.tfkls.hikkei.server.routes.*;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends VerticleBase {
  private final UserDataProvider userProvider;
  private final PageDataProvider pageProvider;
  private final RevisionDataProvider revisionProvider;
  private final CommentDataProvider commentProvider;
  private final CategoryDataProvider categoryProvider;
  private final VersionProvider versionProvider;
  private final Integer port;

  public MainVerticle(
      UserDataProvider userProvider,
      PageDataProvider pageProvider,
      RevisionDataProvider revisionProvider,
      CommentDataProvider commentProvider,
      CategoryDataProvider categoryProvider,
      VersionProvider versionProvider,
      Integer port) {
    super();
    this.userProvider = userProvider;
    this.pageProvider = pageProvider;
    this.revisionProvider = revisionProvider;
    this.commentProvider = commentProvider;
    this.categoryProvider = categoryProvider;
    this.versionProvider = versionProvider;
    this.port = port;
  }

  @Override
  public Future<?> start() {
    UserRouteHandler userRoute = new UserRouteHandler(vertx, userProvider);
    PageRouteHandler pageRoute = new PageRouteHandler(vertx, pageProvider);
    RevisionRouteHandler revisionRoute =
        new RevisionRouteHandler(vertx, revisionProvider, versionProvider);
    CommentRouteHandler commentRoute = new CommentRouteHandler(vertx, commentProvider);
    CategoryRouteHandler categoryRoute = new CategoryRouteHandler(vertx, categoryProvider);

    Router router = Router.router(vertx);
    router.route("/api/*").failureHandler(new FailureHandler());
    router.route("/api/*").handler(BodyHandler.create());
    router.route("/api/user/*").subRouter(userRoute.getRouter());
    router.route("/api/page/*").subRouter(pageRoute.getRouter());
    router.route("/api/revision/*").subRouter(revisionRoute.getRouter());
    router.route("/api/comment/*").subRouter(commentRoute.getRouter());
    router.route("/api/category/*").subRouter(categoryRoute.getRouter());

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(port)
        .onSuccess(
            server -> System.out.println("HTTP server started on port " + server.actualPort()))
        .onFailure(Throwable::printStackTrace);
  }
}
