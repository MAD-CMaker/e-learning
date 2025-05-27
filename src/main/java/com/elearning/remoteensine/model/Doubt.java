package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.DoubtStatus;
import java.time.LocalDateTime;

public class Doubt {
  private int doubtId;
  private int courseId;
  private int studentId;
  private String title;
  private String description;
  private LocalDateTime creationHour;
  private String answer;
  private  String studentName;
  private String professorName;
  private LocalDateTime answerHour;
  private Integer professorId;
  private DoubtStatus status;


  public Doubt() {
    this.creationHour = LocalDateTime.now();
    this.status = DoubtStatus.ABERTA;
  }

  public Doubt(int courseId, int studentId, String title, String description, String studentName, String professorName) {
    this();
    this.courseId = courseId;
    this.studentId = studentId;
    this.title = title;
    this.description = description;
    this.status = DoubtStatus.ABERTA;
    this.studentName = studentName;
    this.professorName = professorName;
  }
  public Doubt(int courseId, int studentId, String title, String description, String studentName) {
    this();
    this.courseId = courseId;
    this.studentId = studentId;
    this.title = title;
    this.description = description;
    this.studentName = studentName;
    // professorName, answer, answerHour, professorId ficam null/default por enquanto
  }


  public int getDoubtId() {
    return doubtId;
  }

  public void setDoubtId(int doubtId) {
    this.doubtId = doubtId;
  }

  public DoubtStatus getStatus() {
    return status;
  }

  public void setStatus(DoubtStatus status) {
    this.status = status;
  }

  public Integer getProfessorId() {
    return professorId;
  }

  public void setProfessorId(Integer professorId) {
    this.professorId = professorId;
  }

  public LocalDateTime getAnswerHour() {
    return answerHour;
  }

  public void setAnswerHour(LocalDateTime answerHour) {
    this.answerHour = answerHour;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public LocalDateTime getCreationHour() {
    return creationHour;
  }

  public void setCreationHour(LocalDateTime creationHour) {
    this.creationHour = creationHour;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public void answerDoubt(String answer, Professor professor){
    if (professor != null) {
      this.setAnswer(answer);
      this.setProfessorId(professor.getIdUser());
      this.setAnswerHour(LocalDateTime.now());
      this.setStatus(DoubtStatus.RESPONDIDA);
    }
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public String getProfessorName() {
    return professorName;
  }

  public void setProfessorName(String professorName) {
    this.professorName = professorName;
  }

  @Override
  public String toString() {
    return "Duvida{" +
        "idDuvida=" + doubtId +
        ", idCurso=" + courseId +
        ", idStudent=" + status +
        ", titulo='" + title + '\'' +
        ", status=" + status +
        '}';
  }

}
