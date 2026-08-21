package com.ibizabroker.bibliotheque.dto;

import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import lombok.Getter;

import java.util.Date;

@Getter
public class ReservationResponse {

    private final Integer id;
    private final Integer livreId;
    private final Integer adherentId;
    private final Date dateReservation;
    private final Date dateExpiration;
    private final ReservationStatus statut;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.livreId = reservation.getLivre().getBookId();
        this.adherentId = reservation.getAdherent().getUserId();
        this.dateReservation = reservation.getDateReservation();
        this.dateExpiration = reservation.getDateExpiration();
        this.statut = reservation.getStatut();
    }
}
