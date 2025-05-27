package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamDefinitionDAO {

  public ExamDefinition saveExamDefinition(ExamDefinition examDef) throws SQLException {
    String sql = "INSERT INTO exam_definitions (id_course, title, description, creation_date, update_date) " +
        "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, examDef.getIdCourse());
      pstmt.setString(2, examDef.getTitle());
      pstmt.setString(3, examDef.getDescription());
      pstmt.setTimestamp(4, Timestamp.valueOf(examDef.getCreationData() != null ? examDef.getCreationData() : LocalDateTime.now()));
      pstmt.setTimestamp(5, Timestamp.valueOf(examDef.getUpdateData() != null ? examDef.getUpdateData() : LocalDateTime.now()));

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Falha ao salvar definição do exame, nenhuma linha afetada.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          examDef.setIdDefinitionExam(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Falha ao salvar definição do exame, nenhum ID obtido.");
        }
      }
      return examDef;
    }
  }

  public ExamDefinition findExamDefinitionById(int idExamDefinition) throws SQLException {
    String sql = "SELECT * FROM exam_definitions WHERE id_exam_definition = ?";
    ExamDefinition examDef = null;
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamDefinition);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          examDef = mapResultSetToExamDefinition(rs);
        } else {
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
    return examDef;
  }

  public List<ExamDefinition> findExamDefinitionsByCourseId(int idCourse, boolean onlyPublished) throws SQLException {
    List<ExamDefinition> examDefs = new ArrayList<>();
    String sql = "SELECT * FROM exam_definitions WHERE id_course = ?";
    if (onlyPublished) {
      sql += " AND published = TRUE";
    }
    sql += " ORDER BY creation_date DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        int rowCount = 0;
        while (rs.next()) {
          rowCount++;
            ExamDefinition examDef = mapResultSetToExamDefinition(rs);
            if (examDef != null) {
              examDefs.add(examDef);
            }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
    return examDefs;
  }

  public boolean updateExamDefinition(ExamDefinition examDef) throws SQLException {
    String sql = "UPDATE exam_definitions SET title = ?, description = ?, published = ? WHERE id_exam_definition = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, examDef.getTitle());
      pstmt.setString(2, examDef.getDescription());
      pstmt.setBoolean(3, examDef.isPublished());
      pstmt.setInt(4, examDef.getIdDefinitionExam());
      int affectedRows = pstmt.executeUpdate();
      return affectedRows > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public boolean deleteExamDefinition(int idExamDefinition) throws SQLException {
    String sql = "DELETE FROM exam_definitions WHERE id_exam_definition = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamDefinition);
      return pstmt.executeUpdate() > 0;
    }
  }

  private ExamDefinition mapResultSetToExamDefinition(ResultSet rs) throws SQLException {
    ExamDefinition examDef = new ExamDefinition();
    try {
      examDef.setIdDefinitionExam(rs.getInt("id_exam_definition"));
      examDef.setIdCourse(rs.getInt("id_course"));
      examDef.setTitle(rs.getString("title"));
      examDef.setDescription(rs.getString("description"));
      examDef.setPublished(rs.getBoolean("published"));

      Timestamp tsCreation = rs.getTimestamp("creation_date");
      if (tsCreation != null) {
        examDef.setCreationData(tsCreation.toLocalDateTime());
      } else {
        examDef.setCreationData(null);
      }
      Timestamp tsUpdate = rs.getTimestamp("update_date");
      if (tsUpdate != null) {
        examDef.setUpdateData(tsUpdate.toLocalDateTime());
      } else {
        examDef.setUpdateData(null);
      }
    } catch (SQLException e){
      throw e;
    }
    return examDef;
  }
}