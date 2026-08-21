package com.ibizabroker.bibliotheque.service;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.dto.ReservationCreateRequest;
import com.ibizabroker.bibliotheque.dto.ReservationResponse;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.exceptions.ConflictException;
import com.ibizabroker.bibliotheque.exceptions.NotFoundException;
import com.ibizabroker.bibliotheque.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = Arrays.asList(
            ReservationStatus.EN_ATTENTE,
            ReservationStatus.DISPONIBLE
    );

    private final ReservationRepository reservationRepository;
    private final BooksRepository booksRepository;
    private final UsersRepository usersRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               BooksRepository booksRepository,
                               UsersRepository usersRepository) {
        this.reservationRepository = reservationRepository;
        this.booksRepository = booksRepository;
        this.usersRepository = usersRepository;
    }

    public ReservationResponse create(ReservationCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Le corps de la requête est obligatoire");
        }
        if (request.getLivreId() == null) {
            throw new ValidationException("livreId est obligatoire");
        }
        if (request.getAdherentId() == null) {
            throw new ValidationException("adherentId est obligatoire");
        }

        Books livre = booksRepository.findById(request.getLivreId())
                .orElseThrow(() -> new NotFoundException("Livre introuvable : " + request.getLivreId()));
        Users adherent = usersRepository.findById(request.getAdherentId())
                .orElseThrow(() -> new NotFoundException("Adhérent introuvable : " + request.getAdherentId()));

        if (livre.getNoOfCopies() != null && livre.getNoOfCopies() > 0) {
            throw new ConflictException("RG-01 : un livre disponible ne peut pas être réservé");
        }
        if (reservationRepository.existsByLivreBookIdAndAdherentUserIdAndStatutIn(
                livre.getBookId(), adherent.getUserId(), ACTIVE_STATUSES)) {
            throw new ConflictException("RG-02 : une seule réservation active est autorisée pour ce livre et cet adhérent");
        }
        if (reservationRepository.countByAdherentUserIdAndStatutIn(adherent.getUserId(), ACTIVE_STATUSES) >= 3) {
            throw new ConflictException("RG-03 : un adhérent ne peut pas dépasser 3 réservations actives");
        }

        Reservation reservation = new Reservation();
        reservation.setLivre(livre);
        reservation.setAdherent(adherent);
        return new ReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll(ReservationStatus statut, Integer adherentId) {
        List<Reservation> reservations;
        if (statut != null && adherentId != null) {
            reservations = reservationRepository.findByStatutAndAdherentUserId(statut, adherentId);
        } else if (statut != null) {
            reservations = reservationRepository.findByStatut(statut);
        } else if (adherentId != null) {
            reservations = reservationRepository.findByAdherentUserId(adherentId);
        } else {
            reservations = reservationRepository.findAll();
        }
        return reservations.stream().map(ReservationResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Integer id) {
        return new ReservationResponse(reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable : " + id)));
    }

    public ReservationResponse cancel(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable : " + id));
        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE
                && reservation.getStatut() != ReservationStatus.DISPONIBLE) {
            throw new ConflictException("RG-05/RG-06 : cette réservation ne peut plus être annulée ou modifiée");
        }
        reservation.setStatut(ReservationStatus.ANNULEE);
        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public void delete(Integer id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("Réservation introuvable : " + id);
        }
        reservationRepository.deleteById(id);
    }
}
