package com.ibizabroker.bibliotheque.service;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.dto.ReservationCreateRequest;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.exceptions.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BooksRepository booksRepository;

    @Mock
    private UsersRepository usersRepository;

    private ReservationService reservationService;
    private ReservationCreateRequest request;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, booksRepository, usersRepository);

        Books book = new Books();
        book.setBookId(10);
        book.setNoOfCopies(0);

        Users user = new Users();
        user.setUserId(20);

        request = new ReservationCreateRequest();
        request.setLivreId(book.getBookId());
        request.setAdherentId(user.getUserId());

        when(booksRepository.findById(10)).thenReturn(Optional.of(book));
        when(usersRepository.findById(20)).thenReturn(Optional.of(user));
        when(reservationRepository.existsByLivreBookIdAndAdherentUserIdAndStatutIn(any(), any(), any()))
                .thenReturn(false);
    }

    @Test
    void adherentWithTwoActiveReservationsCanCreateAThird() {
        when(reservationRepository.countByAdherentUserIdAndStatutIn(any(), any())).thenReturn(2L);
        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> reservationService.create(request));
    }

    @Test
    void adherentWithThreeActiveReservationsCannotCreateAnother() {
        when(reservationRepository.countByAdherentUserIdAndStatutIn(any(), any())).thenReturn(3L);

        assertThrows(ConflictException.class, () -> reservationService.create(request));
    }
}
