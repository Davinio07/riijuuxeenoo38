package nl.hva.elections.controllers;

import nl.hva.elections.Service.dbPartyService;
import nl.hva.elections.models.Party;
import nl.hva.elections.security.JwtTokenProvider; // Import the missing provider
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService; // Required by the filter
import org.springframework.test.context.bean.override.mockito.MockitoBean; // For Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the PartyController.
 * Updated to mock security dependencies to prevent ApplicationContext failure.
 */
@WebMvcTest(PartyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private dbPartyService partyService;

    // --- Security Mocks to satisfy JwtTokenFilter constructor ---
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Happy Flow: Get all parties for a valid electionId")
    void getPoliticalParties_Success() throws Exception {
        String electionId = "TK2023";
        Party p1 = new Party(electionId, "VVD");
        p1.setId(1L);

        when(partyService.getPartiesByElection(electionId)).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/parties/{electionId}", electionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("VVD"));

        verify(partyService, times(1)).getPartiesByElection(electionId);
    }

    @Test
    @DisplayName("Unhappy Flow: Return 404 when no parties are found")
    void getPoliticalParties_NotFound() throws Exception {
        String electionId = "UNKNOWN";
        when(partyService.getPartiesByElection(electionId)).thenReturn(List.of());

        mockMvc.perform(get("/api/parties/{electionId}", electionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Happy Flow: Search for a specific party by name")
    void findPartyByName_Success() throws Exception {
        String electionId = "TK2023";
        String partyName = "D66";
        Party party = new Party(electionId, partyName);

        when(partyService.findPartyByName(electionId, partyName)).thenReturn(Optional.of(party));

        mockMvc.perform(get("/api/parties/{electionId}/search", electionId)
                        .param("partyName", partyName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("D66"));
    }
}