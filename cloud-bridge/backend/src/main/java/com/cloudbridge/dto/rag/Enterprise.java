package com.cloudbridge.dto.rag;

import java.util.List;

public class Enterprise {
    private String id;
    private String name;
    private String industry;
    private String scale;
    private String location;
    private String description;
    private List<String> products;
    private String website;
    private String contact;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getScale() { return scale; }
    public void setScale(String scale) { this.scale = scale; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getProducts() { return products; }
    public void setProducts(List<String> products) { this.products = products; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
