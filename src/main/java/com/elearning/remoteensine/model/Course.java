package com.elearning.remoteensine.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Course {
  private int idCourse;
  private String title;
  private String description;
  private Professor responsibleProfessor;
  private double price;
  private String presentationVideo;
  private String category;
  private int hoursLoad;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private List<Classroom> classes;
  private List<Comment> comments;
  private List<Doubt> doubts;
  private List<Exam> exams;

  public Course(){
    this.classes = new ArrayList<>();
    this.comments = new ArrayList<>();
    this.doubts = new ArrayList<>();
    this.exams = new ArrayList<>();
  }

  public Course(String title, String description, Professor responsibleProfessor, double price, String category, int hoursLoad){
    this.title = title;
    this.description = description;
    this.responsibleProfessor = responsibleProfessor;
    this.price = price;
    this.category = category;
    this.hoursLoad = hoursLoad;
    this.creationDate = LocalDateTime.now();
    this.updateDate = LocalDateTime.now();
    this.classes = new ArrayList<>();
    this.comments = new ArrayList<>();
    this.doubts = new ArrayList<>();
    this.exams = new ArrayList<>();
  }

  public int getIdCourse() {
    return idCourse;
  }

  public void setIdCourse(int idCourse) {
    this.idCourse = idCourse;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Professor getResponsibleProfessor() {
    return responsibleProfessor;
  }

  public void setResponsibleProfessor(Professor responsibleProfessor) {
    this.responsibleProfessor = responsibleProfessor;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public String getPresentationVideo() {
    return presentationVideo;
  }

  public void setPresentationVideo(String presentationVideo) {
    this.presentationVideo = presentationVideo;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getHoursLoad() {
    return hoursLoad;
  }

  public void setHoursLoad(int hoursLoad) {
    this.hoursLoad = hoursLoad;
  }

  public List<Classroom> getClasses() {
    return classes;
  }

  public void setClasses(List<Classroom> classes) {
    this.classes = classes;
  }

  public void addClassroom(Classroom classroom) {
    if(classroom != null) {
      this.classes.add(classroom);
      classroom.setCourseId(this.idCourse);
    }
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  public void addComments(Comment comment){
    if(comment != null){
      this.comments.add(comment);
    }
  }

  public List<Doubt> getDoubts() {
    return doubts;
  }

  public void setDoubts(List<Doubt> doubts) {
    this.doubts = doubts;
  }

  public void addDoubt(Doubt doubt){
    if(doubt != null){
      this.doubts.add(doubt);
    }
  }

  public List<Exam> getExams() {
    return exams;
  }

  public void setExams(List<Exam> exams) {
    this.exams = exams;
  }

  public void addExam(Exam exam) {
    if (exam != null) {
      this.exams.add(exam);
    }
  }

  public double getMediaExam(){
    if(exams.isEmpty()){
      return 0.0;
    }
    double sum = 0;
    for (Exam med : exams){
      sum += med.getGrade();
    }
    return sum / exams.size();
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(LocalDateTime updateDate) {
    this.updateDate = updateDate;
  }
}
