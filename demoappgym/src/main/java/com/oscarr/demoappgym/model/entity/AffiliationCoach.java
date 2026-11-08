package com.oscarr.demoappgym.model.entity;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name="Affiliation_Coach")
public class AffiliationCoach {

    @EmbeddedId 
    @AttributeOverrides({
        @AttributeOverride(name = "idCoach", column = @Column(name = "`idCoach`")),
        @AttributeOverride(name = "numberAffiliation", column = @Column(name = "`numberAffiliation`"))
    })
    private AffiliationCoachId Id= new AffiliationCoachId();

    @ManyToOne 
    @JoinColumn (name="id_coach",nullable = false)
    private Coach coach;

    @ManyToOne 
    @JoinColumn (name="number_affiliation",nullable=false)
    private Affiliation affiliation;

    @Column (name="Start_Date",nullable = false)
    private LocalDate startdate;

    @Column (name="End_Date",nullable = false)
    private LocalDate endDate;

public AffiliationCoach (){}

public AffiliationCoach(AffiliationCoachId id, Coach coach, Affiliation affiliation, LocalDate startdate,
        LocalDate endDate) {
    Id = id;
    this.coach = coach;
    this.affiliation = affiliation;
    this.startdate = startdate;
    this.endDate = endDate;
}

public AffiliationCoachId getId() {
    return Id;
}

public void setId(AffiliationCoachId id) {
    Id = id;
}

public Coach getCoach() {
    return coach;
}

public void setCoach(Coach coach) {
    this.coach = coach;
}

public Affiliation getAffiliation() {
    return affiliation;
}

public void setAffiliation(Affiliation affiliation) {
    this.affiliation = affiliation;
}

public LocalDate getStartdate() {
    return startdate;
}

public void setStartdate(LocalDate startdate) {
    this.startdate = startdate;
}

public LocalDate getEndDate() {
    return endDate;
}

public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
}

}
