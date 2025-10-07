package nl.hva.elections.xml.utils.xml.transformers;

import nl.hva.elections.xml.model.Election;
import nl.hva.elections.xml.model.Region;
import nl.hva.elections.xml.utils.xml.RegionTransformer;

import java.util.Map;

public class DutchRegionTransformer implements RegionTransformer {
    private final Election election;

    public DutchRegionTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerRegion(Map<String, String> electionData) {
        Region region = new Region(
                electionData.get("RegionNumber"),
                electionData.get("RegionName"),
                electionData.get("RegionCategory"),
                electionData.get("SuperiorRegionCategory")
        );

        election.addRegion(region);

        System.out.println("Registered region: " + region);
    }
}
