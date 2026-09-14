package com.ibizabroker.bibliotheque.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.RoleRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.JwtRequest;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.Role;
import com.ibizabroker.bibliotheque.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUpReservationData() {
        Role userRole = roleRepository.findByRoleName("User")
                .orElseGet(() -> roleRepository.save(new Role() {{ setRoleName("User"); }}));

        Users a1 = usersRepository.findByUsername("A1")
                .orElseGet(() -> {
                    Users user = new Users();
                    user.setUsername("A1");
                    user.setName("Adhérent A1");
                    user.setPassword("$2a$10$Qq7F8J1mNQ8fZ0wY7aG8d.0Y1J8fNnNfL0Zs9rQEB7JXU3ml7Fj2");
                    user.setRole(java.util.Set.of(userRole));
                    return usersRepository.save(user);
                });

        Users a2 = usersRepository.findByUsername("A2")
                .orElseGet(() -> {
                    Users user = new Users();
                    user.setUsername("A2");
                    user.setName("Adhérent A2");
                    user.setPassword("$2a$10$Qq7F8J1mNQ8fZ0wY7aG8d.0Y1J8fNnNfL0Zs9rQEB7JXU3ml7Fj2");
                    user.setRole(java.util.Set.of(userRole));
                    return usersRepository.save(user);
                });

        if (reservationRepository.findByAdherentUserId(a2.getUserId()).isEmpty()) {
            Reservation reservation = new Reservation();
            reservation.setAdherent(a2);
            reservation.setLivre(new com.ibizabroker.bibliotheque.entity.Books());
            reservation.getLivre().setBookId(1);
            reservation.getLivre().setBookName("L1");
            reservation.getLivre().setBookAuthor("Auteur L1");
            reservation.getLivre().setBookGenre("Test");
            reservation.getLivre().setNoOfCopies(0);
            reservationRepository.save(reservation);
        }

        if (reservationRepository.findByAdherentUserId(a1.getUserId()).isEmpty()) {
            Reservation myReservation = new Reservation();
            myReservation.setAdherent(a1);
            myReservation.setLivre(new com.ibizabroker.bibliotheque.entity.Books());
            myReservation.getLivre().setBookId(2);
            myReservation.getLivre().setBookName("L2");
            myReservation.getLivre().setBookAuthor("Auteur L2");
            myReservation.getLivre().setBookGenre("Test");
            myReservation.getLivre().setNoOfCopies(0);
            reservationRepository.save(myReservation);
        }
    }

    @Test
    void unauthenticatedReservationListReturns401() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adherentTokenCanListOwnReservations() throws Exception {
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + tokenFor("A1", "a148")))
                .andExpect(status().isOk());
    }

    @Test
    void adherentTokenCannotReadAnotherAdherentsReservation() throws Exception {
        Users anotherAdherent = usersRepository.findByUsername("A2").orElseThrow();
        Reservation reservation = reservationRepository.findByAdherentUserId(anotherAdherent.getUserId())
                .stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/reservations/" + reservation.getId())
                        .header("Authorization", "Bearer " + tokenFor("A1", "a148")))
                .andExpect(status().isForbidden());
    }

            @Test
            void adherentTokenCannotCreateReservationForAnotherAdherent() throws Exception {
            Users anotherAdherent = usersRepository.findByUsername("A2").orElseThrow();
            Reservation reservation = reservationRepository.findByAdherentUserId(anotherAdherent.getUserId())
                .stream()
                .findFirst()
                .orElseThrow();

            String body = "{\"livreId\":" + reservation.getLivre().getBookId()
                + ",\"adherentId\":" + anotherAdherent.getUserId() + "}";

            mockMvc.perform(post("/api/reservations")
                    .header("Authorization", "Bearer " + tokenFor("A1", "a148"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isForbidden());
            }

    private String tokenFor(String username, String password) throws Exception {
        JwtRequest request = new JwtRequest();
        request.setUserName(username);
        request.setUserPassword(password);

        String response = mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseBody = objectMapper.readTree(response);
        return responseBody.get("jwtToken").asText();
    }
}
