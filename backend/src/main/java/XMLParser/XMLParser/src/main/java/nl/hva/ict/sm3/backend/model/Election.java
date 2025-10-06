package XMLParser.XMLParser.src.main.java.nl.hva.ict.sm3.backend.model;

import XMLParser.XMLParser.src.main.java.nl.hva.ict.sm3.backend.model.Candidate;

import java.util.ArrayList;
import java.util.List;

public class Election {
    private final String id;
    private final List<Candidate> candidates = new ArrayList<>();

    public Election(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    // 👇 Method to add a candidate
    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    @Override
    public String toString() {
        return "Election{" +
                "id='" + id + '\'' +
                ", candidates=" + candidates +
                '}';
    }
}
