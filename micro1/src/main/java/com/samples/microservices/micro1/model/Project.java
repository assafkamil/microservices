package com.samples.microservices.micro1.model;


import javax.persistence.*;

@Entity(name="projects")
public class Project {
    @Id
    @GeneratedValue
    @Column(name = "project_id")
    private Long projectId;
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner_id) {
        this.owner = owner_id;
    }
}
