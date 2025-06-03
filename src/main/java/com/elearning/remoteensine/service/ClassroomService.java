package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ClassroomDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.User;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ClassroomService {

  private ClassroomDAO classroomDAO;
  private CourseDAO courseDAO;
  private UserDAO userDAO;

  public ClassroomService(ClassroomDAO classroomDAO, CourseDAO courseDAO, UserDAO userDAO) {
    this.classroomDAO = classroomDAO;
    this.courseDAO = courseDAO;
    this.userDAO = userDAO;
  }

  /**
   * Adiciona uma nova aula a um curso.
   *
   * @param idCourse ID do curso ao qual a aula pertence.
   * @param idProfessorLogged ID do professor que está tentando adicionar a aula (para verificação de permissão).
   * @param title Título da aula.
   * @param description Descrição da aula.
   * @param contentURL URL do conteúdo da aula.
   * @param sequence Ordem da aula no curso.
   * @return A aula criada.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos ou curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável pelo curso.
   */
  public Classroom addClassToCourse(int idCourse, int idProfessorLogged, String title, String description,
                                   String contentURL, int sequence)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Class title cannot be empty.");
    }
    if (idCourse <= 0) {
      throw new IllegalArgumentException("invalid course ID.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }

    if (course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      User loggedProfessor = userDAO.searchById(idProfessorLogged);
      if (loggedProfessor == null || course.getResponsibleProfessor().getIdUser() != loggedProfessor.getIdUser()){
        throw new IllegalAccessException("Teacher not authorized to add lessons to this course.");
      }
    }

    Classroom newClass = new Classroom(idCourse, title, description, contentURL, sequence);
    return classroomDAO.saveClassroom(newClass);
  }

  /**
   * Busca uma aula pelo seu ID.
   *
   * @param idClassroom ID da aula.
   * @return A aula encontrada, ou null.
   * @throws SQLException Erro de banco.
   */
  public Classroom searchClass(int idClassroom) throws SQLException {
    return classroomDAO.searchById(idClassroom);
  }

  /**
   * Lista todas as aulas de um curso específico.
   *
   * @param idCourse ID do curso.
   * @return Lista de aulas.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public List<Classroom> listCoursesClasses(int idCourse) throws SQLException, IllegalArgumentException {
    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }
    return classroomDAO.listClassesByCourse(idCourse);
  }

  /**
   * Atualiza as informações de uma aula.
   *
   * @param idClassroom ID da aula a ser atualizada.
   * @param idProfessorLogged ID do professor para verificação de permissão.
   * @param newTitle Novo título.
   * @param newDescription Nova descrição.
   * @param newContentURL Nova URL de conteúdo.
   * @param newSequence Nova ordem.
   * @return true se atualizada, false caso contrário.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se dados inválidos ou aula/curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável pelo curso da aula.
   */
  public boolean updateClassesInfo(int idClassroom, int idProfessorLogged, String newTitle,
                                          String newDescription, String newContentURL, int newSequence)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Classroom existingClass = classroomDAO.searchById(idClassroom);
    if (existingClass == null) {
      throw new IllegalArgumentException("Class ID " + idClassroom + " not found.");
    }

    Course classCourse = courseDAO.searchCourseById(existingClass.getCourseId());
    if (classCourse == null) {
      throw new IllegalArgumentException("Course associated with the class (ID: " + existingClass.getCourseId() + ") not found.");
    }

    if (classCourse.getResponsibleProfessor() == null || classCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to modify this lesson\n.");
    }

    existingClass.setTitle(newTitle);
    existingClass.setDescription(newDescription);
    existingClass.setContentURL(newContentURL);
    existingClass.setSequence(newSequence);

    return classroomDAO.upddateClass(existingClass);
  }

  /**
   * Remove uma aula.
   *
   * @param idClassroom ID da aula a ser removida.
   * @param idProfessorLogged ID do professor para verificação de permissão.
   * @return true se removida, false caso contrário.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se aula/curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável pelo curso da aula.
   */
  public boolean removeClass(int idClassroom, int idProfessorLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Classroom existingClass = classroomDAO.searchById(idClassroom);
    if (existingClass == null) {
      throw new IllegalArgumentException("Class ID " + idClassroom + " not found.");
    }

    Course classCourse = courseDAO.searchCourseById(existingClass.getCourseId());
    if (classCourse == null) {
      throw new IllegalArgumentException("Course associated with the class (ID: " + existingClass.getCourseId() + ") not found.");
    }

    if (classCourse.getResponsibleProfessor() == null || classCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to remove this class.");
    }

    return classroomDAO.deleteClass(idClassroom);
  }

  public Classroom saveOrUpdateClass(Classroom classroom, int idCourse, int idProfessorLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }
    if (course.getResponsibleProfessor() == null || course.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Professor não autorizado a gerenciar aulas para este curso.");
    }

    classroom.setCourseId(idCourse);

    if (classroom.getClassroomId() > 0) {
      System.out.println("SERVICE: Atualizando aula ID " + classroom.getClassroomId());
      boolean atualizado = classroomDAO.upddateClass(classroom);
      if (!atualizado) throw new SQLException("Falha ao atualizar aula ID " + classroom.getClassroomId());
      return classroom;
    } else {
      System.out.println("SERVICE: Salvando nova aula para curso ID " + idCourse);
      return classroomDAO.saveClassroom(classroom);
    }
  }
}

