package nl.hva.elections.models;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartyTest {

    @Test
    void testConstructorWithElectionIdAndName() {
        Party p = new Party("1", "VVD");

        assertEquals("1", p.getElectionId());
        assertEquals("VVD", p.getName());
        assertEquals(0, p.getVotes());
        assertEquals(0, p.getSeats());
        assertEquals(0.0, p.getPercentage());
    }

    @Test
    void testConstructorWithNameAndVotes() {
        Party p = new Party("VVD", 12345);

        assertEquals("VVD", p.getName());
        assertEquals(12345, p.getVotes());
    }

    @Test
    void testGettersAndSetters() {
        Party p = new Party();
        p.setId(10L);
        p.setElectionId("TK2025");
        p.setName("VVD");
        p.setVotes(5000);
        p.setSeats(12);
        p.setPercentage(7.5);

        assertEquals(10L, p.getId());
        assertEquals("TK2025", p.getElectionId());
        assertEquals("VVD", p.getName());
        assertEquals(5000, p.getVotes());
        assertEquals(12, p.getSeats());
        assertEquals(7.5, p.getPercentage());
    }

    @Test
    void testCandidatesList() {
        Party p = new Party();

        List<Candidate> mockCandidates = new ArrayList<>();
        mockCandidates.add(new Candidate()); // simple empty candidate

        p.setCandidates(mockCandidates);

        assertEquals(1, p.getCandidates().size());
        assertSame(mockCandidates, p.getCandidates());
    }

    @Test
    void testToStringContainsKeyFields() {
        Party p = new Party();
        p.setId(1L);
        p.setElectionId("X");
        p.setName("Party A");
        p.setVotes(100);
        p.setSeats(3);
        p.setPercentage(5.5);

        String result = p.toString();

        assertTrue(result.contains("Party{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("electionId='X'"));
        assertTrue(result.contains("name='Party A'"));
        assertTrue(result.contains("votes=100"));
        assertTrue(result.contains("seats=3"));
        assertTrue(result.contains("percentage=5.5"));
    }
}
