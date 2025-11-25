package nl.hva.elections.controllers;

import nl.hva.elections.dtos.KieskringDTO;
import nl.hva.elections.models.Kieskring;
import nl.hva.elections.models.Province;
import nl.hva.elections.repositories.KieskringRepository;
import nl.hva.elections.repositories.ProvinceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provinces")
public class ProvinceController {

    private static final Logger logger = LoggerFactory.getLogger(ProvinceController.class);
    private final ProvinceRepository provinceRepository;
    private final KieskringRepository kieskringRepository;

    public ProvinceController(ProvinceRepository provinceRepository, KieskringRepository kieskringRepository) {
        this.provinceRepository = provinceRepository;
        this.kieskringRepository = kieskringRepository;
    }

    @GetMapping
    public ResponseEntity<List<Province>> getAllProvinces() {
        try {
            logger.info("Fetching all provinces");
            List<Province> provinces = provinceRepository.findAll();
            return ResponseEntity.ok(provinces);
        } catch (Exception e) {
            logger.error("Error fetching provinces", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/kieskringen")
    public ResponseEntity<List<KieskringDTO>> getKieskringenByProvince(@PathVariable Integer id) {
        try {
            logger.info("Fetching kieskringen for province ID: {}", id);

            if (!provinceRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            List<Kieskring> entities = kieskringRepository.findByProvinceId(id);

            // Convert to DTOs (so the frontend gets "id" instead of "kieskring_id")
            List<KieskringDTO> dtos = entities.stream()
                    .map(k -> new KieskringDTO(
                            k.getKieskring_id(),    // Map kieskring_id -> id
                            k.getName(),
                            k.getProvince().getName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error fetching kieskringen for province {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}