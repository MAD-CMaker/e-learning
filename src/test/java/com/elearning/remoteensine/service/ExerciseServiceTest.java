package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ClassroomDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.ExerciseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exercise;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.enums.ExerciseType;
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
public class ExerciseServiceTest {

    @Mock
    private ExerciseDAO exerciseDAO;

    @Mock
    private ClassroomDAO classroomDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private UserDAO userDAO;

    private ExerciseService exerciseService;
    private Professor professor;
    private Course course;
    private Classroom classroom;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        exerciseService = new ExerciseService(exerciseDAO, classroomDAO, courseDAO, userDAO);
        
        // Setup professor
        professor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        professor.setIdUser(1);
        
        // Setup course
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup classroom
        classroom = new Classroom(1, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        classroom.setClassroomId(1);
        
        // Setup exercise
        exercise = new Exercise(1, "What is Java?", ExerciseType.MULTIPLA_ESCOLHA, "{\"options\":[\"A\",\"B\",\"C\",\"D\"]}", "A");
        exercise.setExerciseId(1);
    }

    @Test
    void addClassExercise_shouldAddValidExerciseToClass() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int classroomId = 1;
        int professorId = 1;
        String statement = "What is Java?";
        ExerciseType type = ExerciseType.MULTIPLA_ESCOLHA;
        String options = "{\"options\":[\"A\",\"B\",\"C\",\"D\"]}";
        String correctAnswer = "A";
        
        when(classroomDAO.searchById(classroomId)).thenReturn(classroom);
        when(courseDAO.searchCourseById(classroom.getCourseId())).thenReturn(course);
        
        Exercise expectedExercise = new Exercise(classroomId, statement, type, options, correctAnswer);
        expectedExercise.setExerciseId(1);
        when(exerciseDAO.saveExercise(any(Exercise.class))).thenReturn(expectedExercise);
        
        // Act
        Exercise result = exerciseService.addClassExercise(classroomId, professorId, statement, type, options, correctAnswer);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getExerciseId());
        assertEquals(classroomId, result.getClassroomId());
        assertEquals(statement, result.getStatement());
        assertEquals(type, result.getExerciseType());
        assertEquals(options, result.getOptions());
        assertEquals(correctAnswer, result.getCorrectAnswer());
        
        verify(classroomDAO).searchById(classroomId);
        verify(courseDAO).searchCourseById(classroom.getCourseId());
        verify(exerciseDAO).saveExercise(any(Exercise.class));
    }

    @Test
    void searchExercise_shouldReturnExerciseWhenFound() throws SQLException {
        // Arrange
        int exerciseId = 1;
        when(exerciseDAO.searchExerciseById(exerciseId)).thenReturn(exercise);
        
        // Act
        Exercise result = exerciseService.searchExercise(exerciseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.getExerciseId());
        assertEquals("What is Java?", result.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, result.getExerciseType());
        assertEquals("{\"options\":[\"A\",\"B\",\"C\",\"D\"]}", result.getOptions());
        assertEquals("A", result.getCorrectAnswer());
        
        verify(exerciseDAO).searchExerciseById(exerciseId);
    }

    @Test
    void listClassExercise_shouldReturnListOfExercises() throws SQLException, IllegalArgumentException {
        // Arrange
        int classroomId = 1;
        List<Exercise> expectedExercises = new ArrayList<>();
        
        Exercise exercise1 = new Exercise(classroomId, "What is Java?", ExerciseType.MULTIPLA_ESCOLHA, 
                "{\"options\":[\"A\",\"B\",\"C\",\"D\"]}", "A");
        exercise1.setExerciseId(1);
        
        Exercise exercise2 = new Exercise(classroomId, "Explain OOP concepts", ExerciseType.DISSERTATIVA, 
                null, "OOP stands for Object-Oriented Programming...");
        exercise2.setExerciseId(2);
        
        expectedExercises.add(exercise1);
        expectedExercises.add(exercise2);
        
        when(classroomDAO.searchById(classroomId)).thenReturn(classroom);
        when(exerciseDAO.listExercisesByClass(classroomId)).thenReturn(expectedExercises);
        
        // Act
        List<Exercise> result = exerciseService.listClassExercise(classroomId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("What is Java?", result.get(0).getStatement());
        assertEquals("Explain OOP concepts", result.get(1).getStatement());
        
        verify(classroomDAO).searchById(classroomId);
        verify(exerciseDAO).listExercisesByClass(classroomId);
    }

    @Test
    void updateExerciseInfos_shouldUpdateExerciseInformation() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int exerciseId = 1;
        int professorId = 1;
        String newStatement = "Updated Java question";
        ExerciseType newType = ExerciseType.MULTIPLA_ESCOLHA;
        String newOptions = "{\"options\":[\"A\",\"B\",\"C\",\"D\",\"E\"]}";
        String newCorrectAnswer = "B";
        
        when(exerciseDAO.searchExerciseById(exerciseId)).thenReturn(exercise);
        when(classroomDAO.searchById(exercise.getClassroomId())).thenReturn(classroom);
        when(courseDAO.searchCourseById(classroom.getCourseId())).thenReturn(course);
        when(exerciseDAO.updateExercise(any(Exercise.class))).thenReturn(true);
        
        // Act
        boolean result = exerciseService.updateExerciseInfos(exerciseId, professorId, newStatement, newType, newOptions, newCorrectAnswer);
        
        // Assert
        assertTrue(result);
        
        verify(exerciseDAO).searchExerciseById(exerciseId);
        verify(classroomDAO).searchById(exercise.getClassroomId());
        verify(courseDAO).searchCourseById(classroom.getCourseId());
        verify(exerciseDAO).updateExercise(any(Exercise.class));
    }

    @Test
    void removeExercise_shouldDeleteExercise() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int exerciseId = 1;
        int professorId = 1;
        
        when(exerciseDAO.searchExerciseById(exerciseId)).thenReturn(exercise);
        when(classroomDAO.searchById(exercise.getClassroomId())).thenReturn(classroom);
        when(courseDAO.searchCourseById(classroom.getCourseId())).thenReturn(course);
        when(exerciseDAO.deleteExercise(exerciseId)).thenReturn(true);
        
        // Act
        boolean result = exerciseService.removeExercise(exerciseId, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(exerciseDAO).searchExerciseById(exerciseId);
        verify(classroomDAO).searchById(exercise.getClassroomId());
        verify(courseDAO).searchCourseById(classroom.getCourseId());
        verify(exerciseDAO).deleteExercise(exerciseId);
    }

    @Test
    void addClassExercise_shouldThrowExceptionForEmptyStatement() {
        // Arrange
        int classroomId = 1;
        int professorId = 1;
        String statement = "";  // Empty statement
        ExerciseType type = ExerciseType.MULTIPLA_ESCOLHA;
        String options = "{\"options\":[\"A\",\"B\",\"C\",\"D\"]}";
        String correctAnswer = "A";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.addClassExercise(classroomId, professorId, statement, type, options, correctAnswer);
        });
        
        assertEquals("Exercise statement cannot be empty.", exception.getMessage());
        
        // Verify no interactions with DAOs
        verifyNoInteractions(exerciseDAO);
    }
}