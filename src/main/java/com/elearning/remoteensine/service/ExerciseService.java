package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ClassroomDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.ExerciseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exercise;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.ExerciseType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ExerciseService {

  private ExerciseDAO exerciseDAO;
  private ClassroomDAO classroomDAO;
  private CourseDAO courseDAO;
  private UserDAO userDAO;


  public ExerciseService() {
    this.exerciseDAO = new ExerciseDAO();
    this.classroomDAO = new ClassroomDAO();
    this.courseDAO = new CourseDAO();
    this.userDAO = new UserDAO();
  }

  /**
   * Adiciona um novo exercício a uma aula.
   *
   * @param idClassroom ID da aula à qual o exercício pertence.
   * @param idProfessorLogged ID do professor para verificação de permissão.
   * @param statement Enunciado do exercício.
   * @param type Tipo do exercício.
   * @param options String contendo opções (ex: JSON para múltipla escolha), pode ser null.
   * @param correctAnswer Gabarito ou resposta esperada.
   * @return O exercício criado.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos ou aula/curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável pelo curso da aula.
   */
  public Exercise addClassExercise(int idClassroom, int idProfessorLogged, String statement,
                                           ExerciseType type, String options, String correctAnswer)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    if (statement == null || statement.trim().isEmpty()) {
      throw new IllegalArgumentException("Exercise statement cannot be empty.");
    }
    if (idClassroom <= 0) {
      throw new IllegalArgumentException("Invalid class ID.");
    }

    Classroom classroom = classroomDAO.searchById(idClassroom);
    if (classroom == null) {
      throw new IllegalArgumentException("Class ID " + idClassroom + " not found.");
    }

    Course classCourse = courseDAO.searchCourseById(classroom.getCourseId());
    if (classCourse == null) {
      throw new IllegalArgumentException("Course associated with the class (ID: " + classroom.getCourseId() + ") não encontrado.");
    }

    if (classCourse.getResponsibleProfessor() == null || classCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Professor não autorizado a adicionar exercícios a esta aula.");
    }

    Exercise newExercise = new Exercise(idClassroom, statement, type, options, correctAnswer);
    return exerciseDAO.saveExercise(newExercise);
  }

  /**
   * Busca um exercício pelo seu ID.
   *
   * @param idExercise ID do exercício.
   * @return O exercício encontrado, ou null.
   * @throws SQLException Erro de banco.
   */
  public Exercise searchExercise(int idExercise) throws SQLException {
    return exerciseDAO.searchExerciseById(idExercise);
  }

  /**
   * Lista todos os exercícios de uma aula específica.
   *
   * @param idClassroom ID da aula.
   * @return Lista de exercícios.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se a aula não for encontrada.
   */
  public List<Exercise> listClassExercise(int idClassroom) throws SQLException, IllegalArgumentException {
    Classroom classroom = classroomDAO.searchById(idClassroom);
    if (classroom == null) {
      throw new IllegalArgumentException("Class ID " + idClassroom + " not found.");
    }
    return exerciseDAO.listExercisesByClass(idClassroom);
  }

  /**
   * Atualiza as informações de um exercício.
   *
   * @param idExercise ID do exercício a ser atualizado.
   * @param idProfessorLogged ID do professor para verificação de permissão.
   * @param newStatement Novo enunciado.
   * @param newType Novo tipo.
   * @param newOptions Novas opções.
   * @param newCorrectAnswer Nova resposta correta.
   * @return true se atualizado, false caso contrário.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se dados inválidos ou exercício/aula/curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável.
   */
  public boolean updateExerciseInfos(int idExercise, int idProfessorLogged, String newStatement,
                                               ExerciseType newType, String newOptions, String newCorrectAnswer)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Exercise existingExercise = exerciseDAO.searchExerciseById(idExercise);
    if (existingExercise == null) {
      throw new IllegalArgumentException("Exercise ID " + idExercise + " not found.");
    }

    Classroom exerciseClass = classroomDAO.searchById(existingExercise.getClassroomId());
    if (exerciseClass == null) {
      throw new IllegalArgumentException("Class associated with exercise (ID: " + existingExercise.getClassroomId() + ") not found.");
    }

    Course classCourse = courseDAO.searchCourseById(exerciseClass.getCourseId());
    if (classCourse == null) {
      throw new IllegalArgumentException("Course associated with class (ID: " + exerciseClass.getCourseId() + ") not found.");
    }

    if (classCourse.getResponsibleProfessor() == null || classCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to modify this exercise.");
    }

    existingExercise.setStatement(newStatement);
    existingExercise.setExerciseType(newType);
    existingExercise.setOption(newOptions);
    existingExercise.setCorrectAnswer(newCorrectAnswer);

    return exerciseDAO.updateExercise(existingExercise);
  }

  /**
   * Remove um exercício.
   *
   * @param idExercise ID do exercício a ser removido.
   * @param idProfessorLogged ID do professor para verificação de permissão.
   * @return true se removido, false caso contrário.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se exercício/aula/curso não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável.
   */
  public boolean removeExercise(int idExercise, int idProfessorLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Exercise existingExercise = exerciseDAO.searchExerciseById(idExercise);
    if (existingExercise == null) {
      throw new IllegalArgumentException("Exercise ID " + idExercise + " not found.");
    }

    Classroom exerciseClass = classroomDAO.searchById(existingExercise.getClassroomId());
    if (exerciseClass == null) {
      throw new IllegalArgumentException("Class associated with exercise (ID: " + existingExercise.getClassroomId() + ") not found.");
    }

    Course classCourse = courseDAO.searchCourseById(exerciseClass.getCourseId());
    if (classCourse == null) {
      throw new IllegalArgumentException("Course associated with class (ID: " + exerciseClass.getCourseId() + ") not found.");
    }

    if (classCourse.getResponsibleProfessor() == null || classCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to modify this exercise.");
    }

    return exerciseDAO.deleteExercise(idExercise);
  }
}

