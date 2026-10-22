package com.campusreporter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ComplaintRequestDTO {

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Campus block is required")
    private String campusBlock;

    @NotBlank(message = "Floor level is required")
    private String floorLevel;

    @NotBlank(message = "Classroom or specific location is required")
    private String specificLocation;

    @NotBlank(message = "Issue title is required")
    @Size(min = 5, max = 150, message = "Issue title must be between 5 and 150 characters")
    private String issueTitle;

    @NotBlank(message = "Description is required")
    @Size(min = 15, max = 1500, message = "Description must be between 15 and 1500 characters")
    private String description;

    @NotBlank(message = "Contact phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    private String studentPhone;

    private boolean emergency;

    private String studentEmail;
    private String studentName;
    private String idempotencyKey;
    private List<String> imageUrls;

    public ComplaintRequestDTO() {}

    // Getters and Setters

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCampusBlock() {
        return campusBlock;
    }

    public void setCampusBlock(String campusBlock) {
        this.campusBlock = campusBlock;
    }

    public String getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(String floorLevel) {
        this.floorLevel = floorLevel;
    }

    public String getSpecificLocation() {
        return specificLocation;
    }

    public void setSpecificLocation(String specificLocation) {
        this.specificLocation = specificLocation;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public void setIsEmergency(boolean isEmergency) {
        this.emergency = isEmergency;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
