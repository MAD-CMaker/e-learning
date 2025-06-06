package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.ExamDefinitionDAO;
import com.elearning.remoteensine.dao.ExamQuestionDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.model.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamQuestionServiceTest {

    @Mock
    private ExamQuestionDAO examQuestionDAO;

    @Mock
    private ExamDefinitionDAO examDefinitionDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private UserDAO userDAO;

    private ExamQuestionService examQuestionService;
    private User professor;
    private Course course;
    private ExamDefinition examDefinition;
    private ExamQuestion examQuestion;

    @BeforeEach
    void setUp() {
        examQuestionService = new ExamQuestionService(examQuestionDAO, examDefinitionDAO, courseDAO, userDAO);
        
        // Setup professor
        professor = new User("John Doe", "john@example.com", "password", UserType.PROFESSOR);
        professor.setIdUser(1);
        
        // Setup course with professor
        Professor responsibleProfessor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        responsibleProfessor.setIdUser(1);
        course = new Course("Java Programming", "Learn Java", responsibleProfessor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup exam definition
        examDefinition = new ExamDefinition(1, "Java Exam", "Test your Java knowledge");
        examDefinition.setIdDefinitionExam(1);
        
        // Setup exam question
        examQuestion = new ExamQuestion(
            1, // idDefinitionExam
            "What is Java?", // statement
            ExerciseType.MULTIPLA_ESCOLHA, // exerciseType
            "[{\"id\":\"A\",\"text\":\"A programming language\"},{\"id\":\"B\",\"text\":\"A coffee type\"}]", // options
            "A", // correctAnswer
            10.0, // grade
            1 // examSequence
        );
        examQuestion.setIdExamQuestion(1);
    }

    @Test
    void addQuestionToExamDefinition_shouldAddValidQuestionToExam() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idProfessorLogado = 1;
        String enunciado = "What is Java?";
        ExerciseType tipoQuestao = ExerciseType.MULTIPLA_ESCOLHA;
        String opcoes = "[{\"id\":\"A\",\"text\":\"A programming language\"},{\"id\":\"B\",\"text\":\"A coffee type\"}]";
        String respostaCorreta = "A";
        double pontuacao = 10.0;
        int ordemNaProva = 1;
        
        when(userDAO.searchById(idProfessorLogado)).thenReturn(professor);
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examQuestionDAO.saveQuestion(any(ExamQuestion.class))).thenReturn(examQuestion);
        
        // Act
        ExamQuestion result = examQuestionService.addQuestionToExamDefinition(
            idExamDefinition, idProfessorLogado, enunciado, tipoQuestao, 
            opcoes, respostaCorreta, pontuacao, ordemNaProva
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getIdExamQuestion());
        assertEquals(idExamDefinition, result.getIdDefinitionExam());
        assertEquals(enunciado, result.getStatement());
        assertEquals(tipoQuestao, result.getExerciseType());
        assertEquals(opcoes, result.getOptions());
        assertEquals(respostaCorreta, result.getCorrectAnswer());
        assertEquals(pontuacao, result.getGrade());
        assertEquals(ordemNaProva, result.getExamSequence());
        
        verify(userDAO).searchById(idProfessorLogado);
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examQuestionDAO).saveQuestion(any(ExamQuestion.class));
    }

    @Test
    void getQuestionsForExamDefinition_shouldReturnListOfQuestions() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idProfessorLogado = 1;
        List<ExamQuestion> expectedQuestions = new ArrayList<>();
        expectedQuestions.add(examQuestion);
        
        when(userDAO.searchById(idProfessorLogado)).thenReturn(professor);
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examQuestionDAO.findQuestionsByExamDefinitionId(idExamDefinition)).thenReturn(expectedQuestions);
        
        // Act
        List<ExamQuestion> result = examQuestionService.getQuestionsForExamDefinition(idExamDefinition, idProfessorLogado);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("What is Java?", result.get(0).getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, result.get(0).getExerciseType());
        
        verify(userDAO).searchById(idProfessorLogado);
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examQuestionDAO).findQuestionsByExamDefinitionId(idExamDefinition);
    }

    @Test
    void getQuestionById_shouldReturnQuestionWhenFound() throws SQLException, IllegalAccessException {
        // Arrange
        int idQuestaoExame = 1;
        int idProfessorLogado = 1;
        
        when(examQuestionDAO.findQuestionById(idQuestaoExame)).thenReturn(examQuestion);
        when(examDefinitionDAO.findExamDefinitionById(examQuestion.getIdDefinitionExam())).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        
        // Act
        ExamQuestion result = examQuestionService.getQuestionById(idQuestaoExame, idProfessorLogado);
        
        // Assert
        assertNotNull(result);
        assertEquals(idQuestaoExame, result.getIdExamQuestion());
        assertEquals("What is Java?", result.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, result.getExerciseType());
        
        verify(examQuestionDAO).findQuestionById(idQuestaoExame);
        verify(examDefinitionDAO).findExamDefinitionById(examQuestion.getIdDefinitionExam());
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
    }

    @Test
    void updateExamQuestion_shouldUpdateQuestionSuccessfully() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idProfessorLogado = 1;
        ExamQuestion questionToUpdate = examQuestion;
        questionToUpdate.setStatement("Updated question statement");
        
        when(examDefinitionDAO.findExamDefinitionById(questionToUpdate.getIdDefinitionExam())).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examQuestionDAO.updateQuestion(questionToUpdate)).thenReturn(true);
        
        // Act
        boolean result = examQuestionService.updateExamQuestion(questionToUpdate, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        
        verify(examDefinitionDAO).findExamDefinitionById(questionToUpdate.getIdDefinitionExam());
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examQuestionDAO).updateQuestion(questionToUpdate);
    }

    @Test
    void deleteExamQuestion_shouldDeleteQuestionSuccessfully() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idQuestaoExame = 1;
        int idProfessorLogado = 1;
        
        when(examQuestionDAO.findQuestionById(idQuestaoExame)).thenReturn(examQuestion);
        when(examDefinitionDAO.findExamDefinitionById(examQuestion.getIdDefinitionExam())).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examQuestionDAO.deleteQuestion(idQuestaoExame)).thenReturn(true);
        
        // Act
        boolean result = examQuestionService.deleteExamQuestion(idQuestaoExame, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        
        verify(examQuestionDAO).findQuestionById(idQuestaoExame);
        verify(examDefinitionDAO).findExamDefinitionById(examQuestion.getIdDefinitionExam());
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examQuestionDAO).deleteQuestion(idQuestaoExame);
    }

    @Test
    void getQuestionsForStudentToTakeExam_shouldReturnQuestionsForStudent() throws SQLException, IllegalArgumentException {
        // Arrange
        int idExamDefinition = 1;
        List<ExamQuestion> expectedQuestions = new ArrayList<>();
        expectedQuestions.add(examQuestion);
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(examQuestionDAO.findQuestionsByExamDefinitionId(idExamDefinition)).thenReturn(expectedQuestions);
        
        // Act
        List<ExamQuestion> result = examQuestionService.getQuestionsForStudentToTakeExam(idExamDefinition);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("What is Java?", result.get(0).getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, result.get(0).getExerciseType());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(examQuestionDAO).findQuestionsByExamDefinitionId(idExamDefinition);
    }

}