package dev.tfkls.hikkei.server;

import dev.tfkls.hikkei.model.user.UserManager;
import dev.tfkls.hikkei.provider.JDBCUserDataProvider;
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

    UserRouteHandler userRoute =
        new UserRouteHandler(new UserManager(new JDBCUserDataProvider(connection)));

    Router router = Router.router(vertx);
    userRoute.addRoutes(router, "/api/user");

    return vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8888)
        .onSuccess(
            server -> System.out.println("HTTP server started on port " + server.actualPort()))
        .onFailure(Throwable::printStackTrace);
  }
}
