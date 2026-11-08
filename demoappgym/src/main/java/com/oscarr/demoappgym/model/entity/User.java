package com.oscarr.demoappgym.model.entity;

import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.model.enums.DocumentType;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name="Users")
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codeUser;
    
    @Column(name="Name",length=60,nullable = false)
        private String name;
    
    @Column(name="Address",length=100,nullable = false)
        private String address;

    @Column(nullable = false)
        private String password;
    
    @Column(name="PhoneNumber",length=40,nullable = false)
        private long phoneNumber;
    
    @Column(name="mail",length=100,nullable = false)
        private String mail;

    @Column(name = "document_number", length = 30)
        private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 30)
        private DocumentType documentType;

    @Column(name = "photo_data", columnDefinition = "LONGTEXT")
        private String photoData;

    @Column(name="Physical_Condition_Report",length=300,nullable=true)
        private String physicalConditionReport;

    @Enumerated (EnumType.STRING)
        private Role role;
    
    public User(){}
    
    public User(Long codeUser, String name, String address, long phoneNumber, 
        String mail,String physicalConditionReport,String password,Role role) {
        this.codeUser = codeUser;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.mail = mail;
        this.physicalConditionReport= physicalConditionReport;
        this.password= password;
        this.role = role;
    }

    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getCodeUser() {
        return codeUser;
    }

    public void setCodeUser(Long codeUser) {
        this.codeUser = codeUser;
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

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
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


}
