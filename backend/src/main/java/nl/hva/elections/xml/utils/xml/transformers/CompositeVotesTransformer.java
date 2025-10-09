package nl.hva.elections.xml.utils.xml.transformers;

import nl.hva.elections.xml.utils.xml.VotesTransformer;
import java.util.Map;

/**
 * A special transformer that wraps two other transformers.
 * This allows data from a single file to be processed by multiple transformers at once.
 */
public class CompositeVotesTransformer implements VotesTransformer {

    private final VotesTransformer primaryTransformer;
    private final VotesTransformer secondaryTransformer;

    public CompositeVotesTransformer(VotesTransformer primary, VotesTransformer secondary) {
        this.primaryTransformer = primary;
        this.secondaryTransformer = secondary;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        primaryTransformer.registerPartyVotes(aggregated, electionData);
        secondaryTransformer.registerPartyVotes(aggregated, electionData);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        primaryTransformer.registerCandidateVotes(aggregated, electionData);
        secondaryTransformer.registerCandidateVotes(aggregated, electionData);
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        primaryTransformer.registerMetadata(aggregated, electionData);
        secondaryTransformer.registerMetadata(aggregated, electionData);
    }
}