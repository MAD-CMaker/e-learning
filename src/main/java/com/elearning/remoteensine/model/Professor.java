package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.UserType;
import java.util.ArrayList;
import java.util.List;

public class Professor extends User{
  private List<Course> createdCourses;
  private String specialization;

  public Professor() {
    super();
    this.setUserType(UserType.PROFESSOR);
    this.createdCourses = new ArrayList<>();
  }

  public Professor(String name, String email, String password, String specialization) {
    super(name, email, password, UserType.PROFESSOR);
    this.createdCourses = new ArrayList<>();
    this.specialization = specialization;
  }

  public List<Course> getCreatedCourses() {
    return createdCourses;
  }

  public void setCreatedCourses(List<Course> createdCourses) {
    this.createdCourses = createdCourses;
  }

  public String getSpecialization() {
    return specialization;
  }

  public void setSpecialization(String specialization) {
    this.specialization = specialization;
  }

  public void createCourse(String title, String description, double price, String category, int hoursLoad) {
    Course newCourse = new Course(title, description, this, price, category, hoursLoad);
    this.createdCourses.add(newCourse);
    System.out.println("'Professor'" + getName() + "'created the course'" + newCourse.getTitle());
  }

  public void addClassroomToCourse(Course course, Classroom classroom){
    if (this.createdCourses.contains(course) && classroom != null){
      course.addClassroom(classroom);
      System.out.println("'Classroom'" + classroom.getTitle() + "'added to course'" + course.getTitle());
    } else {
      System.out.println("\n" +
          "Unable to add class. Check if the course belongs to this teacher.");
    }
  }
}
