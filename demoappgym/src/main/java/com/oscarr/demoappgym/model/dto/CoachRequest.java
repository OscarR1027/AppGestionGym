package com.oscarr.demoappgym.model.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CoachRequest {

    @NotBlank(message = "El nombre del coach es obligatorio")
    private String nameCoach;

    @Size(max = 1400000, message = "La foto no puede superar 1 MB")
    @Pattern(regexp = "^data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/]+={0,2}$",
            message = "La foto debe ser una imagen PNG, JPEG o WEBP válida")
    private String photoData;

    private boolean photoDataProvided;

    public CoachRequest() {
    }

    public CoachRequest(String nameCoach) {
        this.nameCoach = nameCoach;
    }

    public String getNameCoach() {
        return nameCoach;
    }

    public void setNameCoach(String nameCoach) {
        this.nameCoach = nameCoach;
    }

    public String getPhotoData() {
        return photoData;
    }

    @JsonSetter("photoData")
    public void setPhotoData(String photoData) {
        this.photoData = photoData;
        this.photoDataProvided = true;
    }

    public boolean hasPhotoData() {
        return photoDataProvided;
    }
}
