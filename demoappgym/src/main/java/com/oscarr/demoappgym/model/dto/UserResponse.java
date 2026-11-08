package com.oscarr.demoappgym.model.dto;

public class UserResponse {
    private Long id;
    private String name;
    private String address;
    private Long phoneNumber;
    private String documentNumber;
    private String mail;
    private String password;
    private String photoData;
    private String physicalConditionReport;
    private String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String address, Long phoneNumber, String mail,
            String documentNumber, String physicalConditionReport, String role) {
        this(id, name, address, phoneNumber, mail, documentNumber, null, physicalConditionReport, role);
    }

    public UserResponse(Long id, String name, String address, Long phoneNumber, String mail,
            String documentNumber, String password, String physicalConditionReport, String role) {
        this(id, name, address, phoneNumber, mail, documentNumber, password, null, physicalConditionReport, role);
    }

    public UserResponse(Long id, String name, String address, Long phoneNumber, String mail,
            String documentNumber, String password, String photoData, String physicalConditionReport, String role) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.documentNumber = documentNumber;
        this.mail = mail;
        this.password = password;
        this.photoData = photoData;
        this.physicalConditionReport = physicalConditionReport;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public String getPhysicalConditionReport() {
        return physicalConditionReport;
    }

    public void setPhysicalConditionReport(String physicalConditionReport) {
        this.physicalConditionReport = physicalConditionReport;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
