package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.util.DatabaseConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class ExamQuestionDAO extends AbstractDAO {

  public ExamQuestionDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  public ExamQuestion saveQuestion(ExamQuestion question) throws SQLException {
    String sql = "INSERT INTO exam_questions (id_exam_definition, statement, exercise_type, options, correct_answer, grade, exam_sequence) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setInt(1, question.getIdDefinitionExam());
      pstmt.setString(2, question.getStatement());
      pstmt.setString(3, question.getExerciseType().name());
      pstmt.setString(4, question.getOptions());
      pstmt.setString(5, question.getCorrectAnswer());
      pstmt.setDouble(6, question.getGrade());
      pstmt.setInt(7, question.getExamSequence());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Falha ao salvar questão do exame, nenhuma linha afetada.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          question.setIdExamQuestion(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Falha ao salvar questão do exame, nenhum ID obtido.");
        }
      }
      return question;
    }
  }

  public ExamQuestion findQuestionById(int idExamQuestion) throws SQLException {
    String sql = "SELECT * FROM exam_questions WHERE id_exam_question = ?";
    ExamQuestion question = null;
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamQuestion);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          question = mapResultSetToExamQuestion(rs);
        }
      }
    }
    return question;
  }

  public List<ExamQuestion> findQuestionsByExamDefinitionId(int idExamDefinition) throws SQLException {
    List<ExamQuestion> questions = new ArrayList<>();
    String sql = "SELECT * FROM exam_questions WHERE id_exam_definition = ? ORDER BY exam_sequence ASC, id_exam_question ASC";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamDefinition);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          questions.add(mapResultSetToExamQuestion(rs));
        }
      }
    }
    return questions;
  }

  public boolean updateQuestion(ExamQuestion question) throws SQLException {
    String sql = "UPDATE exam_questions SET statement = ?, exercise_type = ?, options = ?, " +
        "correct_answer = ?, grade = ?, exam_sequence = ? " +
        "WHERE id_exam_question = ? AND id_exam_definition = ?"; // id_exam_definition para segurança
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, question.getStatement());
      pstmt.setString(2, question.getExerciseType().name());
      pstmt.setString(3, question.getOptions());
      pstmt.setString(4, question.getCorrectAnswer());
      pstmt.setDouble(5, question.getGrade());
      pstmt.setInt(6, question.getExamSequence());
      pstmt.setInt(7, question.getIdExamQuestion());
      pstmt.setInt(8, question.getIdDefinitionExam());
      return pstmt.executeUpdate() > 0;
    }
  }

  public boolean deleteQuestion(int idExamQuestion) throws SQLException {
    String sql = "DELETE FROM exam_questions WHERE id_exam_question = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamQuestion);
      return pstmt.executeUpdate() > 0;
    }
  }

  public int deleteQuestionsByExamDefinitionId(int idExamDefinition) throws SQLException {
    String sql = "DELETE FROM exam_questions WHERE id_exam_definition = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExamDefinition);
      return pstmt.executeUpdate();
    }
  }


  private ExamQuestion mapResultSetToExamQuestion(ResultSet rs) throws SQLException {
    ExamQuestion question = new ExamQuestion();
    question.setIdExamQuestion(rs.getInt("id_exam_question"));
    question.setIdDefinitionExam(rs.getInt("id_exam_definition"));
    question.setStatement(rs.getString("statement"));
    question.setExerciseType(ExerciseType.valueOf(rs.getString("exercise_type")));
    question.setOptions(rs.getString("options"));
    question.setCorrectAnswer(rs.getString("correct_answer"));
    question.setGrade(rs.getDouble("grade"));
    question.setExamSequence(rs.getInt("exam_sequence"));
    question.setParsedOptions(getParsedOptionsToShow(rs.getString("options")));
    return question;
  }

  private List<Map<String, String>> getParsedOptionsToShow(String options) {
    if(options == null) return null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(options, new TypeReference<List<Map<String, String>>>() {});
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }
}