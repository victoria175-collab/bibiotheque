package com.ibizabroker.bibliotheque.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReservationCreateRequest {

    @NotNull(message = "livreId est obligatoire")
    private Integer livreId;

    @NotNull(message = "adherentId est obligatoire")
    private Integer adherentId;
}
