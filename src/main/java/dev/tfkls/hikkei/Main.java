package dev.tfkls.hikkei;

import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.JDBC.*;
import dev.tfkls.hikkei.provider.auth.TokenAuthenticationProvider;
import dev.tfkls.hikkei.provider.filesystem.FilesystemFileProvider;
import dev.tfkls.hikkei.provider.filesystem.FilesystemVersionProvider;
import dev.tfkls.hikkei.provider.page.MarkdownPageRenderer;
import dev.tfkls.hikkei.server.MainVerticle;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.vertx.core.Vertx;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class Main {

  public static void main(String[] args) {
    Dotenv dotenv = new DotenvBuilder().filename(".env").load();
    String db_host = dotenv.get("POSTGRES_HOST");
    String db_name = dotenv.get("POSTGRES_NAME");
    String db_user = dotenv.get("POSTGRES_USER");
    String db_password = dotenv.get("POSTGRES_PASS");
    String file_dir = dotenv.get("FILE_DIRECTORY");
    String img_dir = dotenv.get("IMAGE_DIRECTORY");
    Integer port = Integer.valueOf(dotenv.get("PORT"));
    String admin_pass = dotenv.get("ADMIN_PASS");
    URI baseUri = URI.create(dotenv.get("BASE_URI"));
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
    VersionProvider versionProvider = new FilesystemVersionProvider(file_dir);
    FileProvider fileProvider = new FilesystemFileProvider(img_dir);

    // Admin auth setup
    Map<String, Function<URI, AuthenticationProvider>> authMethods = new HashMap<>();

    if (admin_pass != null) {
      authMethods.put("admin", uri -> new TokenAuthenticationProvider(admin_pass, uri));
      try {
        AuthData adminData = new AuthData("admin", "");
        @Nullable UserData userData = userDataProvider.getUserByAuth(adminData);
        if (userData == null) {
          int userId =
              userDataProvider.createUser(
                  new UserData(null, "admin", null, null, PermissionLevel.SUPER_ADMIN));
          userDataProvider.addUserAuthMethod(userId, adminData);
        } else if (userData.minimalPermission() != PermissionLevel.SUPER_ADMIN) {
          userDataProvider.updateMinimalPermission(userData.id(), PermissionLevel.SUPER_ADMIN);
        }
      } catch (BadDataException | InternalErrorException err) {
        throw new RuntimeException("Failed to setup the SuperUser account", err);
      }
    }

    System.out.println("Deploying the application");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(
        new MainVerticle(
            userDataProvider,
            pageDataProvider,
            revisionDataProvider,
            commentDataProvider,
            categoryDataProvider,
            versionProvider,
            fileProvider,
            port,
            baseUri,
            authMethods.isEmpty() ? null : authMethods,
            new MarkdownPageRenderer()));
  }
}
