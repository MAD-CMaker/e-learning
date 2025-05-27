package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class Exam {
  private int examId;
  private int courseId;
  private int idExamDefinition;
  private int studentId;
  private int grade;
  private String comment;
  private LocalDateTime hourDate;
  private String studentName;

  public Exam() {
    this.hourDate = LocalDateTime.now();
  }

  public Exam(int courseId, int studentId, int grade, String comment) {
    this();
    this.courseId = courseId;
    this.studentId = studentId;
    this.idExamDefinition = idExamDefinition;
    this.setGrade(grade);
    this.comment = comment;
    this.hourDate = LocalDateTime.now();
  }

  public int getExamId() {
    return examId;
  }

  public void setExamId(int examId) {
    this.examId = examId;
  }

  public LocalDateTime getHourDate() {
    return hourDate;
  }

  public void setHourDate(LocalDateTime hourDate) {
    this.hourDate = hourDate;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getGrade() {
    return grade;
  }

  public void setGrade(int grade)  {
    if(grade >= 0 && grade <= 10){
      this.grade = grade;
    } else {
        throw new IllegalArgumentException("Grade must be between 0 and 10");
    }
  }

  public int getStudentId() {
    return studentId;
  }

  public void setStudentId(int studentId) {
    this.studentId = studentId;
  }

  public int getCourseId() {
    return courseId;
  }

  public void setCourseId(int courseId) {
    this.courseId = courseId;
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public int getIdExamDefinition() {
    return idExamDefinition;
  }

  public void setIdExamDefinition(int idExamDefinition) {
    this.idExamDefinition = idExamDefinition;
  }

  @Override
  public String toString() {
    return "Avaliacao{" +
        "idAvaliacao=" + examId +
        ", idCurso=" + courseId +
        ", idStudent=" + studentId +
        ", nota=" + grade +
        ", comentario='" + (comment != null ? comment.substring(0, Math.min(comment.length(), 30)) + "..." : "N/A") + '\'' +
        ", dataHora=" + hourDate +
        '}';
  }
}
