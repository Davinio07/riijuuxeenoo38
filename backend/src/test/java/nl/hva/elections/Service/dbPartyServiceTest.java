package nl.hva.elections.Service;

import nl.hva.elections.models.Party;
import nl.hva.elections.repositories.PartyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class dbPartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @InjectMocks
    private dbPartyService partyService;

    @Test
    @DisplayName("Happy Flow: Should return list of parties for a valid electionId")
    void getPartiesByElection_Success() {
        // Arrange
        String electionId = "TK2021";
        List<Party> mockParties = List.of(new Party("TK2021", "D66"), new Party("TK2021", "PVV"));
        when(partyRepository.findByElectionId(electionId)).thenReturn(mockParties);

        // Act
        List<Party> result = partyService.getPartiesByElection(electionId);

        // Assert
        assertEquals(2, result.size());
        verify(partyRepository, times(1)).findByElectionId(electionId);
    }

    @Test
    @DisplayName("Unhappy Flow: Should return empty list if electionId does not exist")
    void getPartiesByElection_NotFound() {
        when(partyRepository.findByElectionId("NON_EXISTENT")).thenReturn(List.of());

        List<Party> result = partyService.getPartiesByElection("NON_EXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Happy Flow: Should find a party by name and electionId")
    void findPartyByName_Success() {
        String electionId = "TK2023";
        String name = "NSC";
        Party mockParty = new Party(electionId, name);

        when(partyRepository.findByNameAndElectionId(name, electionId))
                .thenReturn(Optional.of(mockParty));

        Optional<Party> result = partyService.findPartyByName(electionId, name);

        assertTrue(result.isPresent());
        assertEquals("NSC", result.get().getName());
    }
}