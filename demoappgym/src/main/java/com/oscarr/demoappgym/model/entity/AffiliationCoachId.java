package com.oscarr.demoappgym.model.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable 
public class AffiliationCoachId implements Serializable {

    private Long idCoach;
    private Long numberAffiliation;

    public AffiliationCoachId(){}

    public AffiliationCoachId(Long idCoach, Long numberAffiliation) {
        this.idCoach = idCoach;
        this.numberAffiliation = numberAffiliation;
    }

    public Long getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(Long idCoach) {
        this.idCoach = idCoach;
    }

    public Long getNumberAffiliation() {
        return numberAffiliation;
    }

    public void setNumberAffiliation(Long numberAffiliation) {
        this.numberAffiliation = numberAffiliation;
    }
    
@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffiliationCoachId that = (AffiliationCoachId) o;
        return Objects.equals(idCoach, that.idCoach) && 
            Objects.equals(numberAffiliation, that.numberAffiliation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCoach, numberAffiliation);
    }


}
