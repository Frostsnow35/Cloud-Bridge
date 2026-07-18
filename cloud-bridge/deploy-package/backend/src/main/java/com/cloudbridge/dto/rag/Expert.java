package com.cloudbridge.dto.rag;

import java.util.List;

public class Expert {
    private String id;
    private String name;
    private String title;
    private String institution;
    private List<String> researchArea;
    private List<String> achievements;
    private String collaborationHistory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<String> getResearchArea() {
        return researchArea;
    }

    public void setResearchArea(List<String> researchArea) {
        this.researchArea = researchArea;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public String getCollaborationHistory() {
        return collaborationHistory;
    }

    public void setCollaborationHistory(String collaborationHistory) {
        this.collaborationHistory = collaborationHistory;
    }
}
