package com.oscarr.demoappgym.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name="Coach")
public class Coach {

@Id 
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long idCoach;

@Column(nullable=false,length=100)
private String nameCoach;

public Coach(){}


public Coach(Long idCoach, String nameCoach) {
    this.idCoach = idCoach;
    this.nameCoach = nameCoach;
}



public Long getIdCoach() {
    return idCoach;
}

public void setIdCoach(Long idCoach) {
    this.idCoach = idCoach;
}

public String getNameCoach() {
    return nameCoach;
}

public void setNameCoach(String nameCoach) {
    this.nameCoach = nameCoach;
}

}
