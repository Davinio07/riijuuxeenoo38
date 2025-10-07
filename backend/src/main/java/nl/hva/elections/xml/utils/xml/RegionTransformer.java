package nl.hva.elections.xml.utils.xml;

import java.util.Map;

public interface RegionTransformer {
    void registerRegion(Map<String, String> electionData);
}