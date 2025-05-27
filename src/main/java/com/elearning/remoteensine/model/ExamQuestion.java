package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.ExerciseType;

public class ExamQuestion {
  private int idExamQuestion;
  private int idDefinitionExam;
  private String statement;
  private ExerciseType exerciseType;
  private String options;
  private String correctAnswer;
  private double grade;
  private int examSequence;

  public ExamQuestion() {
  }

  public ExamQuestion(int idDefinitionExam, String statement, ExerciseType exerciseType, String options, String correctAnswer, double grade, int examSequence) {
    this.idDefinitionExam = idDefinitionExam;
    this.statement = statement;
    this.exerciseType = exerciseType;
    this.options = options;
    this.correctAnswer = correctAnswer;
    this.grade = grade;
    this.examSequence = examSequence;
  }

  public int getIdExamQuestion() {
    return idExamQuestion;
  }

  public void setIdExamQuestion(int idExamQuestion) {
    this.idExamQuestion = idExamQuestion;
  }

  public int getIdDefinitionExam() {
    return idDefinitionExam;
  }

  public void setIdDefinitionExam(int idDefinitionExam) {
    this.idDefinitionExam = idDefinitionExam;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public ExerciseType getExerciseType() {
    return exerciseType;
  }

  public void setExerciseType(ExerciseType exerciseType) {
    this.exerciseType = exerciseType;
  }

  public String getCorrectAnswer() {
    return correctAnswer;
  }

  public void setCorrectAnswer(String correctAnswer) {
    this.correctAnswer = correctAnswer;
  }

  public int getExamSequence() {
    return examSequence;
  }

  public void setExamSequence(int examSequence) {
    this.examSequence = examSequence;
  }

  public double getGrade() {
    return grade;
  }

  public void setGrade(double grade) {
    this.grade = grade;
  }

  @Override
  public String toString() {
    return "QuestaoExame{" +
        "idQuestaoExame=" + idExamQuestion +
        ", idDefinitionExam=" + idDefinitionExam +
        ", enunciado='" + (statement != null ? statement.substring(0, Math.min(statement.length(), 30)) + "..." : "") + '\'' +
        ", tipoQuestao=" + exerciseType +
        ", pontuacao=" + grade +
        '}';
  }
}