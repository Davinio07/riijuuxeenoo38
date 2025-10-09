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
        // This transformer should only handle municipality-level results, not aggregated ones.
        if (aggregated) {
            return;
        }

        String municipalityName = electionData.get("ReportingUnitIdentifier");
        String partyName = electionData.get("RegisteredName");
        String votesString = electionData.get("ValidVotes");

        if (municipalityName != null && !municipalityName.isBlank() && partyName != null) {
            int totalVotes = votesString != null ? Integer.parseInt(votesString) : 0;
            MunicipalityResult result = new MunicipalityResult(municipalityName, partyName, totalVotes);

            // FIX: Manually check if an identical result already exists before adding it.
            // This prevents the duplicate data issue without changing the Election model.
            boolean alreadyExists = election.getMunicipalityResults().stream().anyMatch(existing ->
                    existing.getMunicipalityName().equals(result.getMunicipalityName()) &&
                    existing.getPartyName().equals(result.getPartyName()) &&
                    existing.getValidVotes() == result.getValidVotes()
            );

            if (!alreadyExists) {
                election.addMunicipalityResult(result);
            }
        }
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Deze methode kan leeg blijven als je geen kandidaat-specifieke data op gemeenteniveau nodig hebt.
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Deze methode kan leeg blijven als je geen metadata op gemeenteniveau nodig hebt.
    }
}