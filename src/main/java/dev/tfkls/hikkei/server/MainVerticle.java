package dev.tfkls.hikkei.server;

import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.provider.*;
import dev.tfkls.hikkei.server.handlers.FailureHandler;
import dev.tfkls.hikkei.server.routes.CommentRouteHandler;
import dev.tfkls.hikkei.server.routes.PageRouteHandler;
import dev.tfkls.hikkei.server.routes.RevisionRouteHandler;
import dev.tfkls.hikkei.server.routes.UserRouteHandler;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    Dotenv dotenv = new DotenvBuilder().filename(".env").load();
    String db_host = dotenv.get("POSTGRES_HOST");
    String db_name = dotenv.get("POSTGRES_NAME");
    String db_user = dotenv.get("POSTGRES_USER");
    String db_password = dotenv.get("POSTGRES_PASS");

    Connection connection;
    try {
      connection =
          DriverManager.getConnection(
              "jdbc:postgresql://" + db_host + "/" + db_name, db_user, db_password);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }

    String file_dir = dotenv.get("FILE_DIRECTORY");

    UserDataProvider userDataProvider = new JDBCUserDataProvider(connection);
    PageDataProvider pageDataProvider = new JDBCPageDataProvider(connection);
    RevisionDataProvider revisionDataProvider = new JDBCRevisionDataProvider(connection);
    CommentDataProvider commentDataProvider = new JDBCCommentDataProvider(connection);
    VersionProvider versionProvider = new FileVersionProvider(file_dir);

    UserRouteHandler userRoute = new UserRouteHandler(vertx, userDataProvider);
    PageRouteHandler pageRoute = new PageRouteHandler(vertx, pageDataProvider);
    RevisionRouteHandler revisionRoute =
        new RevisionRouteHandler(vertx, revisionDataProvider, versionProvider);
    CommentRouteHandler commentRoute = new CommentRouteHandler(vertx, commentDataProvider);

    Router router = Router.router(vertx);
    router.route("/api/*").failureHandler(new FailureHandler());
    router.route("/api/*").handler(BodyHandler.create());
    router.route("/api/user/*").subRouter(userRoute.getRouter());
    router.route("/api/page/*").subRouter(pageRoute.getRouter());
    router.route("/api/revision/*").subRouter(revisionRoute.getRouter());
    router.route("/api/comment/*").subRouter(commentRoute.getRouter());

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8888)
        .onSuccess(
            server -> System.out.println("HTTP server started on port " + server.actualPort()))
        .onFailure(Throwable::printStackTrace);
  }
}
