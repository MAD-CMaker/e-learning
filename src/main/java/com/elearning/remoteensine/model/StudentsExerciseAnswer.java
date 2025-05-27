package com.elearning.remoteensine.model;
import java.time.LocalDateTime;

public class StudentsExerciseAnswer {
  private int idStudentAnswerExercise;
  private int idExercise;
  private int idStudent;
  private int idClass;
  private int idCourse;

  private String AnswerText;
  private LocalDateTime sendDate;
  private Boolean isCorrect;
  private Double grade;
  private String feedbackProfessor;

  public StudentsExerciseAnswer() {
    this.sendDate = LocalDateTime.now();
  }

  public StudentsExerciseAnswer(int idExercise, int idStudent, int idClass, String AnswerText) {
    this();
    this.idExercise = idExercise;
    this.idStudent = idStudent;
    this.idClass = idClass;
    this.AnswerText = AnswerText;
  }

  public int getIdExercise() {
    return idExercise;
  }

  public void setIdExercise(int idExercise) {
    this.idExercise = idExercise;
  }

  public int getIdStudent() {
    return idStudent;
  }

  public void setIdStudent(int idStudent) {
    this.idStudent = idStudent;
  }

  public int getIdStudentAnswerExercise() {
    return idStudentAnswerExercise;
  }

  public void setIdStudentAnswerExercise(int idStudentAnswerExercise) {
    this.idStudentAnswerExercise = idStudentAnswerExercise;
  }

  public int getIdClass() {
    return idClass;
  }

  public void setIdClass(int idClass) {
    this.idClass = idClass;
  }

  public int getIdCourse() {
    return idCourse;
  }

  public void setIdCourse(int idCourse) {
    this.idCourse = idCourse;
  }

  public String getAnswerText() {
    return AnswerText;
  }

  public void setAnswerText(String answerText) {
    AnswerText = answerText;
  }

  public LocalDateTime getSendDate() {
    return sendDate;
  }

  public void setSendDate(LocalDateTime sendDate) {
    this.sendDate = sendDate;
  }

  public Boolean getCorrect() {
    return isCorrect;
  }

  public void setCorrect(Boolean correct) {
    isCorrect = correct;
  }

  public Double getGrade() {
    return grade;
  }

  public void setGrade(Double grade) {
    this.grade = grade;
  }

  public String getFeedbackProfessor() {
    return feedbackProfessor;
  }

  public void setFeedbackProfessor(String feedbackProfessor) {
    this.feedbackProfessor = feedbackProfessor;
  }

  @Override
  public String toString() {
    return "RespostaExercicioAluno{" +
        "idRespostaExercicioAluno=" + idStudentAnswerExercise +
        ", idExercicio=" + idExercise +
        ", idStudent=" + idStudent +
        ", isCorreta=" + isCorrect +
        '}';
  }
}