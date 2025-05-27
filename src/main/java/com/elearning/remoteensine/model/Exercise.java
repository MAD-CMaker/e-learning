package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.ExerciseType;
import java.util.ArrayList;
import java.util.List;

public class Exercise {
  private int exerciseId;
  private int classroomId;
  private String statement;
  private ExerciseType exerciseType;
  private String options;
  private String correctAnswer;

  public Exercise() {
  }

  public Exercise(int classroomId, String statement, ExerciseType exerciseType, String options ,String correctAnswer) {
    this.classroomId = classroomId;
    this.statement = statement;
    this.exerciseType = exerciseType;
    this.options = options;
    this.correctAnswer = correctAnswer;
  }


  public int getExerciseId() {
    return exerciseId;
  }

  public void setExerciseId(int exerciseId) {
    this.exerciseId = exerciseId;
  }

  public String getCorrectAnswer() {
    return correctAnswer;
  }

  public void setCorrectAnswer(String correctAnswer) {
    this.correctAnswer = correctAnswer;
  }

  public boolean checkAnswer(String studentAnswer){
    if (this.correctAnswer != null){
      return this.correctAnswer.equalsIgnoreCase(studentAnswer);
    }
    return false;
  }

  public String getOptions() {
    return options;
  }

  public void setOption(String options) {
    this.options = options;
  }


  public ExerciseType getExerciseType() {
    return exerciseType;
  }

  public void setExerciseType(ExerciseType exerciseType) {
    this.exerciseType = exerciseType;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

  public int getClassroomId() {
    return classroomId;
  }

  public void setClassroomId(int classroomId) {
    this.classroomId = classroomId;
  }

  @Override
  public String toString() {
    return "Exercicio{" +
        "idExercicio=" + exerciseId +
        ", idAula=" + classroomId +
        ", enunciado='" + statement.substring(0, Math.min(statement.length(), 30)) + "..." + '\'' +
        ", tipo=" + exerciseType +
        '}';
  }

}
