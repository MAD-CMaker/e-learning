package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.UserType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends User{
  private List<Course> enrolledCourses;
  private Map<Integer, Double> courseProgress;

  public Student(){
    super();
    this.setUserType(UserType.STUDENT);
    this.enrolledCourses = new ArrayList<>();
    this.courseProgress = new HashMap<>();
  }

  public Student(String name, String email, String password){
    super(name, email, password, UserType.STUDENT);
    this.enrolledCourses = new ArrayList<>();
    this.courseProgress = new HashMap<>();
  }

  public Map<Integer, Double> getCourseProgress() {
    return courseProgress;
  }

  public void setCourseProgress(Map<Integer, Double> courseProgress) {
    this.courseProgress = courseProgress;
  }

  public List<Course> getEnrolledCourses() {
    return enrolledCourses;
  }

  public void setEnrolledCourses(List<Course> enrolledCourses) {
    this.enrolledCourses = enrolledCourses;
  }

  public void enrollCourse(Course course){
    if (course != null && !this.enrolledCourses.contains(course)){
      this.enrolledCourses.add(course);
      this.courseProgress.put(course.getIdCourse(), 0.0);
      System.out.println("Student" + getName() + "enrolled in the course: " + course.getTitle());
    }
  }

  public void attProgress(Course course, double progress){
    if (courseProgress.containsKey(course.getIdCourse())) {
      if (progress >= 0.0 && progress <= 1.0) {
        this.courseProgress.put(course.getIdCourse(), progress);
      } else {
        System.out.println("Invalid progress. Must be between 0.0 and 1.0 ");
      }
    }
  }
}
