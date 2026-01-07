package nl.hva.elections.Service;

import nl.hva.elections.models.Party;
import nl.hva.elections.Service.DutchElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NationalResultService.
 *
 * This class tests the business logic within the NationalResultService,
 * primarily focusing on the seat distribution algorithm and data retrieval from the mocked DutchElectionService.
 */
@ExtendWith(MockitoExtension.class)
class NationalResultServiceTest {

    // Mock the dependency (the XML data source)
    @Mock
    private DutchElectionService electionService;

    // Inject the mocks into the class being tested
    @InjectMocks
    private NationalResultService nationalResultService;

    // Fixed ID representing a specific election
    private final String ELECTION_ID = "TK2023";

    /**
     * Set up method executed before each test.
     *
     * In this setup, the MockitoExtension handles the initialization of
     * `@Mock` and injection into `@InjectMocks`.
     */
    @BeforeEach
    void setUp() {
        // Setup is minimal as we focus on the calculation method
    }

    // HAPPY PATH TESTS
    /**
     * Test case for correct seat distribution in a simple scenario.
     *
     * Goal: Verify the proportional seat calculation logic for 3 parties
     * with distinct vote totals across 10 seats, ensuring the final seat count is correct.
     */
    @Test
    void calculateSeats_shouldDistributeSeatsCorrectly_SimpleCase() {
        // Arrange: 10 seats, 3 parties (Total Votes: 10000)
        List<Party> results = List.of(
                new Party("A", 4200), // Expected Seats: 4
                new Party("B", 3500), // Expected Seats: 4
                new Party("C", 2300)  // Expected Seats: 2
        );
        int totalSeats = 10;

        // Act
        Map<String, Integer> seats = nationalResultService.calculateSeats(results, totalSeats);

        // Assert
        assertNotNull(seats);
        assertEquals(3, seats.size()); // Check all parties are present

        assertEquals(4, seats.get("A"));
        assertEquals(4, seats.get("B"));
        assertEquals(2, seats.get("C"));

        // Check total seats sum to the expected count
        assertEquals(10, seats.values().stream().mapToInt(Integer::intValue).sum());
    }

    /**
     * Test case for the edge scenario where one party wins all seats.
     *
     * Goal: Verify the calculation handles a dominant party receiving all votes,
     * ensuring the total seat count is distributed correctly (100% to one party).
     */
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

    /**
     * Test case for a tie-breaking scenario in seat allocation.
     *
     * Goal: Verify the system's tie-breaking mechanism when two parties have
     * exactly the same vote count and are competing for the final seat.
     */
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

        // Assert: Assumes the proportional rule/ordering results in X getting 2 and Y getting 1
        assertEquals(2, seats.get("X"));
        assertEquals(1, seats.get("Y"));
        assertEquals(3, seats.values().stream().mapToInt(Integer::intValue).sum());
    }

    /**
     * Test case for successful data retrieval from the external service.
     *
     * Goal: Verify that `getNationalResults` correctly calls the mocked
     * `electionService` and returns the party list extracted from the Election object.
     */
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
        // Verify that the dependency method was called exactly once
        verify(electionService, times(1)).getElectionData(ELECTION_ID);
    }

    // UNHAPPY PATH TESTS
    /**
     * Test case for failure when the election data cannot be found.
     *
     * Goal: Verify that `getNationalResults` throws a **NullPointerException**
     * if the underlying data service returns `null` for the requested election ID.
     */
    @Test
    void getNationalResults_shouldFailWhenElectionDoesNotExist() {
        // Arrange: Mock service returns null
        when(electionService.getElectionData(ELECTION_ID)).thenReturn(null);

        // Assert: Expect an exception when the service attempts to process the null result
        assertThrows(NullPointerException.class,
                () -> nationalResultService.getNationalResults(ELECTION_ID));
    }

    /**
     * Test case for calculating seats with an empty list of results.
     *
     * Goal: Verify that the calculation method returns an empty map when
     * provided with an empty list of results, preventing errors.
     */
    @Test
    void calculateSeats_shouldReturnEmptyMap_whenNoResults() {
        // Act
        Map<String, Integer> seats = nationalResultService.calculateSeats(Collections.emptyList(), 10);
        // Assert
        assertTrue(seats.isEmpty());
    }

    /**
     * Test case for calculating seats with a null input list.
     *
     * Goal: Verify that the calculation method correctly throws a **NullPointerException**
     * when the input list of parties is null, enforcing data integrity.
     */
    @Test
    void calculateSeats_shouldThrowException_whenResultsIsNull() {
        // Assert: Expect an exception when the method is called with a null list
        assertThrows(NullPointerException.class,
                () -> nationalResultService.calculateSeats(null, 10));
    }
}