package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.*;
import com.elearning.remoteensine.model.*;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.model.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentsExerciseAnswerServiceTest {

    @Mock
    private StudentsExerciseAnswerDAO answerDAO;
    
    @Mock
    private ExerciseDAO exerciseDAO;
    
    @Mock
    private UserDAO userDAO;
    
    @Mock
    private ClassroomDAO classroomDAO;
    
    @Mock
    private CourseDAO courseDAO;
    
    @Mock
    private EnrollDAO enrollDAO;
    
    private StudentsExerciseAnswerService answerService;
    private User student;
    private Course course;
    private Classroom classroom;
    private Exercise exercise;
    private Exercise multipleChoiceExercise;
    
    @BeforeEach
    void setUp() {
        answerService = new StudentsExerciseAnswerService(
            answerDAO, exerciseDAO, userDAO, classroomDAO, courseDAO, enrollDAO
        );
        
        // Setup student
        student = new User("John Student", "john@example.com", "password", UserType.STUDENT);
        student.setIdUser(1);
        
        // Setup course
        course = new Course();
        course.setIdCourse(1);
        course.setTitle("Java Programming");
        
        // Setup classroom
        classroom = new Classroom();
        classroom.setClassroomId(1);
        classroom.setCourseId(1);
        classroom.setTitle("Introduction to Java");
        
        // Setup exercise (essay type)
        exercise = new Exercise();
        exercise.setExerciseId(1);
        exercise.setClassroomId(1);
        exercise.setStatement("Explain polymorphism in Java");
        exercise.setExerciseType(ExerciseType.DISSERTATIVA);
        
        // Setup multiple choice exercise
        multipleChoiceExercise = new Exercise();
        multipleChoiceExercise.setExerciseId(2);
        multipleChoiceExercise.setClassroomId(1);
        multipleChoiceExercise.setStatement("Which keyword is used for inheritance in Java?");
        multipleChoiceExercise.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        multipleChoiceExercise.setOption("A) extends\nB) implements\nC) inherits\nD) super");
        multipleChoiceExercise.setCorrectAnswer("A) extends");
    }
    
    @Test
    void submitValidAnswerForMultipleChoiceExercise() throws SQLException, IllegalArgumentException {
        // Arrange
        int exerciseId = 2;
        int studentId = 1;
        int classroomId = 1;
        int courseId = 1;
        String studentAnswer = "A) extends";
        
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.searchById(classroomId)).thenReturn(classroom);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(true);
        when(exerciseDAO.searchExerciseById(exerciseId)).thenReturn(multipleChoiceExercise);
        when(answerDAO.getAnswerByStudentAndExercise(studentId, exerciseId)).thenReturn(null);
        
        StudentsExerciseAnswer expectedAnswer = new StudentsExerciseAnswer(exerciseId, studentId, classroomId, studentAnswer);
        expectedAnswer.setIdCourse(courseId);
        expectedAnswer.setIdStudentAnswerExercise(1);
        expectedAnswer.setCorrect(true);
        expectedAnswer.setGrade(10.0);
        
        when(answerDAO.saveAnswer(any(StudentsExerciseAnswer.class))).thenReturn(expectedAnswer);
        
        // Act
        StudentsExerciseAnswer result = answerService.submitOrUpdateAnswer(
            exerciseId, studentId, classroomId, courseId, studentAnswer
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.getIdExercise());
        assertEquals(studentId, result.getIdStudent());
        assertEquals(classroomId, result.getIdClass());
        assertEquals(courseId, result.getIdCourse());
        assertEquals(studentAnswer, result.getAnswerText());
        assertTrue(result.getCorrect());
        assertEquals(10.0, result.getGrade());
        
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).searchById(classroomId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(exerciseDAO).searchExerciseById(exerciseId);
        verify(answerDAO).getAnswerByStudentAndExercise(studentId, exerciseId);
        verify(answerDAO).saveAnswer(any(StudentsExerciseAnswer.class));
    }
    
    @Test
    void updateExistingAnswer() throws SQLException, IllegalArgumentException {
        // Arrange
        int exerciseId = 1;
        int studentId = 1;
        int classroomId = 1;
        int courseId = 1;
        String newAnswer = "Polymorphism is the ability of an object to take many forms.";
        
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.searchById(classroomId)).thenReturn(classroom);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(true);
        when(exerciseDAO.searchExerciseById(exerciseId)).thenReturn(exercise);
        
        StudentsExerciseAnswer existingAnswer = new StudentsExerciseAnswer(exerciseId, studentId, classroomId, "Old answer");
        existingAnswer.setIdStudentAnswerExercise(1);
        existingAnswer.setIdCourse(courseId);
        existingAnswer.setSendDate(LocalDateTime.now().minusDays(1));
        
        when(answerDAO.getAnswerByStudentAndExercise(studentId, exerciseId)).thenReturn(existingAnswer);
        when(answerDAO.updateAnswer(any(StudentsExerciseAnswer.class))).thenReturn(true);
        
        // Act
        StudentsExerciseAnswer result = answerService.submitOrUpdateAnswer(
            exerciseId, studentId, classroomId, courseId, newAnswer
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.getIdExercise());
        assertEquals(studentId, result.getIdStudent());
        assertEquals(newAnswer, result.getAnswerText());
        assertNotNull(result.getSendDate());
        
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).searchById(classroomId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(exerciseDAO).searchExerciseById(exerciseId);
        verify(answerDAO).getAnswerByStudentAndExercise(studentId, exerciseId);
        verify(answerDAO).updateAnswer(any(StudentsExerciseAnswer.class));
    }
    
    @Test
    void getStudentAnswersForClassroom() throws SQLException {
        // Arrange
        int studentId = 1;
        int classroomId = 1;
        
        List<StudentsExerciseAnswer> expectedAnswers = new ArrayList<>();
        
        StudentsExerciseAnswer answer1 = new StudentsExerciseAnswer(1, studentId, classroomId, "Answer 1");
        answer1.setIdStudentAnswerExercise(1);
        
        StudentsExerciseAnswer answer2 = new StudentsExerciseAnswer(2, studentId, classroomId, "Answer 2");
        answer2.setIdStudentAnswerExercise(2);
        
        expectedAnswers.add(answer1);
        expectedAnswers.add(answer2);
        
        when(answerDAO.getAnswersByStudentAndClassroom(studentId, classroomId)).thenReturn(expectedAnswers);
        
        // Act
        List<StudentsExerciseAnswer> result = answerService.getStudentAnswersForClassroom(studentId, classroomId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Answer 1", result.get(0).getAnswerText());
        assertEquals("Answer 2", result.get(1).getAnswerText());
        
        verify(answerDAO).getAnswersByStudentAndClassroom(studentId, classroomId);
    }
    
    @Test
    void getStudentAnswerForExercise() throws SQLException {
        // Arrange
        int studentId = 1;
        int exerciseId = 1;
        
        StudentsExerciseAnswer expectedAnswer = new StudentsExerciseAnswer(exerciseId, studentId, 1, "My answer");
        expectedAnswer.setIdStudentAnswerExercise(1);
        
        when(answerDAO.getAnswerByStudentAndExercise(studentId, exerciseId)).thenReturn(expectedAnswer);
        
        // Act
        StudentsExerciseAnswer result = answerService.getStudentAnswerForExercise(studentId, exerciseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.getIdExercise());
        assertEquals(studentId, result.getIdStudent());
        assertEquals("My answer", result.getAnswerText());
        
        verify(answerDAO).getAnswerByStudentAndExercise(studentId, exerciseId);
    }
    
    @Test
    void submitWithInvalidStudentId() throws SQLException {
        // Arrange
        int exerciseId = 1;
        int invalidStudentId = 999;
        int classroomId = 1;
        int courseId = 1;
        String answer = "My answer";
        
        when(userDAO.searchById(invalidStudentId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            answerService.submitOrUpdateAnswer(exerciseId, invalidStudentId, classroomId, courseId, answer);
        });
        
        assertEquals("Aluno com ID " + invalidStudentId + " não encontrado ou não é um estudante válido.", exception.getMessage());
        
        verify(userDAO).searchById(invalidStudentId);
        verifyNoInteractions(exerciseDAO);
        verifyNoInteractions(answerDAO);
    }
    
    @Test
    void submitWithNonEnrolledStudent() throws SQLException {
        // Arrange
        int exerciseId = 1;
        int studentId = 1;
        int classroomId = 1;
        int courseId = 1;
        String answer = "My answer";
        
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.searchById(classroomId)).thenReturn(classroom);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            answerService.submitOrUpdateAnswer(exerciseId, studentId, classroomId, courseId, answer);
        });
        
        assertEquals("Aluno não está matriculado no curso para responder exercícios.", exception.getMessage());
        
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).searchById(classroomId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verifyNoInteractions(exerciseDAO);
        verifyNoInteractions(answerDAO);
    }
}