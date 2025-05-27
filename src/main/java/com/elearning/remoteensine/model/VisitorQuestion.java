package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class VisitorQuestion {
  private int questionId;
  private String visitorName;
  private String visitorEmail;
  private String questionText;
  private LocalDateTime questionHour;
  private String answer;
  private LocalDateTime answerHour;
  private Integer professorResponsibleId;

  public VisitorQuestion() {
    this.answerHour = LocalDateTime.now();
  }

  public VisitorQuestion(String visitorName, String visitorEmail, String questionText) {
    this();
    this.visitorName = visitorName;
    this.visitorEmail = visitorEmail;
    this.questionText = questionText;
    this.questionHour = LocalDateTime.now();
  }

  public int getQuestionId() {
    return questionId;
  }

  public void setQuestionId(int questionId) {
    this.questionId = questionId;
  }

  public Integer getProfessorResponsibleId() {
    return professorResponsibleId;
  }

  public void setProfessorResponsibleId(Integer professorId) {
    this.professorResponsibleId = professorId;
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

  public LocalDateTime getQuestionHour() {
    return questionHour;
  }

  public void setQuestionHour(LocalDateTime questionHour) {
    this.questionHour = questionHour;
  }

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public String getVisitorEmail() {
    return visitorEmail;
  }

  public void setVisitorEmail(String visitorEmail) {
    this.visitorEmail = visitorEmail;
  }

  public String getVisitorName() {
    return visitorName;
  }

  public void setVisitorName(String visitorName) {
    this.visitorName = visitorName;
  }

  public void answerQuestionByProfessor(String answerText, User professorResponsible){
    this.setAnswer(answerText);
    this.setProfessorResponsibleId(professorResponsible.getIdUser());
    this.setQuestionHour(LocalDateTime.now());
  }
  @Override
  public String toString() {
    return "PerguntaVisitante{" +
        "idPergunta=" + questionId +
        ", nomeVisitante='" + visitorName + '\'' +
        ", emailVisitante='" + visitorEmail + '\'' +
        ", textoPergunta='" + (questionText != null ? questionText.substring(0, Math.min(questionText.length(), 30)) + "..." : "N/A") + '\'' +
        ", respondida=" + (answer != null && !answer.isEmpty()) +
        '}';
  }
}
