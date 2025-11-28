package nl.hva.elections.Service;

import nl.hva.elections.models.Party;
import nl.hva.elections.service.DutchElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NationalResultServiceTest {

    // Mock the dependency (the XML data source)
    @Mock
    private DutchElectionService electionService;

    // Inject the mocks into the class being tested
    @InjectMocks
    private NationalResultService nationalResultService;

    private final String ELECTION_ID = "TK2023";

    @BeforeEach
    void setUp() {
        // Setup is minimal as we focus on the calculation method
    }

    @Test
    void calculateSeats_shouldDistributeSeatsCorrectly_SimpleCase() {
        // Arrange: 10 seats, 3 parties
        List<Party> results = List.of(
                new Party("A", 4200),
                new Party("B", 3500),
                new Party("C", 2300)
        );
        int totalSeats = 10;

        // Act
        Map<String, Integer> seats = nationalResultService.calculateSeats(results, totalSeats);

        // Assert
        assertNotNull(seats);
        assertEquals(3, seats.size());


        assertEquals(4, seats.get("A"));
        assertEquals(4, seats.get("B"));
        assertEquals(2, seats.get("C")); // Corrected expected value from 3 to 2

        assertEquals(10, seats.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void calculateSeats_shouldHandleEdgeCase_AllVotesToOneParty() {
        // Arrange
        List<Party> results = List.of(
                new Party("OnlyOne", 10000),
                new Party("Zero", 0)
        );
        int totalSeats = 10;

        // Act
        Map<String, Integer> seats = nationalResultService.calculateSeats(results, totalSeats);

        // Assert
        assertEquals(10, seats.get("OnlyOne"));
        assertEquals(0, seats.get("Zero"));
    }

    @Test
    void calculateSeats_shouldHandleEqualVotes_TieBreakerByFormula() {
        // Arrange
        List<Party> results = List.of(
                new Party("X", 5000),
                new Party("Y", 5000)
        );
        int totalSeats = 3;

        // Act
        Map<String, Integer> seats = nationalResultService.calculateSeats(results, totalSeats);

        // Assert: Assumes implementation grants the seat to the party encountered first (X) on a tie.
        assertEquals(2, seats.get("X"));
        assertEquals(1, seats.get("Y"));
        assertEquals(3, seats.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void getNationalResults_shouldRetrieveDataFromService() {
        // Arrange
        Party mockParty = new Party("Mock", 100);
        nl.hva.elections.models.Election mockElection = new nl.hva.elections.models.Election(ELECTION_ID);
        mockElection.addNationalResult(mockParty);

        when(electionService.getElectionData(ELECTION_ID)).thenReturn(mockElection);

        // Act
        List<Party> results = nationalResultService.getNationalResults(ELECTION_ID);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals("Mock", results.get(0).getName());
        verify(electionService, times(1)).getElectionData(ELECTION_ID);
    }
}