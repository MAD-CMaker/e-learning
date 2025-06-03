package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Comment;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentDAO extends AbstractDAO {

  public CommentDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Salva um novo comentário no banco de dados.
   *
   * @param comment O objeto Comentario a ser salvo.
   * @return O objeto Comentario com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Comment saveComment(Comment comment) throws SQLException {
    String sql = "INSERT INTO courses_comments (course_id, student_id, text, hour_date) VALUES (?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, comment.getCourseId());
      pstmt.setInt(2, comment.getStudentId());
      pstmt.setString(3, comment.getText());
      pstmt.setTimestamp(4, Timestamp.valueOf(comment.getHourDate() != null ? comment.getHourDate() : LocalDateTime.now()));

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to create comment, no lines affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          comment.setCommentId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to create comment, no ID obtained.");
        }
      }
      return comment;
    }
  }

  /**
   * Busca um comentário pelo seu ID.
   *
   * @param idComment O ID do comentário.
   * @return Um objeto Comentario se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Comment searchCommentById(int idComment) throws SQLException {
    String sql = "SELECT * FROM courses_comments WHERE comment_id = ?";
    Comment comment = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idComment);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        comment = mapResultSetComments(rs);
      }
    }
    return comment;
  }

  /**
   * Lista todos os comentários de um curso específico, ordenados pela data/hora.
   *
   * @param idCourse O ID do curso.
   * @return Uma lista de Comentarios.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Comment> listCoursesComments(int idCourse) throws SQLException {
    List<Comment> comments = new ArrayList<>();
    String sql = "SELECT * FROM courses_comments WHERE course_id = ? ORDER BY hour_date DESC";


    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          comments.add(mapResultSetComments(rs));
        }
      }
    }
    return comments;
  }

  /**
   * Lista todos os comentários feitos por um aluno específico.
   *
   * @param idStudent O ID do aluno.
   * @return Uma lista de Comentarios.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Comment> listStudentsComments(int idStudent) throws SQLException {
    List<Comment> comments = new ArrayList<>();
    String sql = "SELECT * FROM courses_comments WHERE student_id = ? ORDER BY hour_date DESC";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idStudent);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          comments.add(mapResultSetComments(rs));
        }
      }
    }
    return comments;
  }


  /**
   * Atualiza o texto de um comentário existente.
   * Apenas o autor do comentário ou um administrador/professor poderia fazer isso.
   * (A lógica de permissão ficará no Service).
   *
   * @param comment O objeto Comentario com o texto atualizado.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateComment(Comment comment) throws SQLException {
    String sql = "UPDATE courses_comments SET text = ?, hour_date = ? WHERE comment_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, comment.getText());
      pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setInt(3, comment.getCommentId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta um comentário do banco de dados.
   *
   * @param idComment O ID do comentário a ser deletado.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteComment(int idComment) throws SQLException {
    String sql = "DELETE FROM courses_comments WHERE comment_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idComment);
      return pstmt.executeUpdate() > 0;
    }
  }

  private Comment mapResultSetComments(ResultSet rs) throws SQLException {
    Comment comment = new Comment();
    comment.setCommentId(rs.getInt("comment_id"));
    comment.setCourseId(rs.getInt("course_id"));
    comment.setStudentId(rs.getInt("student_id"));
    comment.setText(rs.getString("text"));
    Timestamp tsDataHora = rs.getTimestamp("hour_date");
    if (tsDataHora != null) {
      comment.setHourDate(tsDataHora.toLocalDateTime());
    }
    return comment;
  }
}

