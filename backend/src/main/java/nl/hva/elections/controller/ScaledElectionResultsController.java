package nl.hva.elections.controller;

import nl.hva.elections.Service.ScaledElectionResultsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ScaledElectionResults")
public class ScaledElectionResultsController {

    private final ScaledElectionResultsService service;

    public ScaledElectionResultsController(ScaledElectionResultsService service) {
        this.service = service;
    }

    @GetMapping("/Result")
    public Map<String, String> test() {
        return Map.of("message", service.getTestMessage());
    }
}
