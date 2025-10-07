package nl.hva.elections.xml.utils.xml.transformers;

import nl.hva.elections.xml.model.Candidate;
import nl.hva.elections.xml.model.Election;
import nl.hva.elections.xml.utils.xml.CandidateTransformer;

import java.util.Map;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchCandidateTransformer implements CandidateTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the candidate lists. It expects an instance of Election that can
     * be used for storing the candidates lists.
     * @param election the election in which the candidate lists wil be stored.
     */
    public DutchCandidateTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerCandidate(Map<String, String> electionData) {
        Candidate candidate = new Candidate(electionData.get("ContestIdentifier"));
        candidate.setFirstName(electionData.get("FirstName"));
        candidate.setLastName(electionData.get("LastName"));
        candidate.setGender(electionData.get("Gender"));
        candidate.setLocality(electionData.get("LocalityName"));






        System.out.println("Registering candidate: " + electionData);
    }
}
