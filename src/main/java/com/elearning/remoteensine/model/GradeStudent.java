package com.elearning.remoteensine.model;

import java.time.LocalDateTime;

public class GradeStudent {

    private Integer idDefinitionExam;
    private String title;
    private String description;
    private LocalDateTime  creationDate;
    private boolean published;
    private Integer examId;
    private Integer grade;
    private Integer totalValueExam;

    public Integer getIdDefinitionExam() {
        return idDefinitionExam;
    }

    public void setIdDefinitionExam(Integer idDefinitionExam) {
        this.idDefinitionExam = idDefinitionExam;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getTotalValueExam() {
        return totalValueExam;
    }

    public void setTotalValueExam(Integer totalValueExam) {
        this.totalValueExam = totalValueExam;
    }
}
