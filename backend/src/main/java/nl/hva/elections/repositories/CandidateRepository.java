// src/main/java/nl/hva/elections/repositories/CandidateRepository.java
package nl.hva.elections.repositories;

import nl.hva.elections.persistence.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // <-- Import this

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer> {
    boolean existsByPartyIdAndName(Integer partyId, String name);

    /** Finds candidates based on both Party ID and Gender. */
    List<Candidate> findByPartyIdAndGender(Integer partyId, String gender);

    /** Finds candidates based only on Party ID. */
    List<Candidate> findByPartyId(Integer partyId);

    /** Finds candidates based only on Gender. */
    List<Candidate> findByGender(String gender);
}