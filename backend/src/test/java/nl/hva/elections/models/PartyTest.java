package nl.hva.elections.models;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Party model.
 * Verifies constructors, getters/setters, candidates list handling, and toString().
 */
class PartyTest {

    /**
     * Ensures the constructor that accepts electionId and name
     * correctly initializes default values for all other fields.
     */
    @Test
    void testConstructorWithElectionIdAndName() {
        Party p = new Party("1", "VVD");

        assertEquals("1", p.getElectionId());
        assertEquals("VVD", p.getName());
        assertEquals(0, p.getVotes());
        assertEquals(0, p.getSeats());
        assertEquals(0.0, p.getPercentage());
    }

    /**
     * Ensures the constructor that accepts name and votes
     * correctly stores both values.
     */
    @Test
    void testConstructorWithNameAndVotes() {
        Party p = new Party("VVD", 12345);

        assertEquals("VVD", p.getName());
        assertEquals(12345, p.getVotes());
    }

    /**
     * Ensures all getters and setters work correctly.
     */
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

    /**
     * Ensures Party can store and retrieve a candidate list correctly.
     */
    @Test
    void testCandidatesList() {
        Party p = new Party();

        List<Candidate> mockCandidates = new ArrayList<>();
        mockCandidates.add(new Candidate());

        p.setCandidates(mockCandidates);

        assertEquals(1, p.getCandidates().size());
        assertSame(mockCandidates, p.getCandidates());
    }

    /**
     * Ensures toString() output contains all key field values.
     */
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
