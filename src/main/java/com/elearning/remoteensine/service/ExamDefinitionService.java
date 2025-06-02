package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.ExamDefinitionDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.ExamDefinition; // Sua classe
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType; // Seu enum
import org.springframework.stereotype.Service;

import java.sql.SQLException;
// import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamDefinitionService {

  private ExamDefinitionDAO examDefinitionDAO;
  private CourseDAO courseDAO;
  private UserDAO userDAO;

  public ExamDefinitionService() {
    this.examDefinitionDAO = new ExamDefinitionDAO();
    this.courseDAO = new CourseDAO();
    this.userDAO = new UserDAO();
  }

  public ExamDefinition createExamDefinition(int idCourse, int idProfessorLogado, String title, String description)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("O título do exame não pode ser vazio.");
    }

    Course curso = courseDAO.searchCourseById(idCourse);
    if (curso == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }

    User professor = userDAO.searchById(idProfessorLogado);
    if (professor == null || professor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Usuário com ID " + idProfessorLogado + " não é um professor válido.");
    }

    if (curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a criar exames para este curso.");
    }

    ExamDefinition newExamDef = new ExamDefinition(idCourse, title, description);
    return examDefinitionDAO.saveExamDefinition(newExamDef);
  }

  public ExamDefinition getExamDefinitionById(int idExamDefinition) throws SQLException {
    return examDefinitionDAO.findExamDefinitionById(idExamDefinition);
  }

  public List<ExamDefinition> getExamDefinitionsForCourse(int idCourse, int idProfessorLogado)
      throws SQLException, IllegalAccessException, IllegalArgumentException {
    Course curso = courseDAO.searchCourseById(idCourse);
    if (curso == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }
    if (curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a ver definições de exame deste curso.");
    }
    return examDefinitionDAO.findExamDefinitionsByCourseId(idCourse, true);
  }

  public boolean updateExamDefinitionDetails(int idExamDefinition, int idCourse, int idProfessorLogado, String newTitle, String newDescription)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }
    if (examDef.getIdCourse() != idCourse) {
      throw new IllegalArgumentException("Esta definição de exame não pertence ao curso especificado (ID: "+ idCourse +").");
    }

    Course curso = courseDAO.searchCourseById(idCourse);
    if (curso == null || curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a modificar este exame.");
    }

    examDef.setTitle(newTitle);
    examDef.setDescription(newDescription);
    examDef.setUpdateData(LocalDateTime.now());
    return examDefinitionDAO.updateExamDefinition(examDef);
  }

  public boolean deleteExamDefinition(int idExamDefinition, int idCourse, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }
    if (examDef.getIdCourse() != idCourse) {
      throw new IllegalArgumentException("Esta definição de exame não pertence ao curso especificado (ID: "+ idCourse +").");
    }

    Course curso = courseDAO.searchCourseById(idCourse);
    if (curso == null || curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a deletar este exame.");
    }
    return examDefinitionDAO.deleteExamDefinition(idExamDefinition);
  }
  public List<ExamDefinition> listarProvasPorCursoPublicadasNaoSubmetidas(int idCurso) throws SQLException {
    System.out.println("SERVICE (Aluno): Chamando DAO para buscar provas publicadas para curso ID: " + idCurso);
    List<ExamDefinition> publishedsNotAnswered = examDefinitionDAO.findExamDefinitionsNotSubmittedByCourseId(idCurso);
    System.out.println("SERVICE (Aluno): DAO retornou " + (publishedsNotAnswered != null ? publishedsNotAnswered.size() : "null") + " provas (supostamente publicadas).");

    return publishedsNotAnswered;
  }

  public List<ExamDefinition> getExamDefinitionsForCourseManagement(int idCurso, int idProfessorLogado)
      throws SQLException, IllegalAccessException, IllegalArgumentException {
    return examDefinitionDAO.findExamDefinitionsByCourseId(idCurso, false);
  }

  public boolean publishExamDefinition(int idExamDefinition, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {
    System.out.println("SERVICE: Entrando em publishExamDefinition para ExamDef ID: " + idExamDefinition + " por Professor ID: " + idProfessorLogado); // LOG S1
    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      System.err.println("SERVICE: ExamDef ID " + idExamDefinition + " NÃO ENCONTRADO no DAO."); // LOG S2
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }
    System.out.println("SERVICE: ExamDef ID " + idExamDefinition + " encontrado. Status atual de publicação: " + examDef.isPublished()); // LOG S3 (use seu getter isPublished/isPublicada)
    Course curso = courseDAO.searchCourseById(examDef.getIdCourse());
    if (curso == null) {
      System.err.println("SERVICE: Curso associado (ID: " + examDef.getIdCourse() + ") à ExamDef ID " + idExamDefinition + " NÃO ENCONTRADO."); // LOG S4
      throw new IllegalArgumentException("Curso associado (ID: "+ examDef.getIdCourse() +") à definição de exame não encontrado.");
    }
    if (curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      System.err.println("SERVICE: Professor ID " + idProfessorLogado + " NÃO AUTORIZADO para publicar ExamDef ID " + idExamDefinition + " do curso ID " + curso.getIdCourse()); // LOG S5
      throw new IllegalAccessException("Professor não autorizado a publicar este exame.");
    }

    if (examDef.isPublished()) {
      System.out.println("SERVICE: Exame ID " + idExamDefinition + " já está publicado.");
      return true;
    }

    examDef.setPublished(true);
    examDef.setUpdateData(LocalDateTime.now());
    System.out.println("SERVICE: Objeto ExamDef (ID: " + idExamDefinition + ") modificado para published=true. Chamando DAO para atualizar..."); // LOG S7
    boolean atualizadoPeloDAO = examDefinitionDAO.updateExamDefinition(examDef);
    System.out.println("SERVICE: DAO.updateExamDefinition retornou: " + atualizadoPeloDAO + " para ExamDef ID: " + idExamDefinition); // LOG S8
    return atualizadoPeloDAO;
  }

  public boolean unpublishExamDefinition(int idExamDefinition, int idProfessorLogado)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    ExamDefinition examDef = examDefinitionDAO.findExamDefinitionById(idExamDefinition);
    if (examDef == null) {
      throw new IllegalArgumentException("Definição de exame com ID " + idExamDefinition + " não encontrada.");
    }

    Course curso = courseDAO.searchCourseById(examDef.getIdCourse());
    if (curso == null) {
      throw new IllegalArgumentException("Curso associado (ID: "+ examDef.getIdCourse() +") à definição de exame não encontrado.");
    }
    if (curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != idProfessorLogado) {
      throw new IllegalAccessException("Professor não autorizado a despublicar este exame.");
    }

    if (!examDef.isPublished()) {
      System.out.println("SERVICE: Exame ID " + idExamDefinition + " já está despublicado/não publicado.");
      return true;
    }

    examDef.setPublished(false);
    examDef.setUpdateData(LocalDateTime.now());
    System.out.println("SERVICE: Despublicando exame ID " + idExamDefinition);
    return examDefinitionDAO.updateExamDefinition(examDef);
  }
}