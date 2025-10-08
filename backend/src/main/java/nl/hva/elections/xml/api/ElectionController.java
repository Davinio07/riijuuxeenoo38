package nl.hva.elections.xml.api;

import nl.hva.elections.xml.model.Election;
import nl.hva.elections.xml.model.PoliticalParty;
import nl.hva.elections.xml.model.Region;
import nl.hva.elections.xml.service.DutchElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demo controller for showing how you could load the election data in the backend.
 */
@RestController
@RequestMapping("/api/elections")
public class ElectionController {
    private final DutchElectionService electionService;
    private List<Region> regions = new ArrayList<>();

    public ElectionController(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    /**
     * Processes the result for a specific election.
     * @param electionId the id of the election, e.g. the value of the Id attribute from the ElectionIdentifier tag.
     * @param folderName the name of the folder that contains the XML result files. If none is provided the value from
     *                   the electionId is used.
     * @return Election if the results have been processed successfully. Please be sure you don't output all the data!
     * Just the general data about the election should be sent back to the front-end!<br/>
     * <i>If you want to return something else please feel free to do so!</i>
     */
    @PostMapping("{electionId}")
    public Election readResults(@PathVariable String electionId, @RequestParam(required = false) String folderName) {
        if (folderName == null) {
            return electionService.readResults(electionId, electionId);
        } else {
            return electionService.readResults(electionId, folderName);
        }
    }

    public void addRegion(Region region) {
        this.regions.add(region);
    }

    public List<Region> getRegions() {
        return regions;
    }

    @GetMapping("{electionId}/regions")
    public List<Region> getRegions(@PathVariable String electionId,
                                   @RequestParam(required = false) String folderName) {
        try {
            // If no folderName is provided via URL parameter, use the correct default.
            String effectiveFolderName = (folderName == null) ? "TK2023_HvA_UvA" : folderName;

            // For the specific case of the TK2023 election, we ensure the correct folder is used.
            if ("TK2023".equals(electionId)) {
                effectiveFolderName = "TK2023_HvA_UvA";
            }

            Election election = electionService.readResults(electionId, effectiveFolderName);

            if (election == null) {
                return Collections.emptyList();
            }

            return election.getRegions();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get all political parties for a specific election.
     *
     * @param electionId the election identifier (e.g., "TK2023")
     * @param folderName optional folder name, defaults to TK2023_HvA_UvA for TK2023
     * @return list of political parties
     */
    @GetMapping("{electionId}/parties")
    public ResponseEntity<List<PoliticalParty>> getPoliticalParties(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {

        try {
            // Use the same folder logic as getRegions
            String effectiveFolderName = (folderName == null) ? "TK2023_HvA_UvA" : folderName;

            if ("TK2023".equals(electionId)) {
                effectiveFolderName = "TK2023_HvA_UvA";
            }

            Election election = electionService.readResults(electionId, effectiveFolderName);

            if (election == null) {
                return ResponseEntity.notFound().build();
            }

            List<PoliticalParty> parties = election.getPoliticalParties();

            // Print to console
            System.out.println("\n=== Political Parties for " + electionId + " ===");
            parties.forEach(party ->
                    System.out.println("- " + party.getRegisteredAppellation())
            );
            System.out.println("Total parties: " + parties.size() + "\n");

            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * Get a specific political party by its name.
     *
     * @param electionId the election identifier
     * @param partyName the party name to search for
     * @param folderName optional folder name
     * @return the political party if found
     */
    @GetMapping("{electionId}/parties/search")
    public ResponseEntity<PoliticalParty> findPartyByName(
            @PathVariable String electionId,
            @RequestParam String partyName,
            @RequestParam(required = false) String folderName) {

        try {
            String effectiveFolderName = (folderName == null) ? "TK2023_HvA_UvA" : folderName;

            if ("TK2023".equals(electionId)) {
                effectiveFolderName = "TK2023_HvA_UvA";
            }

            Election election = electionService.readResults(electionId, effectiveFolderName);

            if (election == null) {
                return ResponseEntity.notFound().build();
            }

            PoliticalParty foundParty = election.getPoliticalParties().stream()
                    .filter(party -> party.getRegisteredAppellation()
                            .toLowerCase()
                            .contains(partyName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (foundParty != null) {
                System.out.println("Found party: " + foundParty.getRegisteredAppellation());
                return ResponseEntity.ok(foundParty);
            } else {
                System.out.println("Party not found: " + partyName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get the count of political parties.
     *
     * @param electionId the election identifier
     * @param folderName optional folder name
     * @return count of parties
     */
    @GetMapping("{electionId}/parties/count")
    public ResponseEntity<Integer> getPartyCount(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {

        try {
            String effectiveFolderName = (folderName == null) ? "TK2023_HvA_UvA" : folderName;

            if ("TK2023".equals(electionId)) {
                effectiveFolderName = "TK2023_HvA_UvA";
            }

            Election election = electionService.readResults(electionId, effectiveFolderName);

            if (election == null) {
                return ResponseEntity.ok(0);
            }

            int count = election.getPoliticalParties().size();
            System.out.println("Total parties for " + electionId + ": " + count);

            return ResponseEntity.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(0);
        }
    }

    /**
     * Get all party names as a simple list of strings.
     * Useful for dropdowns or autocomplete features.
     *
     * @param electionId the election identifier
     * @param folderName optional folder name
     * @return list of party names
     */
    @GetMapping("{electionId}/parties/names")
    public ResponseEntity<List<String>> getPartyNames(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {

        try {
            String effectiveFolderName = (folderName == null) ? "TK2023_HvA_UvA" : folderName;

            if ("TK2023".equals(electionId)) {
                effectiveFolderName = "TK2023_HvA_UvA";
            }

            Election election = electionService.readResults(electionId, effectiveFolderName);

            if (election == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<String> partyNames = election.getPoliticalParties().stream()
                    .map(PoliticalParty::getRegisteredAppellation)
                    .toList();

            return ResponseEntity.ok(partyNames);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}