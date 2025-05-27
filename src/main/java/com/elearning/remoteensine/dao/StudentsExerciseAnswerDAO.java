package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.StudentsExerciseAnswer;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentsExerciseAnswerDAO {

  /**
   * Salva a resposta de um aluno a um exercício.
   * Se já existir uma resposta para este aluno e exercício (devido à UNIQUE KEY),
   * este método lançará uma SQLException. Considere um método update ou delete+insert.
   * Por simplicidade, este método apenas tenta inserir.
   */
  public StudentsExerciseAnswer saveAnswer(StudentsExerciseAnswer answer) throws SQLException {
    String sql = "INSERT INTO student_exercise_answers " +
        "(id_exercise, id_student, id_class, id_course, answer_text, send_date, is_correct, grade, feedback_professor) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, answer.getIdExercise());
      pstmt.setInt(2, answer.getIdStudent());
      pstmt.setInt(3, answer.getIdClass());
      pstmt.setInt(4, answer.getIdCourse());
      pstmt.setString(5, answer.getAnswerText());
      pstmt.setTimestamp(6, Timestamp.valueOf(answer.getSendDate() != null ? answer.getSendDate() : LocalDateTime.now()));

      if (answer.getCorrect() != null) {
        pstmt.setBoolean(7, answer.getCorrect());
      } else {
        pstmt.setNull(7, Types.BOOLEAN);
      }
      if (answer.getGrade() != null) {
        pstmt.setDouble(8, answer.getGrade());
      } else {
        pstmt.setNull(8, Types.DECIMAL);
      }
      pstmt.setString(9, answer.getFeedbackProfessor());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Falha ao salvar resposta do aluno, nenhuma linha afetada.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          answer.setIdStudentAnswerExercise(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Falha ao salvar resposta do aluno, nenhum ID obtido.");
        }
      }
      return answer;
    }
  }

  /**
   * Busca a resposta de um aluno para um exercício específico.
   * Útil para verificar se já respondeu ou para carregar uma resposta existente.
   */
  public StudentsExerciseAnswer getAnswerByStudentAndExercise(int studentId, int exerciseId) throws SQLException {
    String sql = "SELECT * FROM student_exercise_answers WHERE id_student = ? AND id_exercise = ?";
    StudentsExerciseAnswer answer = null;
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, studentId);
      pstmt.setInt(2, exerciseId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          answer = mapResultSetToAnswer(rs);
        }
      }
    }
    return answer;
  }

  /**
   * Lista todas as respostas de um aluno para uma aula (classroom) específica.
   */
  public List<StudentsExerciseAnswer> getAnswersByStudentAndClassroom(int studentId, int classroomId) throws SQLException {
    List<StudentsExerciseAnswer> answers = new ArrayList<>();
    String sql = "SELECT * FROM student_exercise_answers WHERE id_student = ? AND id_class = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, studentId);
      pstmt.setInt(2, classroomId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          answers.add(mapResultSetToAnswer(rs));
        }
      }
    }
    return answers;
  }

  /**
   * Atualiza uma resposta existente.
   * Útil se você permitir que o aluno edite a resposta ou se o professor for adicionar nota/feedback.
   */
  public boolean updateAnswer(StudentsExerciseAnswer answer) throws SQLException {
    String sql = "UPDATE student_exercise_answers SET " +
        "answer_text = ?, send_date = ?, is_correct = ?, grade = ?, feedback_professor = ? " +
        "WHERE id_student_answer_exercise = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, answer.getAnswerText());
      pstmt.setTimestamp(2, Timestamp.valueOf(answer.getSendDate() != null ? answer.getSendDate() : LocalDateTime.now()));
      if (answer.getCorrect() != null) {
        pstmt.setBoolean(3, answer.getCorrect());
      } else {
        pstmt.setNull(3, Types.BOOLEAN);
      }
      if (answer.getGrade() != null) {
        pstmt.setDouble(4, answer.getGrade());
      } else {
        pstmt.setNull(4, Types.DECIMAL);
      }
      pstmt.setString(5, answer.getFeedbackProfessor());
      pstmt.setInt(6, answer.getIdStudentAnswerExercise());

      return pstmt.executeUpdate() > 0;
    }
  }


  private StudentsExerciseAnswer mapResultSetToAnswer(ResultSet rs) throws SQLException {
    StudentsExerciseAnswer answer = new StudentsExerciseAnswer();
    answer.setIdStudentAnswerExercise(rs.getInt("id_student_answer_exercise"));
    answer.setIdExercise(rs.getInt("id_exercise"));
    answer.setIdStudent(rs.getInt("id_student"));
    answer.setIdClass(rs.getInt("id_class"));
    answer.setIdCourse(rs.getInt("id_course"));
    answer.setAnswerText(rs.getString("answer_text"));

    Timestamp tsSendDate = rs.getTimestamp("send_date");
    if (tsSendDate != null) {
      answer.setSendDate(tsSendDate.toLocalDateTime());
    }

    answer.setCorrect(rs.getObject("is_correct") != null ? rs.getBoolean("is_correct") : null);
    answer.setGrade(rs.getObject("grade") != null ? rs.getDouble("grade") : null);

    answer.setFeedbackProfessor(rs.getString("feedback_professor"));
    return answer;
  }
}