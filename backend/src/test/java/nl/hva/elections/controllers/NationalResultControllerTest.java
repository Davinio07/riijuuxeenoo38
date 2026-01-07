package nl.hva.elections.controllers;

import nl.hva.elections.Service.NationalResultService;
import nl.hva.elections.Service.dbPartyService;
import nl.hva.elections.models.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the NationalResultController.
 *
 * This class tests the HTTP endpoints exposed by the NationalResultController
 * using Spring's MockMvc for isolated testing of the controller layer.
 * Service dependencies are mocked to control their behavior.
 */
class NationalResultControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NationalResultService nationalResultService;

    @Mock
    private dbPartyService dbPartyService;

    // Fixed ID representing a specific election (e.g., 2023)
    private final String ELECTION_ID = "TK2023";

    /**
     * Set up method executed before each test.
     *
     * 1. Initializes Mockito annotations (e.g., @Mock).
     * 2. Instantiates the NationalResultController with the mocked services.
     * 3. Sets up MockMvc for the controller under test in standalone mode.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        NationalResultController controller =
                new NationalResultController(nationalResultService, dbPartyService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // HAPPY PATH TESTS
    /**
     * Test case for successful retrieval of national results.
     *
     * Goal: Verify that the GET request to `/api/nationalResult/{ELECTION_ID}/national`
     * returns an HTTP **200 OK** status, **application/json** content type,
     * and a list of parties with their vote counts.
     *
     * @throws Exception If MockMvc execution fails.
     */
    @Test
    void getNationalResults_shouldReturn200AndParties() throws Exception {
        // ARRANGE: Mock data and service behavior
        List<Party> parties = List.of(
                new Party("A", 1000), // Party A with 1000 votes
                new Party("B", 500)   // Party B with 500 votes
        );

        when(dbPartyService.getPartiesByElection(ELECTION_ID))
                .thenReturn(parties);

        // ACT & ASSERT: Perform request and check expectations
        mockMvc.perform(get("/api/nationalResult/" + ELECTION_ID + "/national"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("A")) // Check party A's name
                .andExpect(jsonPath("$[1].name").value("B")); // Check party B's name
    }

    /**
     * Test case for successful seat calculation.
     *
     * Goal: Verify that the GET request to `/api/nationalResult/{ELECTION_ID}/seats`
     * returns an HTTP **200 OK** status and a map containing the calculated seats per party.
     *
     * @throws Exception If MockMvc execution fails.
     */
    @Test
    void getSeats_shouldReturn200AndSeatMap() throws Exception {
        // ARRANGE: Mock data and service behavior
        List<Party> results = List.of(new Party("A", 1000));
        Map<String, Integer> seatMap = Map.of("A", 150); // Party A gets 150 seats

        // Mock the two necessary service calls
        when(nationalResultService.getNationalResults(ELECTION_ID))
                .thenReturn(results);

        when(nationalResultService.calculateSeats(results, 150))
                .thenReturn(seatMap);

        // ACT & ASSERT: Perform request and check expectations
        mockMvc.perform(get("/api/nationalResult/" + ELECTION_ID + "/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.A").value(150)); // Check if key "A" maps to value 150
    }

    // UNHAPPY PATH TESTS
    /**
     * Test case for retrieving national results when no parties are found.
     *
     * Goal: Verify that the GET request returns an HTTP **204 No Content**
     * status when the underlying service returns an empty list of parties.
     *
     * @throws Exception If MockMvc execution fails.
     */
    @Test
    void getNationalResults_shouldReturn204WhenNoPartiesFound() throws Exception {
        // ARRANGE: Mock service to return an empty list
        when(dbPartyService.getPartiesByElection(ELECTION_ID))
                .thenReturn(List.of());

        // ACT & ASSERT: Perform request and check for 204
        mockMvc.perform(get("/api/nationalResult/" + ELECTION_ID + "/national"))
                .andExpect(status().isNoContent());
    }

    /**
     * Test case for seat calculation when the election ID is invalid or not found.
     *
     * Goal: Verify that the GET request returns an HTTP **404 Not Found**
     * when the `getNationalResults` service method throws a RuntimeException,
     * simulating an election not being present in the data store.
     *
     * @throws Exception If MockMvc execution fails.
     */
    @Test
    void getSeats_shouldReturn404WhenElectionNotFound() throws Exception {
        // ARRANGE: Mock service to throw an exception
        when(nationalResultService.getNationalResults(ELECTION_ID))
                .thenThrow(new RuntimeException("Not found"));

        // ACT & ASSERT: Perform request and check for 404
        mockMvc.perform(get("/api/nationalResult/" + ELECTION_ID + "/seats"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test case for seat calculation failing due to invalid data/logic.
     *
     * Goal: Verify that the GET request returns an HTTP **404 Not Found**
     * when the `calculateSeats` service method throws an exception (e.g., IllegalArgumentException),
     * signaling a problem during the calculation process.
     *
     * @throws Exception If MockMvc execution fails.
     */
    @Test
    void getSeats_shouldReturn404OnSeatCalculationError() throws Exception {
        // ARRANGE: Mock data and service behavior
        List<Party> results = List.of(new Party("A", 1000));

        when(nationalResultService.getNationalResults(ELECTION_ID))
                .thenReturn(results);

        // Mock the calculation step to throw an exception
        when(nationalResultService.calculateSeats(results, 150))
                .thenThrow(new IllegalArgumentException("Bad input"));

        // ACT & ASSERT: Perform request and check for 404
        mockMvc.perform(get("/api/nationalResult/" + ELECTION_ID + "/seats"))
                .andExpect(status().isNotFound());
    }
}