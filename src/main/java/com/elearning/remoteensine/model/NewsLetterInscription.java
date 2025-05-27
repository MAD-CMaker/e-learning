package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class NewsLetterInscription {
  private int inscriptionId;
  private String email;
  private LocalDateTime inscriptionDate;
  private boolean active;

  public NewsLetterInscription() {
    this.inscriptionDate = LocalDateTime.now();
    this.active = true;
  }

  public NewsLetterInscription(String email) {
    this();
    this.email = email;
  }

  public int getInscriptionId() {
    return inscriptionId;
  }

  public void setInscriptionId(int inscriptionId) {
    this.inscriptionId = inscriptionId;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDateTime getInscriptionDate() {
    return inscriptionDate;
  }

  public void setInscriptionDate(LocalDateTime inscriptionHour) {
    this.inscriptionDate = inscriptionHour;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "NewsletterInscricao{" +
        "idInscricao=" + inscriptionId +
        ", email='" + email + '\'' +
        ", dataInscricao=" + inscriptionDate +
        ", ativo=" + active +
        '}';
  }
}
