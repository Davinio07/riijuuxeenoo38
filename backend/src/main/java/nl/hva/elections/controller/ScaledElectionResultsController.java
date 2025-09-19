package nl.hva.elections.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ScaledElectionResults")
public class ScaledElectionResultsController {

    @GetMapping("/Result")
    public Map<String, String> test() {
        return Map.of("message", "Backend werkt!");
    }
}
