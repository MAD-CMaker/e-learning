package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ClassroomDAO; // Ou AulaDAO
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.dao.ExerciseDAO;
import com.elearning.remoteensine.dao.StudentsExerciseAnswerDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exercise;
import com.elearning.remoteensine.model.StudentsExerciseAnswer;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.model.enums.UserType;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class StudentsExerciseAnswerService {

  private final StudentsExerciseAnswerDAO answerDAO;
  private final ExerciseDAO exerciseDAO;
  private final UserDAO userDAO;
  private final ClassroomDAO classroomDAO;
  private final CourseDAO courseDAO;
  private final EnrollDAO enrollDAO;


  public StudentsExerciseAnswerService(StudentsExerciseAnswerDAO answerDAO, ExerciseDAO exerciseDAO, UserDAO userDAO, ClassroomDAO classroomDAO, CourseDAO courseDAO, EnrollDAO enrollDAO) {
    this.answerDAO = answerDAO;
    this.exerciseDAO = exerciseDAO;
    this.userDAO = userDAO;
    this.classroomDAO = classroomDAO;
    this.courseDAO = courseDAO;
    this.enrollDAO = enrollDAO;
  }

  /**
   * Permite que um aluno submeta ou atualize sua resposta para um exercício.
   *
   * @param exerciseId    ID do exercício.
   * @param studentId     ID do aluno.
   * @param classroomId   ID da aula à qual o exercício pertence.
   * @param courseId      ID do curso ao qual a aula pertence.
   * @param studentAnswerText A resposta textual do aluno.
   * @return A resposta salva ou atualizada.
   * @throws SQLException             Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos, entidades não encontradas, ou permissões violadas.
   */
  public StudentsExerciseAnswer submitOrUpdateAnswer(int exerciseId, int studentId, int classroomId, int courseId, String studentAnswerText)
      throws SQLException, IllegalArgumentException {

    User student = userDAO.searchById(studentId);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Aluno com ID " + studentId + " não encontrado ou não é um estudante válido.");
    }

    Course course = courseDAO.searchCourseById(courseId);
    if (course == null) {
      throw new IllegalArgumentException("Curso com ID " + courseId + " não encontrado.");
    }
    Classroom classroom = classroomDAO.searchById(classroomId);
    if (classroom == null || classroom.getCourseId() != courseId) {
      throw new IllegalArgumentException("Aula com ID " + classroomId + " não encontrada ou não pertence ao curso especificado.");
    }

    if (!enrollDAO.checkEnroll(studentId, courseId)) {
      throw new IllegalArgumentException("Aluno não está matriculado no curso para responder exercícios.");
    }

    Exercise exercise = exerciseDAO.searchExerciseById(exerciseId);
    if (exercise == null || exercise.getClassroomId() != classroomId) {
      throw new IllegalArgumentException("Exercício com ID " + exerciseId + " não encontrado ou não pertence à aula especificada.");
    }

    if (studentAnswerText == null || studentAnswerText.trim().isEmpty()) {
      throw new IllegalArgumentException("A resposta do exercício não pode ser vazia.");
    }
    StudentsExerciseAnswer existingAnswer = answerDAO.getAnswerByStudentAndExercise(studentId, exerciseId);
    Boolean isCorrect = null;
    Double grade = null;

    if (exercise.getExerciseType() == ExerciseType.MULTIPLA_ESCOLHA) {
      isCorrect = Objects.equals(studentAnswerText.trim(), exercise.getCorrectAnswer() != null ? exercise.getCorrectAnswer().trim() : null);
      if (Boolean.TRUE.equals(isCorrect)) {
        grade = 10.0;
      } else {
        grade = 0.0;
      }
    }
    if (existingAnswer != null) {
      System.out.println("SERVICE: Atualizando resposta existente para exercício ID " + exerciseId + " do aluno ID " + studentId);
      existingAnswer.setAnswerText(studentAnswerText);
      existingAnswer.setSendDate(LocalDateTime.now());
      existingAnswer.setCorrect(isCorrect);
      existingAnswer.setGrade(grade);
      answerDAO.updateAnswer(existingAnswer);
      return existingAnswer;
    } else {
      System.out.println("SERVICE: Salvando nova resposta para exercício ID " + exerciseId + " do aluno ID " + studentId);
      StudentsExerciseAnswer newAnswer = new StudentsExerciseAnswer(exerciseId, studentId, classroomId, studentAnswerText);
      newAnswer.setIdCourse(courseId);
      newAnswer.setCorrect(isCorrect);
      newAnswer.setGrade(grade);
      return answerDAO.saveAnswer(newAnswer);
    }
  }

  /**
   * Busca todas as respostas de um aluno para os exercícios de uma aula específica.
   *
   * @param studentId ID do aluno.
   * @param classroomId ID da aula.
   * @return Lista de respostas.
   * @throws SQLException Se ocorrer erro no banco.
   */
  public List<StudentsExerciseAnswer> getStudentAnswersForClassroom(int studentId, int classroomId) throws SQLException {
    return answerDAO.getAnswersByStudentAndClassroom(studentId, classroomId);
  }

  /**
   * Busca a resposta de um aluno para um exercício específico.
   * @param studentId ID do aluno.
   * @param exerciseId ID do exercício.
   * @return A resposta, ou null se não encontrada.
   * @throws SQLException Se ocorrer erro no banco.
   */
  public StudentsExerciseAnswer getStudentAnswerForExercise(int studentId, int exerciseId) throws SQLException {
    return answerDAO.getAnswerByStudentAndExercise(studentId, exerciseId);
  }
}