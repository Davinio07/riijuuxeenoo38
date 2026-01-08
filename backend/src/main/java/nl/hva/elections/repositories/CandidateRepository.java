package nl.hva.elections.repositories;

import nl.hva.elections.models.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByNameAndPartyId(String name, Long partyId);
    // Nieuwe methodes om te filteren op electionId via de Party relatie
    List<Candidate> findByPartyElectionId(String electionId);
    List<Candidate> findByPartyIdAndPartyElectionId(Long partyId, String electionId);
    List<Candidate> findByGenderAndPartyElectionId(String gender, String electionId);
    List<Candidate> findByPartyIdAndGenderAndPartyElectionId(Long partyId, String gender, String electionId);

    // Bestaande methodes (als fallback)
    List<Candidate> findByPartyIdAndGender(Long partyId, String gender);
    List<Candidate> findByPartyId(Long partyId);
    List<Candidate> findByGender(String gender);
}