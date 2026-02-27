package com.cloudbridge.dto.rag;

import java.util.List;

public class Patent {
    private String id;
    private String title;
    private String patentNumber;
    private String assignee;
    private String inventor;
    private String applicationDate;
    private String publicationDate;
    private String abstractText;
    private String status;
    private List<String> classifications;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPatentNumber() { return patentNumber; }
    public void setPatentNumber(String patentNumber) { this.patentNumber = patentNumber; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getInventor() { return inventor; }
    public void setInventor(String inventor) { this.inventor = inventor; }
    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    public String getPublicationDate() { return publicationDate; }
    public void setPublicationDate(String publicationDate) { this.publicationDate = publicationDate; }
    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getClassifications() { return classifications; }
    public void setClassifications(List<String> classifications) { this.classifications = classifications; }
}
