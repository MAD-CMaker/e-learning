package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class Enroll {
  private Student student;
  private Course course;
  private LocalDateTime enrollDate;
  private double progress;

  public Enroll() {
  }

  public Enroll(Student student, Course course, LocalDateTime enrollDate, double progress) {
    this.student = student;
    this.course = course;
    this.enrollDate = enrollDate;
    this.progress = progress;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public double getProgress() {
    return progress;
  }
  public void setProgress(double progress) {
    if (progress >= 0.0 && progress <= 1.0) {
      this.progress = progress;
    } else {
      System.err.println("Invalid progress: " + progress + ". Must be between 0.0 and 1.0.");
      this.progress = Math.max(0.0, Math.min(1.0, progress));
    }
  }

  public LocalDateTime getEnrollDate() {
    return enrollDate;
  }

  public void setEnrollDate(LocalDateTime enrollDate) {
    this.enrollDate = enrollDate;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  @Override
  public String toString() {
    return "Matricula{" +
        "student=" + (student != null ? student.getName() : "N/A") +
        ", curso=" + (course != null ? course.getTitle() : "N/A") +
        ", dataMatricula=" + enrollDate +
        ", progresso=" + progress +
        '}';
  }
}
