package com.elearning.remoteensine.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamDefinition {
  private int idDefinitionExam;
  private int idCourse;
  private String title;
  private String description;
  private LocalDateTime creationData;
  private LocalDateTime updateData;
  private boolean published;
  private List<ExamQuestion> questions;


  public ExamDefinition() {
    this.creationData = LocalDateTime.now();
    this.updateData = LocalDateTime.now();
    this.questions = new ArrayList<>();
  }

  public ExamDefinition(int idCourse, String title, String description) {
    this();
    this.idCourse = idCourse;
    this.title = title;
    this.description = description;
  }

  public int getIdDefinitionExam() {
    return idDefinitionExam;
  }

  public void setIdDefinitionExam(int idDefinitionExam) {
    this.idDefinitionExam = idDefinitionExam;
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

  public LocalDateTime getCreationData() {
    return creationData;
  }

  public void setCreationData(LocalDateTime creationData) {
    this.creationData = creationData;
  }

  public LocalDateTime getUpdateData() {
    return updateData;
  }

  public void setUpdateData(LocalDateTime updateData) {
    this.updateData = updateData;
  }

  public boolean isPublished() {
    return published;
  }
  public void setPublished(boolean published) {
    this.published = published;
  }

  public List<ExamQuestion> getQuestions() {
    return questions;
  }
  public void setQuestions(List<ExamQuestion> questions) {
    this.questions = questions;
  }

  @Override
  public String toString() {
    return "DefinicaoProva{" +
        "idDefinicaoProva=" + idDefinitionExam +
        ", idCurso=" + idCourse +
        ", titulo='" + title + '\'' +
        ", dataCriacao=" + creationData +
        '}';
  }
}