package com.elearning.remoteensine.model;

import java.util.ArrayList;
import java.util.List;

public class Classroom {
  private int classroomId;
  private int courseId;
  private String title;
  private String description;
  private String contentURL;
  private int sequence;
  private List<Exercise> exercises;

  public Classroom() {
    this.exercises = new ArrayList<>();
  }

  public Classroom(int courseId, String title, String description, String contentURL, int sequence) {
    this.courseId = courseId;
    this.title = title;
    this.description = description;
    this.contentURL = contentURL;
    this.sequence = sequence;
    this.exercises = new ArrayList<>();
  }

  public int getClassroomId() {
    return classroomId;
  }

  public void setClassroomId(int classroomId) {
    this.classroomId = classroomId;
  }

  public List<Exercise> getExercises() {
    return exercises;
  }

  public void setExercises(List<Exercise> exercises) {
    this.exercises = exercises;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public String getContentURL() {
    return contentURL;
  }

  public void setContentURL(String contentURL) {
    this.contentURL = contentURL;
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

  public int getCourseId() {
    return courseId;
  }

  public void setCourseId(int courseId) {
    this.courseId = courseId;
  }

  public void addExercise(Exercise exercise) {
    if (exercise != null) {
      this.exercises.add(exercise);
    }
  }
  @Override
  public String toString() {
    return "Aula{" +
        "idAula=" + classroomId +
        ", idCurso=" + courseId +
        ", titulo='" + title + '\'' +
        ", ordem=" + sequence +
        '}';
  }
}