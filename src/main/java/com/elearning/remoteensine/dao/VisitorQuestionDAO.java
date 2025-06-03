package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.VisitorQuestion;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VisitorQuestionDAO extends AbstractDAO {

  public VisitorQuestionDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Salva uma nova pergunta de visitante no banco de dados.
   *
   * @param question O objeto PerguntaVisitante a ser salvo.
   * @return O objeto PerguntaVisitante com o ID preenchido.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public VisitorQuestion saveQuestion(VisitorQuestion question) throws SQLException {
    String sql = "INSERT INTO visitors_questions (visitor_name, visitor_email, question_text, question_hour_date) " +
        "VALUES (?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, question.getVisitorName());
      pstmt.setString(2, question.getVisitorEmail());
      pstmt.setString(3, question.getQuestionText());
      pstmt.setTimestamp(4, Timestamp.valueOf(question.getQuestionHour() != null ? question.getQuestionHour() : LocalDateTime.now()));

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to save visitor question, no rows affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          question.setQuestionId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to save visitor question, no ID obtained.");
        }
      }
      return question;
    }
  }

  /**
   * Busca uma pergunta de visitante pelo seu ID.
   *
   * @param idQuestion O ID da pergunta.
   * @return Um objeto PerguntaVisitante se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public VisitorQuestion searchById(int idQuestion) throws SQLException {
    String sql = "SELECT * FROM visitors_questions WHERE question_id = ?";
    VisitorQuestion question = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idQuestion);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        question = mapResultSetForQuestion(rs);
      }
    }
    return question;
  }

  /**
   * Lista todas as perguntas de visitantes. Pode-se adicionar filtros (ex: apenas não respondidas).
   *
   * @param onlyUnanswered Se true, lista apenas perguntas sem resposta.
   * @return Uma lista de PerguntaVisitante.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<VisitorQuestion> listAllQuestions(boolean onlyUnanswered) throws SQLException {
    List<VisitorQuestion> questions = new ArrayList<>();
    String sql = "SELECT * FROM visitors_questions";
    if (onlyUnanswered) {
      sql += " WHERE answer IS NULL";
    }
    sql += " ORDER BY question_hour_date DESC";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        questions.add(mapResultSetForQuestion(rs));
      }
    }
    return questions;
  }

  /**
   * Atualiza uma pergunta de visitante, geralmente para adicionar uma resposta.
   *
   * @param question O objeto PerguntaVisitante com os dados atualizados (resposta, etc.).
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateQuestion(VisitorQuestion question) throws SQLException {
    String sql = "UPDATE visitors_questions SET answer = ?, answer_hour_date = ?, professor_responsible_id = ? " +
        "WHERE question_id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, question.getAnswer());
      if (question.getAnswerHour() != null) {
        pstmt.setTimestamp(2, Timestamp.valueOf(question.getAnswerHour()));
      } else {
        pstmt.setNull(2, Types.TIMESTAMP);
      }
      if (question.getProfessorResponsibleId() != null && question.getProfessorResponsibleId() > 0) {
        pstmt.setInt(3, question.getProfessorResponsibleId());
      } else {
        pstmt.setNull(3, Types.INTEGER);
      }
      pstmt.setInt(4, question.getQuestionId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta uma pergunta de visitante.
   *
   * @param idQuestion O ID da pergunta a ser deletada.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteQuestion(int idQuestion) throws SQLException {
    String sql = "DELETE FROM visitors_questions WHERE question_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idQuestion);
      return pstmt.executeUpdate() > 0;
    }
  }

  private VisitorQuestion mapResultSetForQuestion(ResultSet rs) throws SQLException {
    VisitorQuestion question = new VisitorQuestion();
    question.setQuestionId(rs.getInt("question_id"));
    question.setVisitorName(rs.getString("visitor_name"));
    question.setVisitorEmail(rs.getString("visitor_email"));
    question.setQuestionText(rs.getString("question_text"));

    Timestamp tsPergunta = rs.getTimestamp("question_hour_date");
    if (tsPergunta != null) {
      question.setQuestionHour(tsPergunta.toLocalDateTime());
    }

    question.setAnswer(rs.getString("answer"));

    Timestamp tsAnswer = rs.getTimestamp("answer_hour_date");
    if (tsAnswer != null) {
      question.setQuestionHour(tsAnswer.toLocalDateTime());
    }

    Integer idResp = rs.getInt("professor_responsible_id");
    if (rs.wasNull()) {
      question.setProfessorResponsibleId(null);
    } else {
      question.setProfessorResponsibleId(idResp);
    }
    return question;
  }
}

