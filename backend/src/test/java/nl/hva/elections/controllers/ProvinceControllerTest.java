package nl.hva.elections.controllers;

import nl.hva.elections.models.Kieskring;
import nl.hva.elections.models.Province;
import nl.hva.elections.repositories.KieskringRepository;
import nl.hva.elections.repositories.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProvinceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KieskringRepository kieskringRepository;
    @Mock
    private ProvinceRepository provinceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ProvinceController controller = new ProvinceController(provinceRepository, kieskringRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getAllProvinces_ShouldReturnListOfProvinces() throws Exception {
        // Create the expected data
        Province province = new Province();
        province.setProvince_id(1);
        province.setName("Noord-Holland");

        List<Province> allProvinces = Collections.singletonList(province);

        // Tell the Mock Repository: "When someone asks for findAll(), return this list"
        when(provinceRepository.findAll()).thenReturn(allProvinces);

        // Perform the request and verify
        mockMvc.perform(get("/api/provinces")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()) // Expect 200 OK
                        .andExpect(jsonPath("$[0].province_id").value(1))
                        .andExpect(jsonPath("$[0].name").value("Noord-Holland"));
    }

    @Test
    public void shouldReturnKieskringenForValidProvince() throws Exception {
        int provinceId = 1;

        Province dummyProvince = new Province();
        dummyProvince.setProvince_id(provinceId);
        dummyProvince.setName("North Holland");

        Kieskring kieskring = new Kieskring();
        kieskring.setKieskring_id(101);
        kieskring.setName("Amsterdam");
        kieskring.setProvince(dummyProvince);

        List<Kieskring> kieskringen = Collections.singletonList(kieskring);

        // Mock the repository calls
        when(provinceRepository.existsById(provinceId)).thenReturn(true);
        when(kieskringRepository.findByProvinceId(provinceId)).thenReturn(kieskringen);

        // Act & Assert: Call the endpoint
        mockMvc.perform(get("/api/provinces/{id}/kieskringen", provinceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))             // Checks if DTO mapping (kieskring_id -> id) worked
                .andExpect(jsonPath("$[0].name").value("Amsterdam"))
                .andExpect(jsonPath("$[0].provinceName").value("North Holland")); // Checks if province name was fetched
    }

    @Test
    public void shouldReturnNotFoundForInvalidProvince() throws Exception {
        // Arrange
        int invalidId = 999;

        // Mock existsById to return false
        when(provinceRepository.existsById(invalidId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/provinces/{id}/kieskringen", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect 404
    }
}