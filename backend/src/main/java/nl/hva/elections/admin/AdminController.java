package nl.hva.elections.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        // We sturen een simpele JSON response terug
        return Map.of("message", "Pong! De connectie met de Java backend werkt! 🐧");
    }
}