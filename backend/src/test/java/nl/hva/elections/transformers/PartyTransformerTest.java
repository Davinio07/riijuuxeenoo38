package nl.hva.elections.transformers;

import nl.hva.elections.Service.XmlPartyService.AffiliationIdentifier;
import nl.hva.elections.models.Party;
import nl.hva.elections.xml.utils.xml.transformers.PartyTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartyTransformerTest {

    private PartyTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new PartyTransformer();
    }

    @Test
    @DisplayName("Happy Flow: Should transform valid AffiliationIdentifier to Party")
    void toParty_ShouldMapCorrectly() {
        AffiliationIdentifier raw = new AffiliationIdentifier("1", "VVD");
        Party result = transformer.toParty(raw);

        assertEquals("1", result.getElectionId());
        assertEquals("VVD", result.getName());
    }

    @Test
    @DisplayName("Unhappy Flow: Should throw IllegalArgumentException for incomplete data")
    void toParty_IncompleteData_ShouldThrowException() {
        // ID is missing
        AffiliationIdentifier incomplete = new AffiliationIdentifier(null, "Test Party");

        assertThrows(IllegalArgumentException.class, () -> transformer.toParty(incomplete));
    }

    @Test
    @DisplayName("Happy Flow: Should return an empty list when input list is null")
    void toPartyList_NullInput_ShouldReturnEmptyList() {
        List<Party> results = transformer.toPartyList(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}