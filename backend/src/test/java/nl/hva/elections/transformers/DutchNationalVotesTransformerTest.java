package nl.hva.elections.transformers;

import nl.hva.elections.models.Election;
import nl.hva.elections.models.Party;
import nl.hva.elections.xml.utils.xml.transformers.DutchNationalVotesTransformer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DutchNationalVotesTransformer.
 * Verifies correct Party creation, vote parsing, and ignored methods.
 */
class DutchNationalVotesTransformerTest {

    /**
     * Ensures registerPartyVotes() creates a Party with the correct values
     * and passes it to the Election object.
     */
    @Test
    void testRegisterPartyVotesCreatesCorrectParty() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        Map<String, String> data = Map.of(
                "RegisteredName", "VVD",
                "ValidVotes", "25000"
        );

        transformer.registerPartyVotes(true, data);

        ArgumentCaptor<Party> captor = ArgumentCaptor.forClass(Party.class);
        verify(election, times(1)).addNationalResult(captor.capture());

        Party p = captor.getValue();

        assertEquals("VVD", p.getName());
        assertEquals(25000, p.getVotes());
    }

    /**
     * Ensures registerPartyVotes() sets votes to 0 when "ValidVotes"
     * is missing in the input data.
     */
    @Test
    void testRegisterPartyVotesHandlesMissingVotes() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        Map<String, String> data = Map.of(
                "RegisteredName", "NoVotesParty"
        );

        transformer.registerPartyVotes(true, data);

        ArgumentCaptor<Party> captor = ArgumentCaptor.forClass(Party.class);
        verify(election).addNationalResult(captor.capture());

        Party p = captor.getValue();

        assertEquals("NoVotesParty", p.getName());
        assertEquals(0, p.getVotes());
    }

    /**
     * Ensures registerCandidateVotes() performs no action and does not throw.
     */
    @Test
    void testRegisterCandidateVotesDoesNothing() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        assertDoesNotThrow(() ->
                transformer.registerCandidateVotes(false, Map.of("x", "y"))
        );
    }

    /**
     * Ensures registerMetadata() performs no action and does not throw.
     */
    @Test
    void testRegisterMetadataDoesNothing() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        assertDoesNotThrow(() ->
                transformer.registerMetadata(false, Map.of("m", "data"))
        );
    }
}
