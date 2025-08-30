package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JDBCPageDataProvider implements PageDataProvider {
  private final Connection connection;
  private final String internal_error = "Database internal error";
  private final String invalid_category = "The provided category is invalid";
  private final String invalid_id = "The provided page id is invalid";
  private final String taken_title = "This title is already taken";

  public JDBCPageDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesPageExist(int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM pages WHERE id = ?")) {
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
      try (ResultSet rs = st.executeQuery("SELECT * FROM pages ORDER BY RANDOM() LIMIT 1")) {
        if (!rs.next()) return null;
        return new PageData(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getInt("category"),
            rs.getBoolean("archived"));
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<PageData> searchPageByTitle(String title) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM pages WHERE title ILIKE ?")) {
      st.setString(1, "%" + title + "%");
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<PageData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(
              new PageData(
                  rs.getInt("id"),
                  rs.getString("title"),
                  rs.getInt("category"),
                  rs.getBoolean("archived")));
        }
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public PageData getPageData(int pageId) throws InternalErrorException {
    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM pages WHERE id = ?")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return new PageData(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getInt("category"),
            rs.getBoolean("archived"));
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public PageData getPageByTitle(String title) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM pages WHERE title = ?")) {
      st.setString(1, title);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        else return new PageData(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getBoolean(4));
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public Integer createPage(PageData data) throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages (title, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.title());
      st.setInt(2, data.category());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next())
          throw new InternalErrorException("Couldn't create a new page " + data.title());
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("pages_title_key")) throw new BadDataException(taken_title);
      if (message.contains("pages_category_fkey")) throw new BadDataException(invalid_category);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updatePageTitle(int pageId, String title)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_id);

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET title = ? WHERE id = ?")) {
      st.setString(1, title);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("pages_title_key")) throw new BadDataException(taken_title);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updatePageCategory(int pageId, int categoryId)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_id);

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET category = ? WHERE id = ?")) {
      st.setInt(1, categoryId);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("pages_category_fkey")) throw new BadDataException(invalid_category);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void toggleArchive(int pageId, boolean type)
      throws BadDataException, InternalErrorException {
    if (!doesPageExist(pageId)) throw new BadDataException(invalid_id);

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE pages SET archived = ? WHERE id = ?")) {
      st.setBoolean(1, type);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<RevisionData> getPageRevisions(int pageId) throws InternalErrorException {
    if (!doesPageExist(pageId)) return null;

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM revisions WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<RevisionData> res = new ArrayList<>();
        while (rs.next())
          res.add(
              new RevisionData(
                  rs.getInt(1),
                  rs.getInt(2),
                  rs.getObject(3, UUID.class),
                  rs.getTimestamp(4).toLocalDateTime(),
                  rs.getObject(5, Boolean.class)));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<CommentData> getPageComments(int pageId) throws InternalErrorException {
    if (!doesPageExist(pageId)) return null;

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM comments WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<CommentData> res = new ArrayList<>();
        while (rs.next())
          res.add(
              new CommentData(
                  rs.getInt(1),
                  rs.getInt(2),
                  rs.getString(3),
                  rs.getTimestamp(4).toLocalDateTime(),
                  rs.getBoolean(5)));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }
}
