package dev.tfkls.hikkei;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.role.RoleDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.provider.*;
import dev.tfkls.hikkei.server.MainVerticle;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.vertx.core.Vertx;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args) {
    Dotenv dotenv = new DotenvBuilder().filename(".env").load();
    String db_host = dotenv.get("POSTGRES_HOST");
    String db_name = dotenv.get("POSTGRES_NAME");
    String db_user = dotenv.get("POSTGRES_USER");
    String db_password = dotenv.get("POSTGRES_PASS");
    String file_dir = dotenv.get("FILE_DIRECTORY");
    Integer port = Integer.valueOf(dotenv.get("PORT"));
    System.out.println("Read environment variables");

    Connection connection;
    try {
      connection =
          DriverManager.getConnection(
              "jdbc:postgresql://" + db_host + "/" + db_name, db_user, db_password);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
    System.out.println("Database connection established");

    UserDataProvider userDataProvider = new JDBCUserDataProvider(connection);
    PageDataProvider pageDataProvider = new JDBCPageDataProvider(connection);
    RevisionDataProvider revisionDataProvider = new JDBCRevisionDataProvider(connection);
    CommentDataProvider commentDataProvider = new JDBCCommentDataProvider(connection);
    CategoryDataProvider categoryDataProvider = new JDBCCategoryDataProvider(connection);
    RoleDataProvider roleDataProvider = new JDBCRoleDataProvider(connection);
    VersionProvider versionProvider = new FileVersionProvider(file_dir);

    System.out.println("Deploying the application");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(
        new MainVerticle(
            userDataProvider,
            pageDataProvider,
            revisionDataProvider,
            commentDataProvider,
            categoryDataProvider,
            roleDataProvider,
            versionProvider,
            port));
  }
}
