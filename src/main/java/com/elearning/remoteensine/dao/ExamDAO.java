package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Exam;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class  ExamDAO {

  /**
   * Salva uma nova avaliação no banco de dados.
   *
   * @param exam O objeto Avaliacao a ser salvo.
   * @return O objeto Avaliacao com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados,
   * incluindo violação da restrição UNIQUE (aluno já avaliou o curso).
   */
  public Exam saveExam(Exam exam) throws SQLException {
    String sql = "INSERT INTO exams_courses (course_id, id_exam_definition, student_id, grade, comment, hour_date) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setInt(1, exam.getCourseId());
      pstmt.setInt(2, exam.getIdExamDefinition());
      pstmt.setInt(3, exam.getStudentId());
      pstmt.setInt(4, exam.getGrade());
      pstmt.setString(5, exam.getComment());
      pstmt.setTimestamp(6, Timestamp.valueOf(exam.getHourDate() != null ? exam.getHourDate() : LocalDateTime.now()));
      int affectedRows = pstmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Falha ao salvar tentativa do exame, nenhuma linha afetada.");
      }
      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          exam.setExamId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Falha ao salvar tentativa do exame, nenhum ID obtido.");
        }
      }
      return exam;
    }
  }

  /**
   * Busca uma avaliação pelo seu ID.
   *
   * @param idExam O ID da avaliação.
   * @return Um objeto Avaliacao se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Exam searchExamById(int idExam) throws SQLException {
    String sql = "SELECT * FROM exams_curses WHERE exam_id = ?";
    Exam exam = null;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idExam);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        exam = mapResultSetExam(rs);
      }
    }
    return exam;
  }

  /**
   * Busca a avaliação de um aluno específico para um curso específico.
   * Útil para verificar se o aluno já avaliou ou para carregar sua avaliação.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return A Avaliacao se existir, ou null.
   * @throws SQLException Erro de banco.
   */
  public Exam searchExamByStundentACourse(int idStudent, int idCourse) throws SQLException {
    String sql = "SELECT * FROM avaliacoes_curso WHERE id_student = ? AND id_curso = ?";
    Exam exam = null;
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idStudent);
      pstmt.setInt(2, idCourse);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        exam = mapResultSetExam(rs);
      }
    }
    return exam;
  }


  /**
   * Lista todas as avaliações de um curso específico, ordenadas pela data/hora.
   *
   * @param idCourse O ID do curso.
   * @return Uma lista de Avaliacoes.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Exam> listCoursesExams(int idCourse) throws SQLException {
    List<Exam> exams = new ArrayList<>();
    String sql = "SELECT * FROM exams_courses WHERE course_id = ? ORDER BY hour_date DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          exams.add(mapResultSetExam(rs));
        }
      }
    }
    return exams;
  }

  /**
   * Calcula a média das notas de um curso específico.
   *
   * @param idCourse O ID do curso.
   * @return A média das notas, ou 0.0 se não houver avaliações.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public double mediaCoursesGrade(int idCourse) throws SQLException {
    String sql = "SELECT AVG(grade) FROM exams_courses WHERE course_id = ?";
    double media = 0.0;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          media = rs.getDouble(1);
        }
      }
    }
    return media;
  }


  /**
   * Atualiza uma avaliação existente (ex: o aluno edita seu comentário ou nota).
   *
   * @param exam O objeto Avaliacao com os dados atualizados.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateExam(Exam exam) throws SQLException {
    String sql = "UPDATE exams_courses SET nota = ?, comment = ?, hour_date = ? WHERE exam_id = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, exam.getGrade());
      pstmt.setString(2, exam.getComment());
      pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setInt(4, exam.getExamId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta uma avaliação do banco de dados.
   *
   * @param idExam O ID da avaliação a ser deletada.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteExam(int idExam) throws SQLException {
    String sql = "DELETE FROM exams_courses WHERE exam_id = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExam);
      return pstmt.executeUpdate() > 0;
    }
  }



  private Exam mapResultSetExam(ResultSet rs) throws SQLException {
    Exam exam = new Exam();
    exam.setExamId(rs.getInt("exam_id"));
    exam.setCourseId(rs.getInt("course_id"));
    exam.setStudentId(rs.getInt("student_id"));
    exam.setGrade(rs.getInt("grade"));
    exam.setComment(rs.getString("comment"));
    Timestamp tsDataHora = rs.getTimestamp("hour_date");
    if (tsDataHora != null) {
      exam.setHourDate(tsDataHora.toLocalDateTime());
    }
    return exam;
  }

  
}

