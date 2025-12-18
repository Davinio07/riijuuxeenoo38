package nl.hva.elections.Service;

import nl.hva.elections.models.Election;
import nl.hva.elections.models.MunicipalityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import nl.hva.elections.Service.DutchElectionService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DutchElectionServiceTest {

    @InjectMocks
    private nl.hva.elections.Service.DutchElectionService dutchElectionService;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Create a dummy Election object
        Election mockElection = new Election("TK2023");

        // 2. Populate it with dummy data
        // We add multiple entries for "Amsterdam" to test aggregation (summing votes)
        List<MunicipalityResult> results = mockElection.getMunicipalityResults();

        results.add(new MunicipalityResult("Amsterdam", "VVD", 100));
        results.add(new MunicipalityResult("Amsterdam", "VVD", 50)); // Same party, should sum to 150
        results.add(new MunicipalityResult("Amsterdam", "D66", 200));

        results.add(new MunicipalityResult("Rotterdam", "PVV", 300));
        results.add(new MunicipalityResult("Utrecht", "GL", 150));

        // 3. Inject this mock election into the private "electionCache" using Reflection
        // This bypasses the XML parsing logic entirely
        Field cacheField = DutchElectionService.class.getDeclaredField("electionCache");
        cacheField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Election> cache = (Map<String, Election>) cacheField.get(dutchElectionService);
        cache.put("TK2023", mockElection);
    }

    @Test
    void getMunicipalityNames_ShouldReturnUniqueAndSortedNames() {
        // Act
        List<String> names = dutchElectionService.getMunicipalityNames("TK2023");

        // Assert
        assertEquals(3, names.size(), "Should have 3 unique municipalities");
        assertEquals("Amsterdam", names.get(0)); // Verifies sorting (A-Z)
        assertEquals("Rotterdam", names.get(1));
        assertEquals("Utrecht", names.get(2));
    }

    @Test
    void getResultsForMunicipality_ShouldAggregateAndSortVotes() {
        // Act
        List<MunicipalityResult> results = dutchElectionService.getResultsForMunicipality("TK2023", "Amsterdam");

        // Assert
        assertEquals(2, results.size(), "Should return 2 parties (VVD and D66) for Amsterdam");

        // Check Sorting: Highest votes first
        // D66 has 200, VVD has 100+50=150. So D66 must be first.
        assertEquals("D66", results.get(0).getPartyName());
        assertEquals(200, results.get(0).getValidVotes());

        // Check Aggregation: VVD should be 100 + 50 = 150
        assertEquals("VVD", results.get(1).getPartyName());
        assertEquals(150, results.get(1).getValidVotes());
    }

    @Test
    void getResultsForMunicipality_ShouldReturnEmptyListForUnknownMunicipality() {
        // Act
        List<MunicipalityResult> results = dutchElectionService.getResultsForMunicipality("TK2023", "UnknownCity");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for unknown municipality");
    }

    @Test
    void getResultsForMunicipality_ShouldIgnoreCase() {
        // Act: Search for "amsterdam" (lowercase) when data is "Amsterdam"
        List<MunicipalityResult> results = dutchElectionService.getResultsForMunicipality("TK2023", "amsterdam");

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
    }
}