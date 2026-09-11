package com.ibizabroker.bibliotheque.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.JwtRequest;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
