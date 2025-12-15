package nl.hva.elections.transformers;

import nl.hva.elections.models.Election;
import nl.hva.elections.models.Party;
import nl.hva.elections.xml.utils.xml.transformers.DutchNationalVotesTransformer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DutchNationalVotesTransformerTest {

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

    @Test
    void testRegisterPartyVotesHandlesMissingVotes() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        Map<String, String> data = Map.of(
                "RegisteredName", "NoVotesParty"
                // Missing "ValidVotes"
        );

        transformer.registerPartyVotes(true, data);

        ArgumentCaptor<Party> captor = ArgumentCaptor.forClass(Party.class);
        verify(election).addNationalResult(captor.capture());

        Party p = captor.getValue();

        assertEquals("NoVotesParty", p.getName());
        assertEquals(0, p.getVotes());
    }

    @Test
    void testRegisterCandidateVotesDoesNothing() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        assertDoesNotThrow(() ->
                transformer.registerCandidateVotes(false, Map.of("x", "y"))
        );
    }

    @Test
    void testRegisterMetadataDoesNothing() {
        Election election = mock(Election.class);
        DutchNationalVotesTransformer transformer = new DutchNationalVotesTransformer(election);

        assertDoesNotThrow(() ->
                transformer.registerMetadata(false, Map.of("m", "data"))
        );
    }
}
