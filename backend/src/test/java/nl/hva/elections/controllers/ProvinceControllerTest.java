package nl.hva.elections.controllers;

import nl.hva.elections.OnStartUp.DataSeeder;
import nl.hva.elections.Service.dbPartyService;
import nl.hva.elections.repositories.KieskringRepository;
import nl.hva.elections.repositories.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import nl.hva.elections.controllers.ProvinceController;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ProvinceControllerTest {
    private MockMvc mockMvc;

    @Mock
    private KieskringRepository kieskringRepository;
    @Mock
    private ProvinceRepository provinceRepository;


    @BeforeEach
    void setUp() {
        ProvinceController controller = new ProvinceController(provinceRepository, kieskringRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void ProvinceControllerTest() {

    }
}
