package nl.hva.elections.xml.api;

import nl.hva.elections.xml.model.PoliticalParty;
import nl.hva.elections.xml.service.PartyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsible for serving all data related to Political Parties
 * from the XML cache.
 */
@RestController
@RequestMapping("/api/parties") // Een schone, eigen base URL voor partijen
public class PartyController {

    private static final Logger logger = LoggerFactory.getLogger(PartyController.class);

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    /**
     * Haalt alle politieke partijen op voor een specifieke verkiezing.
     * Endpoint: GET /api/parties/{electionId}
     */
    @GetMapping("/{electionId}")
    public ResponseEntity<List<PoliticalParty>> getPoliticalParties(@PathVariable String electionId) {
        try {
            List<PoliticalParty> parties = partyService.getPoliticalParties(electionId);
            logger.debug("Total parties: {}\n", parties.size());
            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            logger.error("Error fetching parties for electionId: {}. {}", electionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Zoekt een specifieke partij op naam.
     * Endpoint: GET /api/parties/{electionId}/search?partyName=...
     */
    @GetMapping("/{electionId}/search")
    public ResponseEntity<PoliticalParty> findPartyByName(
            @PathVariable String electionId,
            @RequestParam String partyName) {
        try {
            return partyService.findPartyByName(electionId, partyName)
                    .map(party -> {
                        logger.info("Found party: {}", party.getRegisteredAppellation());
                        return ResponseEntity.ok(party);
                    })
                    .orElseGet(() -> {
                        logger.warn("Party not found: {}", partyName);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error searching for party '{}' in election '{}'. {}", partyName, electionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Telt het aantal partijen voor een verkiezing.
     * Endpoint: GET /api/parties/{electionId}/count
     */
    @GetMapping("/{electionId}/count")
    public ResponseEntity<Integer> getPartyCount(@PathVariable String electionId) {
        try {
            int count = partyService.getPartyCount(electionId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error counting parties for electionId: {}. {}", electionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Haalt alle partijnamen op voor een verkiezing.
     * Endpoint: GET /api/parties/{electionId}/names
     */
    @GetMapping("/{electionId}/names")
    public ResponseEntity<List<String>> getPartyNames(@PathVariable String electionId) {
        try {
            List<String> partyNames = partyService.getPartyNames(electionId);
            return ResponseEntity.ok(partyNames);
        } catch (Exception e) {
            logger.error("Error fetching party names for electionId: {}. {}", electionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}