package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {
  private CourseDAO courseDAO;
  private UserDAO userDAO;

  public CourseService() {
    this.courseDAO = new CourseDAO();
    this.userDAO = new UserDAO();
  }

  /**
   * Cria um novo curso.
   *
   * @param title                  Título do curso.
   * @param description            Descrição do curso.
   * @param responsibleProfessorId ID do professor que está criando o curso.
   * @param price                  Preço do curso.
   * @param category               Categoria do curso.
   * @param loadHour               Carga horária em horas.
   * @param presentationVideoURL   URL do vídeo de apresentação (opcional).
   * @return O curso criado.
   * @throws SQLException             Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se os dados forem inválidos ou o professor não for encontrado/habilitado.
   */
  public Course createCourse(String title, String description, int responsibleProfessorId, double price,
                             String category, int loadHour, String presentationVideoURL)
      throws SQLException, IllegalArgumentException {

    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("\n" +
          "Course title cannot be empty.");
    }
    if (responsibleProfessorId <= 0) {
      throw new IllegalArgumentException("\n" +
          "Invalid professor ID.");
    }

    User userProfessor = userDAO.searchById(responsibleProfessorId);
    if (userProfessor == null || !(userProfessor instanceof Professor)) {
      throw new IllegalArgumentException("Professor com ID " + responsibleProfessorId + " não encontrado ou não é um professor.");
    }
    Professor professorResponsibleObj = (Professor) userProfessor;

    Course newCourse = new Course();
    newCourse.setTitle(title);
    newCourse.setDescription(description);
    newCourse.setResponsibleProfessor(professorResponsibleObj);
    newCourse.setPrice(price);
    newCourse.setCategory(category);
    newCourse.setHoursLoad(loadHour);
    newCourse.setPresentationVideo(presentationVideoURL);
    newCourse.setCreationDate(LocalDateTime.now());
    return courseDAO.saveCourse(newCourse);
  }
  /**
   * Busca um curso pelo ID e popula completamente o objeto Professor responsável.
   *
   * @param idCourse ID do curso.
   * @return O curso encontrado, ou null.
   * @throws SQLException Erro de banco.
   */
  public Course searchCourseByIdComplete(int idCourse) throws SQLException {
    Course course = courseDAO.searchCourseById(idCourse);
    if (course != null && course.getResponsibleProfessor() != null && course.getResponsibleProfessor().getIdUser() > 0) {
      User professorUser = userDAO.searchById(course.getResponsibleProfessor().getIdUser());
      if (professorUser instanceof Professor) {
        course.setResponsibleProfessor((Professor) professorUser);
      }
    }
    return course;
  }
  /**
   * Lista todos os cursos disponíveis, populando o professor responsável de cada um.
   *
   * @return Lista de cursos.
   * @throws SQLException Erro de banco.
   */
  public List<Course> listAllCoursesWithProfessors() throws SQLException {
    List<Course> courses = courseDAO.listAllCourses();
    if (courses != null) {
      for (Course course : courses) {
        if (course.getResponsibleProfessor() != null && course.getResponsibleProfessor().getIdUser() > 0) {
          int professorId = course.getResponsibleProfessor().getIdUser();
          User userProfessor = userDAO.searchById(professorId);
          if (userProfessor != null && userProfessor.getUserType() == UserType.PROFESSOR) {
            course.setResponsibleProfessor((Professor) userProfessor);
          }
        }
      }
    }
    System.out.println("SERVICE: Saindo de listAllCoursesWithProfessors. Tamanho final da lista de cursos a ser retornada: " + (courses != null ? courses.size() : "null"));
    return courses;
  }
  /**
   * Lista todos os cursos criados por um professor específico.
   *
   * @param idProfessor O ID do professor.
   * @return Uma lista de Cursos.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Course> listCoursesOfProfessor(int idProfessor) throws SQLException {
    User userProfessor = userDAO.searchById(idProfessor);
    if (userProfessor == null || userProfessor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Professor with ID " + idProfessor + "not found or not a teacher.");
    }

    List<Course> courses = courseDAO.listCoursesByProfessor(idProfessor);
    for (Course course : courses) {
      course.setResponsibleProfessor((Professor) userProfessor);
    }
    return courses;
  }

  /**
   * Atualiza as informações de um curso.
   *
   * @param idCourse             ID do curso a ser atualizado.
   * @param title                Novo título.
   * @param description          Nova descrição.
   * @param price                Novo preço.
   * @param category             Nova categoria.
   * @param loadHour             Nova carga horária.
   * @param presentationVideoURL Novo vídeo.
   * @param idProfessorLogged    ID do professor que está tentando a ação (para verificação de permissão).
   * @return true se atualizado, false caso contrário.
   * @throws SQLException             Erro de banco.
   * @throws IllegalArgumentException Se dados inválidos ou permissão negada.
   * @throws IllegalAccessException   Se o professor logado não for o dono do curso.
   */
  public boolean attCourseInfo(int idCourse, String title, String description, double price,
                               String category, int loadHour, String presentationVideoURL,
                               int idProfessorLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Course existingCourse = courseDAO.searchCourseById(idCourse);
    if (existingCourse == null) {
      throw new IllegalArgumentException("Course with ID " + idCourse + " not found.");
    }


    if (existingCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to modify this course.");
    }

    existingCourse.setTitle(title);
    existingCourse.setDescription(description);
    existingCourse.setPrice(price);
    existingCourse.setCategory(category);
    existingCourse.setHoursLoad(loadHour);
    existingCourse.setPresentationVideo(presentationVideoURL);

    return courseDAO.attCourses(existingCourse);
  }

  /**
   * Remove um curso.
   *
   * @param idCourse          ID do curso a ser removido.
   * @param idProfessorLogged ID do professor que está tentando a ação (para verificação de permissão).
   * @return true se removido, false caso contrário.
   * @throws SQLException             Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   * @throws IllegalAccessException   Se o professor logado não for o dono do curso.
   */
  public boolean removeCourse(int idCourse, int idProfessorLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Course existingCourse = courseDAO.searchCourseById(idCourse);
    if (existingCourse == null) {
      throw new IllegalArgumentException("Course with ID " + idCourse + " not found.");
    }
    if (existingCourse.getResponsibleProfessor().getIdUser() != idProfessorLogged) {
      throw new IllegalAccessException("Teacher not authorized to modify this course.");
    }

    return courseDAO.deleteCourse(idCourse);
  }

}
