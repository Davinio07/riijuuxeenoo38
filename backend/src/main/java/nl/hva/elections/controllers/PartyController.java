package nl.hva.elections.controllers;

import nl.hva.elections.Service.dbPartyService;
import nl.hva.elections.models.Party;
import nl.hva.elections.dtos.NationalResultDto; // Import DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors; // Unnecessary after Java 16, but safe to keep for consistency

/**
 * Controller responsible for serving all data related to Political Parties.
 * Refactored to fetch data from the database via dbPartyService.
 */
@RestController
@RequestMapping("/api/parties")
public class PartyController {

    private static final Logger logger = LoggerFactory.getLogger(PartyController.class);

    private final dbPartyService partyService;

    /**
     * Constructs a new PartyController with the database service.
     *
     * @param partyService The service used to retrieve party data from the DB.
     */
    public PartyController(dbPartyService partyService) {
        this.partyService = partyService;
    }

    /**
     * Retrieves all political parties for a specific election from the database.
     * <p>
     * Endpoint: {@code GET /api/parties/{electionId}}
     *
     * @param electionId The unique identifier for the election.
     * @return A {@link ResponseEntity} containing a list of {@link NationalResultDto} DTOs.
     */
    @GetMapping("/{electionId}")
    public ResponseEntity<List<NationalResultDto>> getPoliticalParties(@PathVariable String electionId) { // Changed return type
        try {
            List<Party> parties = partyService.getPartiesByElection(electionId);

            if (parties.isEmpty()) {
                logger.warn("No parties found in database for electionId: {}", electionId);
                return ResponseEntity.notFound().build();
            }

            // Map Party Entity to DTO
            List<NationalResultDto> dtos = parties.stream()
                    .map(PartyController::convertToDto)
                    .toList();

            logger.debug("Total parties fetched from DB: {}\n", parties.size());
            return ResponseEntity.ok(dtos); // Return DTOs
        } catch (Exception e) {
            logger.error("Internal server error fetching parties for electionId: {}. {}", electionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds a specific political party by its name within a given election.
     * <p>
     * Endpoint: {@code GET /api/parties/{electionId}/search?partyName=...}
     *
     * @param electionId The unique identifier for the election.
     * @param partyName  The name of the party to find (passed as a request parameter).
     * @return A {@link ResponseEntity} containing the matching {@link NationalResultDto}.
     */
    @GetMapping("/{electionId}/search")
    public ResponseEntity<NationalResultDto> findPartyByName( // Changed return type
                                                              @PathVariable String electionId,
                                                              @RequestParam String partyName) {
        try {
            return partyService.findPartyByName(electionId, partyName)
                    .map(party -> {
                        logger.info("Found party in DB: {}", party.getName());
                        // Map Party Entity to DTO
                        NationalResultDto dto = convertToDto(party);
                        return ResponseEntity.ok(dto); // Return DTO
                    })
                    .orElseGet(() -> {
                        logger.warn("Party not found in DB: {}", partyName);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error searching for party '{}' in election '{}'. {}", partyName, electionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Counts the total number of political parties participating in a specific election.
     * <p>
     * Endpoint: {@code GET /api/parties/{electionId}/count}
     *
     * @param electionId The unique identifier for the election.
     * @return A {@link ResponseEntity} containing the total count.
     */
    @GetMapping("/{electionId}/count")
    public ResponseEntity<Integer> getPartyCount(@PathVariable String electionId) {
        try {
            List<Party> parties = partyService.getPartiesByElection(electionId);
            return ResponseEntity.ok(parties.size());
        } catch (Exception e) {
            logger.error("Error counting parties for electionId: {}. {}", electionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a list of all political party names for a specific election.
     * <p>
     * Endpoint: {@code GET /api/parties/{electionId}/names}
     *
     * @param electionId The unique identifier for the election.
     * @return A {@link ResponseEntity} containing a {@link List} of party names.
     */
    @GetMapping("/{electionId}/names")
    public ResponseEntity<List<String>> getPartyNames(@PathVariable String electionId) {
        try {
            List<String> partyNames = partyService.getPartiesByElection(electionId).stream()
                    .map(Party::getName)
                    .toList();
            return ResponseEntity.ok(partyNames);
        } catch (Exception e) {
            logger.error("Error fetching party names for electionId: {}. {}", electionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Private helper method to convert entity to DTO
    private static NationalResultDto convertToDto(Party party) {
        return new NationalResultDto(
                party.getId(),
                party.getElectionId(),
                party.getName(),
                party.getVotes(),
                party.getSeats(),
                party.getPercentage()
        );
    }
}