package com.cloudbridge.dto.rag;

import java.util.Map;

public class KGEntity {
    private String id;
    private String name;
    private String type;
    private Map<String, Object> attributes;
    private String industryChain;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getIndustryChain() {
        return industryChain;
    }

    public void setIndustryChain(String industryChain) {
        this.industryChain = industryChain;
    }
}
