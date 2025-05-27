package com.elearning.remoteensine.model;

import com.elearning.remoteensine.model.enums.UserType;
import java.time.LocalDateTime;

public class User {
  private int idUser;
  private String name;
  private String email;
  private String password;
  private UserType userType;
  private  LocalDateTime registerDate;


  public User(){

  }
  public User(String name, String email, String password, UserType userType){
    this.name = name;
    this.email = email;
    this.password = password;
    this.userType = userType;
    this.registerDate = LocalDateTime.now();
  }

  public int getIdUser() {
    return idUser;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public UserType getUserType() {
    return userType;
  }

  public LocalDateTime getRegisterDate() {
    return registerDate;
  }

  public void setIdUser(int idUser) {
    this.idUser = idUser;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public void setRegisterDate(LocalDateTime registerDate) {
    this.registerDate = registerDate;
  }


}
