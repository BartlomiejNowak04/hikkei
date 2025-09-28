package dev.tfkls.hikkei.server;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.blob.BlobDataProvider;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.page.PageRenderer;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.auth.*;
import dev.tfkls.hikkei.server.handlers.AuthFailureHandler;
import dev.tfkls.hikkei.server.handlers.FailureHandler;
import dev.tfkls.hikkei.server.handlers.LogHandler;
import dev.tfkls.hikkei.server.handlers.SessionHandler;
import dev.tfkls.hikkei.server.routes.*;
import dev.tfkls.hikkei.server.util.LogFunction;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class MainVerticle extends VerticleBase {
  private final UserDataProvider userProvider;
  private final PageDataProvider pageProvider;
  private final RevisionDataProvider revisionProvider;
  private final CommentDataProvider commentProvider;
  private final CategoryDataProvider categoryProvider;
  private final BlobDataProvider blobProvider;
  private final VersionProvider versionProvider;
  private final FileProvider fileProvider;
  private final Integer port;
  private final URI baseUri;
  private final @Nullable Map<String, Function<URI, AuthenticationProvider>> authMethods;
  private final PageRenderer pageRenderer;
  private final AccessManager accessManager;
  private final LogFunction requestLogger;
  private final String afterAuthCallbackRoute;

  public MainVerticle(
      UserDataProvider userProvider,
      PageDataProvider pageProvider,
      RevisionDataProvider revisionProvider,
      CommentDataProvider commentProvider,
      CategoryDataProvider categoryProvider,
      BlobDataProvider blobProvider,
      VersionProvider versionProvider,
      FileProvider fileProvider,
      Integer port,
      URI baseUri,
      @Nullable Map<String, Function<URI, AuthenticationProvider>> authMethods,
      PageRenderer pageRenderer,
      AccessManager accessManager,
      @Nullable LogFunction requestLogger,
      String afterAuthCallbackRoute) {
    super();
    this.userProvider = userProvider;
    this.pageProvider = pageProvider;
    this.revisionProvider = revisionProvider;
    this.commentProvider = commentProvider;
    this.categoryProvider = categoryProvider;
    this.blobProvider = blobProvider;
    this.versionProvider = versionProvider;
    this.fileProvider = fileProvider;
    this.port = port;
    this.baseUri = baseUri;
    this.authMethods = authMethods;
    this.pageRenderer = pageRenderer;
    this.accessManager = accessManager;
    this.requestLogger = Optional.ofNullable(requestLogger).orElse(ignored -> {});
    this.afterAuthCallbackRoute = afterAuthCallbackRoute;
  }

  @Override
  public Future<?> start() {
    UserRouteHandler userRoute =
        new UserRouteHandler(vertx, userProvider, fileProvider, categoryProvider, accessManager);
    PageRouteHandler pageRoute = new PageRouteHandler(vertx, pageProvider, accessManager);
    RevisionRouteHandler revisionRoute =
        new RevisionRouteHandler(
            vertx, revisionProvider, versionProvider, pageProvider, pageRenderer, accessManager);
    CommentRouteHandler commentRoute =
        new CommentRouteHandler(vertx, commentProvider, pageProvider, accessManager);
    CategoryRouteHandler categoryRoute =
        new CategoryRouteHandler(vertx, categoryProvider, userProvider, accessManager);
    BlobRouteHandler blobRoute = new BlobRouteHandler(vertx, blobProvider, accessManager);

    // * Auth configuration section *//
    SessionProvider sessionProvider;
    AuthEventHandler authEventHandler;
    if (authMethods != null) {
      sessionProvider = new StandardSessionProvider(Duration.ofDays(7), LocalDateTime::now);
      authEventHandler = new StandardAuthEventHandler(userProvider, sessionProvider, accessManager);
    } else {
      sessionProvider = new DummySessionProvider();
      authEventHandler = new DummyAuthEventHandler();
    }

    SessionHandler sessionHandler = new SessionHandler(sessionProvider);
    AuthRouteHandler authRoute =
        new AuthRouteHandler(vertx, baseUri, authEventHandler, sessionHandler);
    if (authMethods != null) {
      for (var authEntry : authMethods.entrySet()) {
        authRoute.registerProvider(
            authEntry.getKey(),
            authEntry.getValue().apply(authRoute.getProviderURI(authEntry.getKey())));
      }
    }

    Router router = Router.router(vertx);
    router.route("/api/auth/*").failureHandler(new AuthFailureHandler(afterAuthCallbackRoute));
    router.route("/api/*").failureHandler(new FailureHandler());
    router.route("/api/*").handler(sessionHandler);
    router.route("/api/*").handler(new LogHandler(requestLogger, accessManager));
    router.route("/api/*").handler(CorsHandler.create());
    router
        .route("/api/*")
        .handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true).setBodyLimit(-1));
    router.route("/api/auth/*").subRouter(authRoute.getRouter());
    router.route("/api/user/*").subRouter(userRoute.getRouter());
    router.route("/api/page/*").subRouter(pageRoute.getRouter());
    router.route("/api/revision/*").subRouter(revisionRoute.getRouter());
    router.route("/api/comment/*").subRouter(commentRoute.getRouter());
    router.route("/api/category/*").subRouter(categoryRoute.getRouter());
    router.route("/api/blob/*").subRouter(blobRoute.getRouter());

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(port)
        .onSuccess(
            server -> System.out.println("HTTP server started on port " + server.actualPort()))
        .onFailure(Throwable::printStackTrace);
  }
}
