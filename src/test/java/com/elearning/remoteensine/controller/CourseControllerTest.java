package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.*;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @Mock
    private CourseService courseService;
    
    @Mock
    private EnrollService enrollService;
    
    @Mock
    private CommentService commentService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private ClassroomService classroomService;
    
    @Mock
    private NewsletterService newsletterService;
    
    @Mock
    private DoubtService doubtService;
    
    @Mock
    private ExerciseService exerciseService;
    
    @Mock
    private ExamDefinitionService examDefinitionService;
    
    @Mock
    private StudentsExerciseAnswerService studentsAnswerService;
    
    @Mock
    private ExamQuestionService examQuestionService;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private Model model;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    private CourseController courseController;
    private User studentUser;
    private User professorUser;
    private Course testCourse;
    private List<Course> courseList;
    
    @BeforeEach
    void setUp() {
        courseController = new CourseController(
            courseService, userService, enrollService, commentService,
            classroomService, newsletterService, doubtService, exerciseService,
            examDefinitionService, studentsAnswerService, examQuestionService
        );
        
        // Setup student user
        studentUser = new User("Student Name", "student@example.com", "password", UserType.STUDENT);
        studentUser.setIdUser(1);
        
        // Setup professor user
        professorUser = new Professor("Professor Name", "professor@example.com", "password", "Computer Science");
        professorUser.setIdUser(2);
        
        // Setup test course
        testCourse = new Course("Java Programming", "Learn Java Programming", (Professor) professorUser, 99.99, "Programming", 40);
        testCourse.setIdCourse(1);
        
        // Setup course list
        courseList = new ArrayList<>();
        courseList.add(testCourse);
    }

    @Test
    void exibirDetalhesCurso_shouldReturnDetalhesCursoView() throws Exception {
        // Arrange
        int courseId = 1;
        List<Comment> comments = new ArrayList<>();
        List<Doubt> doubts = new ArrayList<>();
        List<Classroom> classrooms = new ArrayList<>();
        
        when(courseService.searchCourseByIdComplete(courseId)).thenReturn(testCourse);
        when(commentService.listStudentsCommentsInCourse(courseId)).thenReturn(comments);
        when(doubtService.listCourseDoubts(courseId)).thenReturn(doubts);
        when(classroomService.listCoursesClasses(courseId)).thenReturn(classrooms);
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(enrollService.checkEnrolledStudents(studentUser.getIdUser(), courseId)).thenReturn(true);
        
        // Act
        String viewName = courseController.exibirDetalhesCurso(courseId, model, session);
        
        // Assert
        assertEquals("detalhes-curso", viewName);
        verify(model).addAttribute("curso", testCourse);
        verify(model).addAttribute("comentarios", comments);
        verify(model).addAttribute("duvidas", doubts);
        verify(model).addAttribute("aulasDoCurso", classrooms);
        verify(model).addAttribute("isAlunoMatriculado", true);
    }
    
    @Test
    void salvarNovoCurso_withValidData_shouldRedirectToCursos() throws SQLException {
        // Arrange
        Course newCourse = new Course("New Course", "Course Description", (Professor) professorUser, 99.99, "Category", 40);
        newCourse.setPresentationVideo("https://www.youtube.com/watch?v=abcdefg");
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        when(courseService.createCourse(
            eq(newCourse.getTitle()),
            eq(newCourse.getDescription()),
            eq(professorUser.getIdUser()),
            eq(newCourse.getPrice()),
            eq(newCourse.getCategory()),
            eq(newCourse.getHoursLoad()),
            anyString()
        )).thenReturn(newCourse);
        
        // Act
        String viewName = courseController.salvarNovoCurso(newCourse, session, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/cursos", viewName);
        verify(courseService).createCourse(
            eq(newCourse.getTitle()),
            eq(newCourse.getDescription()),
            eq(professorUser.getIdUser()),
            eq(newCourse.getPrice()),
            eq(newCourse.getCategory()),
            eq(newCourse.getHoursLoad()),
            anyString()
        );
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_curso"), anyString());
    }
    
    @Test
    void salvarNovoCurso_withEmptyTitle_shouldReturnFormCursoView() throws SQLException {
        // Arrange
        Course invalidCourse = new Course("", "Course Description", (Professor) professorUser, 99.99, "Category", 40);
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        
        // Act
        String viewName = courseController.salvarNovoCurso(invalidCourse, session, redirectAttributes, model);
        
        // Assert
        assertEquals("form-curso", viewName);
        verify(model).addAttribute(eq("erro_titulo"), anyString());
        verify(model).addAttribute("professorLogado", professorUser);
        verifyNoInteractions(courseService);
    }
    
    @Test
    void adicionarComentario_shouldRedirectToDetalhesCurso() throws SQLException {
        // Arrange
        int courseId = 1;
        String commentText = "This is a test comment";
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);

        Comment commentCreated = new Comment(courseId, studentUser.getIdUser(), commentText, studentUser.getName());

        when(commentService.createComment(courseId, studentUser.getIdUser(), commentText)).thenReturn(commentCreated);

        // Act
        String viewName = courseController.adicionarComentario(courseId, commentText, session, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(commentService).createComment(courseId, studentUser.getIdUser(), commentText);
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_comentario"), anyString());
    }
    
    @Test
    void matricularNoCurso_shouldRedirectToDetalhesCurso() throws SQLException {
        // Arrange
        int courseId = 1;
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(enrollService.makeEnroll(studentUser.getIdUser(), courseId)).thenReturn(true);
        
        // Act
        String viewName = courseController.matricularNoCurso(courseId, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(enrollService).makeEnroll(studentUser.getIdUser(), courseId);
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_matricula"), anyString());
    }
    
    @Test
    void exibirFormularioNovoCurso_withoutAuthentication_shouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(null);
        
        // Act
        String viewName = courseController.exibirFormularioNovoCurso(model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_login"), anyString());
    }
    
    @Test
    void exibirFormularioNovoCurso_withStudentUser_shouldRedirectToCursos() {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        
        // Act
        String viewName = courseController.exibirFormularioNovoCurso(model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/cursos", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_permissao"), anyString());
    }
}