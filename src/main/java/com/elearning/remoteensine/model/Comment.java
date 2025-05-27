package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class Comment {
  private int commentId;
  private int courseId;
  private int studentId;
  private String text;
  private LocalDateTime hourDate;
  private String studentName;

  public Comment() {
    this.hourDate = LocalDateTime.now();
  }

  public Comment(int courseId, int studentId, String text, String studentName) {
    this();
    this.courseId = courseId;
    this.studentId = studentId;
    this.text = text;
    this.studentName = studentName;
    this.hourDate = LocalDateTime.now();
  }

  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(int commentId) {
    this.commentId = commentId;
  }

  public LocalDateTime getHourDate() {
    return hourDate;
  }

  public void setHourDate(LocalDateTime hourDate) {
    this.hourDate = hourDate;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
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

  @Override
  public String toString() {
    return "Comentario{" +
        "studentName=" + studentName +
        "idComentario=" + commentId +
        ", idCurso=" + courseId +
        ", idStudent=" + studentId +
        ", texto='" + (text != null ? text.substring(0, Math.min(text.length(), 30)) + "..." : "N/A") + '\'' +
        ", dataHora=" + hourDate +
        '}';
  }
}
