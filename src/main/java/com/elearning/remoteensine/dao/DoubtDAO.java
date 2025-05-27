package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Doubt;
import com.elearning.remoteensine.model.enums.DoubtStatus;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoubtDAO {
  /**
   * Salva uma nova dúvida no banco de dados.
   *
   * @param doubt O objeto Duvida a ser salvo.
   * @return O objeto Duvida com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Doubt saveDoubt(Doubt doubt) throws SQLException {
    String sql = "INSERT INTO courses_doubts (course_id, student_id, title, description, creation_hour_date, status) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, doubt.getCourseId());
      pstmt.setInt(2, doubt.getStudentId());
      pstmt.setString(3, doubt.getTitle());
      pstmt.setString(4, doubt.getDescription());
      pstmt.setTimestamp(5, Timestamp.valueOf(doubt.getCreationHour() != null ? doubt.getCreationHour() : LocalDateTime.now()));
      pstmt.setString(6, (doubt.getStatus() != null ? doubt.getStatus() : DoubtStatus.ABERTA).name());


      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to create query, no rows affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          doubt.setDoubtId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to create query, no ID obtained.");
        }
      }
      return doubt;
    }
  }

  /**
   * Busca uma dúvida pelo seu ID.
   *
   * @param idDoubt O ID da dúvida a ser buscada.
   * @return Um objeto Duvida se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Doubt searchDoubtById(int idDoubt) throws SQLException {
    String sql = "SELECT * FROM courses_doubts WHERE doubt_id = ?";
    Doubt doubt = null;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idDoubt);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        doubt = mapResultsSetForDoubt(rs);
      }
    }
    return doubt;
  }

  /**
   * Lista todas as dúvidas de um curso específico.
   *
   * @param idCourse O ID do curso.
   * @return Uma lista de Dúvidas.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Doubt> lisCoursesDoubt(int idCourse) throws SQLException {
    List<Doubt> doubts = new ArrayList<>();
    String sql = "SELECT * FROM courses_doubts WHERE course_id = ? ORDER BY creation_hour_date DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          doubts.add(mapResultsSetForDoubt(rs));
        }
      }
    }
    return doubts;
  }

  /**
   * Lista todas as dúvidas criadas por um aluno específico.
   *
   * @param idStudent O ID do aluno.
   * @return Uma lista de Dúvidas.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Doubt> listStudentsDoubts(int idStudent) throws SQLException {
    List<Doubt> doubts = new ArrayList<>();
    String sql = "SELECT * FROM courses_doubts WHERE student_id = ? ORDER BY creation_hour_date DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idStudent);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          doubts.add(mapResultsSetForDoubt(rs));
        }
      }
    }
    return doubts;
  }


  /**
   * Atualiza uma dúvida, geralmente para adicionar uma resposta.
   *
   * @param doubt O objeto Duvida com os dados atualizados (resposta, status, etc.).
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateDouts(Doubt doubt) throws SQLException {
    String sql = "UPDATE courses_doubts SET answer = ?, creation_hour_date = ?, answer_professor_id = ?, status = ? " +
        "WHERE doubt_id = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, doubt.getAnswer());
      if (doubt.getAnswerHour() != null) {
        pstmt.setTimestamp(2, Timestamp.valueOf(doubt.getAnswerHour()));
      } else {
        pstmt.setNull(2, Types.TIMESTAMP);
      }
      if (doubt.getProfessorId() != null && doubt.getProfessorId() > 0) {
        pstmt.setInt(3, doubt.getProfessorId());
      } else {
        pstmt.setNull(3, Types.INTEGER);
      }
      pstmt.setString(4, doubt.getStatus().name());
      pstmt.setInt(5, doubt.getDoubtId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta uma dúvida do banco de dados pelo seu ID.
   *
   * @param idDoubt O ID da dúvida a ser deletada.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteDoubt(int idDoubt) throws SQLException {
    String sql = "DELETE FROM courses_doubts WHERE doubt_id = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idDoubt);
      return pstmt.executeUpdate() > 0;
    }
  }

  private Doubt mapResultsSetForDoubt(ResultSet rs) throws SQLException {
    Doubt doubt = new Doubt();
    doubt.setDoubtId(rs.getInt("doubt_id"));
    doubt.setCourseId(rs.getInt("course_id"));
    doubt.setStudentId(rs.getInt("student_id"));
    doubt.setTitle(rs.getString("title"));
    doubt.setDescription(rs.getString("description"));
    Timestamp tsCriation = rs.getTimestamp("creation_hour_date");
    if (tsCriation != null) {
      doubt.setCreationHour(tsCriation.toLocalDateTime());
    } else {
      doubt.setCreationHour(null);
      System.err.println("Warning: creation_hour_date is null in the bank for doubt ID: " + rs.getInt("doubt_id"));
    }

    doubt.setAnswer(rs.getString("answer"));

    Timestamp tsAnswer = rs.getTimestamp("creation_hour_date");
    if (tsAnswer != null) {
      doubt.setCreationHour(tsAnswer.toLocalDateTime());
    } else {
      doubt.setCreationHour(null);
    }
    Integer idProfResp = rs.getInt("answer_professor_id");
    if (rs.wasNull()) {
      doubt.setProfessorId(null);
    } else {
      doubt.setProfessorId(idProfResp);
    }
    String statusStr = rs.getString("status");
    if (statusStr != null) {
      doubt.setStatus(DoubtStatus.valueOf(statusStr));
    } else {
      System.err.println("Warning: status is null in the bank for doubt ID: " + rs.getInt("doubt_id"));
    }

    return doubt;
  }
}

