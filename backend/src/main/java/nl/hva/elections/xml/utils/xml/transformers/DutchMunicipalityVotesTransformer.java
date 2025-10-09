package nl.hva.elections.xml.utils.xml.transformers;

import nl.hva.elections.xml.model.Election;
import nl.hva.elections.xml.model.MunicipalityResult;
import nl.hva.elections.xml.utils.xml.VotesTransformer;

import java.util.Map;

/**
 * Transforms municipality-level voting data from the XML files into the application's data model.
 */
public class DutchMunicipalityVotesTransformer implements VotesTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the votes at the municipality level. It expects an instance of
     * Election that can be used for storing the results.
     * @param election the election in which the votes wil be stored.
     */
    public DutchMunicipalityVotesTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        // This is the key: The municipality data is NOT aggregated. National data is.
        // This 'if' statement ensures we only process municipality results.
        if (aggregated) {
            return;
        }

        String municipalityName = electionData.get("ReportingUnitIdentifier");
        String partyName = electionData.get("RegisteredName");
        String votesString = electionData.get("ValidVotes");

        if (municipalityName != null && !municipalityName.isBlank() && partyName != null) {
            int totalVotes = votesString != null ? Integer.parseInt(votesString) : 0;
            MunicipalityResult result = new MunicipalityResult(municipalityName, partyName, totalVotes);
            election.addMunicipalityResult(result);
        }
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Not needed for this feature
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Not needed for this feature
    }
}