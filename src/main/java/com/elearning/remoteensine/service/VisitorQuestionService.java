package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.VisitorQuestionDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.VisitorQuestion;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class VisitorQuestionService {

  private VisitorQuestionDAO questionDAO;
  private UserDAO userDAO;

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  public VisitorQuestionService(VisitorQuestionDAO questionDAO, UserDAO userDAO) {
    this.questionDAO = questionDAO;
    this.userDAO = userDAO;
  }

  /**
   * Permite que um visitante submeta uma pergunta.
   *
   * @param visitorName Nome do visitante (opcional).
   * @param visitorEmail E-mail do visitante (para possível contato).
   * @param questionText O texto da pergunta.
   * @return A pergunta submetida.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados essenciais (e-mail, texto) forem inválidos.
   */
  public VisitorQuestion submitQuestion(String visitorName, String visitorEmail, String questionText  )
      throws SQLException, IllegalArgumentException {

    if (visitorEmail == null || visitorEmail.trim().isEmpty() || !EMAIL_PATTERN.matcher(visitorEmail).matches()) {
      throw new IllegalArgumentException("\n" +
          "Invalid visitor email format.");
    }
    if (questionText == null || questionText.trim().isEmpty()) {
      throw new IllegalArgumentException("The question text cannot be empty.");
    }

    String nome = (visitorName != null && !visitorName.trim().isEmpty()) ? visitorName.trim() : "Visitante Anônimo";

    VisitorQuestion newQuestion = new VisitorQuestion(nome, visitorEmail.trim(), questionText.trim());
    return questionDAO.saveQuestion(newQuestion);
  }

  /**
   * Permite que um professor responda a uma pergunta de visitante.
   *
   * @param idQuestion ID da pergunta a ser respondida.
   * @param idProfessor ID do professor que está respondendo.
   * @param questionText Texto da resposta.
   * @return A pergunta atualizada com a resposta.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos, pergunta ou professor não encontrado.
   */
  public VisitorQuestion answerQuestion(int idQuestion, int idProfessor, String questionText)
      throws SQLException, IllegalArgumentException {

    if (questionText == null || questionText.trim().isEmpty()) {
      throw new IllegalArgumentException("The question text cannot be empty.");
    }

    VisitorQuestion question = questionDAO.searchById(idQuestion);
    if (question == null) {
      throw new IllegalArgumentException("Question ID " + idQuestion + " not found.");
    }

    if (question.getAnswer() != null && !question.getAnswer().isEmpty()) {
      throw new IllegalArgumentException("This question has already been answered.");
    }

    User userProfessor = userDAO.searchById(idProfessor);
    if (userProfessor == null || userProfessor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Professor ID " + idProfessor + " not found.");
    }
    Professor professor = (Professor) userProfessor;

    question.answerQuestionByProfessor(questionText, professor);
    // O método adicionarRespostaPeloProfessor já atualiza dataHoraResposta e idResponsavelResposta.

    if (questionDAO.updateQuestion(question)) {
      return question;
    } else {
      throw new SQLException("Failed to update question with answer.");
    }
  }

  /**
   * Busca uma pergunta de visitante pelo ID.
   *
   * @param idQuestion ID da pergunta.
   * @return A pergunta encontrada, ou null.
   * @throws SQLException Erro de banco.
   */
  public VisitorQuestion searchQuestion(int idQuestion) throws SQLException {
    VisitorQuestion question = questionDAO.searchById(idQuestion);
    return question;
  }

  /**
   * Lista todas as perguntas de visitantes, com opção de filtrar apenas as não respondidas.
   *
   * @param onlyUnanswered true para listar apenas perguntas sem resposta.
   * @return Lista de perguntas.
   * @throws SQLException Erro de banco.
   */
  public List<VisitorQuestion> listAllQuestions(boolean onlyUnanswered) throws SQLException {
    return questionDAO.listAllQuestions(onlyUnanswered);
  }

  /**
   * Deleta uma pergunta de visitante (ex: por um administrador ou professor).
   * @param idQuestion ID da pergunta a ser deletada.
   * @param idUserLogged ID do usuário (professor) tentando a ação.
   * @return true se deletado.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se pergunta não encontrada.
   * @throws IllegalAccessException Se usuário não for professor (regra de exemplo).
   */
  public boolean deleteVisitorQuestions(int idQuestion, int idUserLogged) throws SQLException, IllegalArgumentException, IllegalAccessException {
    VisitorQuestion question = questionDAO.searchById(idQuestion);
    if (question == null) {
      throw new IllegalArgumentException("Question ID " + idQuestion + " not found.");
    }

    User loggedUser = userDAO.searchById(idUserLogged);
    if (loggedUser == null || loggedUser.getUserType() != UserType.PROFESSOR) {
      throw new IllegalAccessException("User not authorized to delete visitor questions.");
    }

    return questionDAO.deleteQuestion(idQuestion);
  }
}
