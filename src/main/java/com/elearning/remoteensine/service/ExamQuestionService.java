package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ExamDefinitionDAO;
import com.elearning.remoteensine.dao.ExamQuestionDAO;
import com.elearning.remoteensine.dao.CourseDAO; // Para buscar o curso e verificar o professor
import com.elearning.remoteensine.dao.UserDAO;     // Para verificar o tipo de usuário
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.ExerciseType; // Seu enum
import com.elearning.remoteensine.model.enums.UserType;     // Seu enum

import org.springframework.stereotype.Service;
// Removido @Autowired pois estamos instanciando DAOs manualmente como nos exemplos anteriores.
// Se você migrar para injeção de dependência completa, precisará dos DAOs como beans @Repository e @Autowired aqui.


import java.sql.SQLException;
import java.util.List;

@Service
public class ExamQuestionService {

  private final ExamQuestionDAO examQuestionDAO;
  private final ExamDefinitionDAO examDefinitionDAO;
  private final CourseDAO courseDAO;
  private final UserDAO userDAO;

  public ExamQuestionService(ExamQuestionDAO examQuestionDAO, ExamDefinitionDAO examDefinitionDAO, CourseDAO courseDAO, UserDAO userDAO) {
    this.examQuestionDAO = examQuestionDAO;
    this.examDefinitionDAO = examDefinitionDAO;
    this.courseDAO = courseDAO;
    this.userDAO = userDAO;
  }

  /**
   * Adiciona uma nova questão a uma definição de exame existente.
   *
   * @param idExamDefinition ID da definição do exame.
   * @param idProfessorLogado ID do professor que está adicionando a questão.
   * @param enunciado Enunciado da questão.
   * @param tipoQuestao Tipo da questão.
   * @param opcoes Opções da questão (ex: JSON para múltipla escolha).
   * @param respostaCorreta Resposta correta/gabarito.
   * @param pontuacao Pontuação da questão.
   * @param ordemNaProva Ordem da questão no exame.
   * @return A QuestaoExame criada e salva.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos ou entidades não encontradas.
   * @throws IllegalAccessException Se o professor não tiver permissão.
   */
  public ExamQuestion addQuestionToExamDefinition(int idExamDefinition, int idProfessorLogado,
                                                  String enunciado, ExerciseType tipoQuestao, String opcoes,
                                                  String respostaCorreta, double pontuacao, int ordemNaProva)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    User professor = userDAO.searchById(idProfessorLogado);
    if (professor == null || professor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Usuário com ID " + idProfessorLogado + " não é um professor válido.");
    }

    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }

    Course course = courseDAO.searchCourseById(examDef.getIdCourse());
    if (course == null) {
      throw new IllegalArgumentException("Curso associado à definição de exame (ID Curso: " + examDef.getIdCourse() + ") não encontrado.");
    }
    if (course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a adicionar questões a este exame.");
    }


    if (enunciado == null || enunciado.trim().isEmpty()) {
      throw new IllegalArgumentException("O enunciado da questão não pode ser vazio.");
    }
    if (tipoQuestao == null) {
      throw new IllegalArgumentException("O tipo da questão é obrigatório.");
    }

    ExamQuestion novaQuestao = new ExamQuestion(
        idExamDefinition, enunciado, tipoQuestao, opcoes,
        respostaCorreta, pontuacao, ordemNaProva
    );

    return examQuestionDAO.saveQuestion(novaQuestao);
  }

  /**
   * Lista todas as questões de uma definição de exame específica.
   * Requer que o usuário logado seja o professor dono do curso ao qual o exame pertence.
   *
   * @param idExamDefinition ID da definição do exame.
   * @param idProfessorLogado ID do professor para verificação de permissão.
   * @return Lista de QuestaoExame.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se ExamDefinition ou Curso não encontrado.
   * @throws IllegalAccessException Se o professor não tiver permissão.
   */
  public List<ExamQuestion> getQuestionsForExamDefinition(int idExamDefinition, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    User professor = userDAO.searchById(idProfessorLogado);
    if (professor == null || professor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Usuário não é um professor válido.");
    }

    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }

    Course course = courseDAO.searchCourseById(examDef.getIdCourse());
    if (course == null) {
      throw new IllegalArgumentException("Curso associado à definição de exame não encontrado.");
    }

    if (course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a visualizar questões deste exame.");
    }

    return examQuestionDAO.findQuestionsByExamDefinitionId(idExamDefinition);
  }

  public ExamQuestion getQuestionById(int idQuestaoExame, int idProfessorLogado) throws SQLException, IllegalAccessException {
    ExamQuestion question = examQuestionDAO.findQuestionById(idQuestaoExame);
    if (question != null) {
      ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(question.getIdDefinitionExam());
      if(examDef != null) {
        Course course = courseDAO.searchCourseById(examDef.getIdCourse());
        if (course != null && course.getResponsibleProfessor() != null && course.getResponsibleProfessor().getIdUser() == idProfessorLogado) {
          return question;
        }
      }
    }
    throw new IllegalAccessException("Não foi possível buscar a questão ou acesso não autorizado.");
  }


  public boolean updateExamQuestion(ExamQuestion question, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {
    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(question.getIdDefinitionExam());
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame associada à questão não encontrada.");
    }
    Course course = courseDAO.searchCourseById(examDef.getIdCourse());
    if (course == null || course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a atualizar questões deste exame.");
    }

    return examQuestionDAO.updateQuestion(question);
  }

  public boolean deleteExamQuestion(int idQuestaoExame, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {
    ExamQuestion question = examQuestionDAO.findQuestionById(idQuestaoExame);
    if (question == null) {
      throw new IllegalArgumentException("Questão com ID " + idQuestaoExame + " não encontrada.");
    }
    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(question.getIdDefinitionExam());
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame associada à questão não encontrada.");
    }
    Course course = courseDAO.searchCourseById(examDef.getIdCourse());
    if (course == null || course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a deletar questões deste exame.");
    }

    return examQuestionDAO.deleteQuestion(idQuestaoExame);
  }

  /**
   * Busca as questões de uma definição de exame específica para um aluno realizar.
   * Pressupõe que as verificações de que a prova está publicada e o aluno está matriculado
   * já foram feitas no controller.
   *
   * @param idExamDefinition ID da definição do exame.
   * @return Lista de QuestaoExame.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se a ExamDefinition não for encontrada.
   */
  public List<ExamQuestion> getQuestionsForStudentToTakeExam(int idExamDefinition)
      throws SQLException, IllegalArgumentException {

    System.out.println("SERVICE (ExamQuestion): Buscando questões para aluno para ExamDef ID: " + idExamDefinition);
    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }
    return examQuestionDAO.findQuestionsByExamDefinitionId(idExamDefinition);
  }
}