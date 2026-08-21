package com.ibizabroker.bibliotheque.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Data
@Entity
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Books livre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Users adherent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date dateReservation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE;

    @PrePersist
    private void initializeDates() {
        if (dateReservation == null) {
            dateReservation = new Date();
        }
        if (dateExpiration == null) {
            Calendar expiration = Calendar.getInstance();
            expiration.setTime(dateReservation);
            expiration.add(Calendar.DAY_OF_MONTH, 7);
            dateExpiration = expiration.getTime();
        }
        if (statut == null) {
            statut = ReservationStatus.EN_ATTENTE;
        }
    }
}
