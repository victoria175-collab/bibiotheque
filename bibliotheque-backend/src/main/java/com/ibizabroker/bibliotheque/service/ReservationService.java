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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        return create(request, null);
    }

    public ReservationResponse create(ReservationCreateRequest request, Authentication authentication) {
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
        ensureCanActFor(authentication, adherent);

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
        return findAll(statut, adherentId, null);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll(ReservationStatus statut, Integer adherentId,
                                              Authentication authentication) {
        Integer effectiveAdherentId = restrictToAuthenticatedAdherent(adherentId, authentication);
        List<Reservation> reservations;
        if (statut != null && effectiveAdherentId != null) {
            reservations = reservationRepository.findByStatutAndAdherentUserId(statut, effectiveAdherentId);
        } else if (statut != null) {
            reservations = reservationRepository.findByStatut(statut);
        } else if (effectiveAdherentId != null) {
            reservations = reservationRepository.findByAdherentUserId(effectiveAdherentId);
        } else {
            reservations = reservationRepository.findAll();
        }
        return reservations.stream().map(ReservationResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Integer id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Integer id, Authentication authentication) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable : " + id));
        ensureCanAccess(authentication, reservation);
        return new ReservationResponse(reservation);
    }

    public ReservationResponse cancel(Integer id) {
        return cancel(id, null);
    }

    public ReservationResponse cancel(Integer id, Authentication authentication) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Réservation introuvable : " + id));
        ensureCanAccess(authentication, reservation);
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

    private Integer restrictToAuthenticatedAdherent(Integer requestedAdherentId, Authentication authentication) {
        if (authentication == null) {
            return requestedAdherentId;
        }
        if (isLibrarian(authentication)) {
            return requestedAdherentId;
        }
        Users currentUser = currentUser(authentication);
        if (requestedAdherentId != null && !requestedAdherentId.equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Un adhérent ne peut consulter que ses réservations");
        }
        return currentUser.getUserId();
    }

    private void ensureCanActFor(Authentication authentication, Users adherent) {
        if (authentication == null) {
            return;
        }
        if (isLibrarian(authentication)) {
            return;
        }
        Users currentUser = currentUser(authentication);
        if (!adherent.getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Un adhérent ne peut créer une réservation que pour lui-même");
        }
    }

    private void ensureCanAccess(Authentication authentication, Reservation reservation) {
        if (authentication == null) {
            return;
        }
        if (isLibrarian(authentication)) {
            return;
        }
        Users currentUser = currentUser(authentication);
        if (!reservation.getAdherent().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Cette réservation appartient à un autre adhérent");
        }
    }

    private Users currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur authentifié introuvable"));
    }

    private boolean isLibrarian(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_BIBLIOTHECAIRE")
                        || authority.equals("ROLE_Admin"));
    }
}
