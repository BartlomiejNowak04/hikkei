package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCPermissionDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCPermissionDataProvider provider;

  @BeforeAll
  public void initProvider() {
    provider = new JDBCPermissionDataProvider(connection);
  }

  @Test
  void createRole() throws InternalErrorException, SQLException {
    Integer res = provider.createRole("test role");
    PreparedStatement st = connection.prepareStatement("SELECT name FROM roles WHERE id = ?");
    st.setInt(1, res);
    ResultSet rs = st.executeQuery();
    assertTrue(rs.next());
    assertEquals("test role", rs.getString("name"));
  }

  @Test
  void createCategory() throws InternalErrorException, SQLException {
    Integer res = provider.createCategory("test cat");
    PreparedStatement st = connection.prepareStatement("SELECT name FROM categories WHERE id = ?");
    st.setInt(1, res);
    ResultSet rs = st.executeQuery();
    assertTrue(rs.next());
    assertEquals("test cat", rs.getString("name"));
  }
}
