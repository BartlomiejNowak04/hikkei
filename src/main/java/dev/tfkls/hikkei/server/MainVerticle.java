package dev.tfkls.hikkei.server;

import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.provider.*;
import dev.tfkls.hikkei.server.routing.CommentRouteHandler;
import dev.tfkls.hikkei.server.routing.PageRouteHandler;
import dev.tfkls.hikkei.server.routing.RevisionRouteHandler;
import dev.tfkls.hikkei.server.routing.UserRouteHandler;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.ext.web.Router;
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

    UserRouteHandler userRoute = new UserRouteHandler(userDataProvider);
    PageRouteHandler pageRoute = new PageRouteHandler(pageDataProvider, versionProvider);
    RevisionRouteHandler revisionRoute =
        new RevisionRouteHandler(revisionDataProvider, versionProvider);
    CommentRouteHandler commentRoute = new CommentRouteHandler(commentDataProvider);

    Router router = Router.router(vertx);
    userRoute.addRoutes(router, "/api/user");
    pageRoute.addRoutes(router, "/api/page");
    revisionRoute.addRoutes(router, "/api/revision");
    commentRoute.addRoutes(router, "/api/comment");

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8888)
        .onSuccess(
            server -> System.out.println("HTTP server started on port " + server.actualPort()))
        .onFailure(Throwable::printStackTrace);
  }
}
