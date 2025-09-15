package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCPageDataProvider implements PageDataProvider {
  private final Connection connection;

  public JDBCPageDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesPageExist(int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT COUNT(*) FROM pages WHERE id = ? AND deleted = false")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        rs.next();
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public PageData getRandomPage() throws InternalErrorException {
    try (Statement st = connection.createStatement()) {
      try (ResultSet rs =
          st.executeQuery(
              "SELECT * FROM pages_with_data WHERE deleted = false ORDER BY RANDOM() LIMIT 1")) {
        if (!rs.next()) return null;
        return getPageData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<PageData> searchByTitle(String title) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM pages_with_data WHERE title ILIKE ? AND deleted = false")) {
      st.setString(1, "%" + title + "%");
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<PageData> res = new ArrayList<>();
        while (rs.next()) res.add(getPageData(rs));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public PageData getById(int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM pages_with_data WHERE id = ?")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return getPageData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public PageData getByTitle(String title) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM pages_with_data WHERE title = ?")) {
      st.setString(1, title);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        else return getPageData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<PageData> getByCategory(int categoryId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM pages_with_data WHERE category = ? and deleted = false")) {
      st.setInt(1, categoryId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<PageData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(getPageData(rs));
        }
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public int createPage(PageData data) throws BadDataException, InternalErrorException {
    if (data == null) throw new BadDataException(invalid_data);
    if (data.title() == null) throw new BadDataException(invalid_title);
    if (data.category() == null) throw new BadDataException(invalid_category);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (user_id, title, category) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setObject(1, data.userId());
      st.setString(2, data.title());
      st.setInt(3, data.category());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_page);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(taken_title);
      if (message.contains("page_category_category_fkey"))
        throw new BadDataException(invalid_category);
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      if (message.contains("table categories is marked as deleted"))
        throw new BadDataException(invalid_category);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updatePageTitle(int pageId, String title)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_page);
    if (title == null) throw new BadDataException(invalid_title);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO page_title (title, page_id) values (?, ?)")) {
      st.setString(1, title);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(taken_title);
      if (message.contains("table pages is marked as deleted"))
        throw new BadDataException(invalid_page);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updatePageCategory(int pageId, int categoryId)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_page);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO page_category (category, page_id) values (?, ?)")) {
      st.setInt(1, categoryId);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("pages_category_fkey")) throw new BadDataException(invalid_category);
      if (message.contains("table categories is marked as deleted"))
        throw new BadDataException(invalid_category);
      if (message.contains("table pages is marked as deleted"))
        throw new BadDataException(invalid_page);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void toggleArchive(int pageId, boolean type)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_page);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO page_archived (archived, page_id) values (?, ?)")) {
      st.setBoolean(1, type);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deletePage(int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE pages SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private PageData getPageData(ResultSet rs) throws SQLException {
    if (rs.getBoolean("deleted")) return PageData.DELETED;

    return new PageData(
        rs.getInt("id"),
        rs.getInt("user_id"),
        rs.getString("title"),
        rs.getInt("category"),
        rs.getBoolean("archived"),
        rs.getTimestamp("created").toLocalDateTime());
  }
}
