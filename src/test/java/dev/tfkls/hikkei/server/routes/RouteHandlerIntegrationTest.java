package dev.tfkls.hikkei.server.routes;

import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.MainVerticle;
import dev.tfkls.hikkei.test.IntegrationTest;
import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RouteHandlerIntegrationTest extends IntegrationTest {
  protected UserDataProvider userProvider;
  protected PageDataProvider pageProvider;
  protected RevisionDataProvider revisionProvider;
  protected CommentDataProvider commentProvider;
  protected CategoryDataProvider categoryProvider;
  protected VersionProvider versionProvider;

  @BeforeAll
  void initServer(Vertx vertx, VertxTestContext testContext) {
    Integer port = Integer.valueOf(require("PORT"));
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    userProvider = mock();
    pageProvider = mock();
    revisionProvider = mock();
    commentProvider = mock();
    categoryProvider = mock();
    versionProvider = mock();

    vertx
        .deployVerticle(
            new MainVerticle(
                userProvider,
                pageProvider,
                revisionProvider,
                commentProvider,
                categoryProvider,
                versionProvider,
                port))
        .onComplete(testContext.succeedingThenComplete());
  }

  @BeforeEach
  void resetMocks() {
    Mockito.reset(
        userProvider,
        pageProvider,
        revisionProvider,
        commentProvider,
        categoryProvider,
        versionProvider);
  }
}
