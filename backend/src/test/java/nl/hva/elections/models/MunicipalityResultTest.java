package nl.hva.elections.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MunicipalityResultTest {

    @Test
    void constructor_ShouldInitializeFieldsCorrectly() {
        // Arrange & Act
        MunicipalityResult result = new MunicipalityResult("Amsterdam", "Partij A", 500);

        // Assert
        assertEquals("Amsterdam", result.getMunicipalityName());
        assertEquals("Partij A", result.getPartyName());
        assertEquals(500, result.getValidVotes());
    }

    @Test
    void setters_ShouldUpdateFieldsCorrectly() {
        // Arrange
        MunicipalityResult result = new MunicipalityResult("Old City", "Old Party", 10);

        // Act
        result.setMunicipalityName("New City");
        result.setPartyName("New Party");
        result.setValidVotes(99);

        // Assert
        assertEquals("New City", result.getMunicipalityName());
        assertEquals("New Party", result.getPartyName());
        assertEquals(99, result.getValidVotes());
    }

    @Test
    void hashCode_ShouldBeEqualForSameData() {
        // Arrange: Create two distinct objects with identical data
        MunicipalityResult result1 = new MunicipalityResult("Utrecht", "GL", 1200);
        MunicipalityResult result2 = new MunicipalityResult("Utrecht", "GL", 1200);

        // Act & Assert
        // Since you overrode hashCode, these MUST match
        assertEquals(result1.hashCode(), result2.hashCode(), "Hash codes should match for identical data");
    }

    @Test
    void hashCode_ShouldBeDifferentForDifferentData() {
        // Arrange
        MunicipalityResult result1 = new MunicipalityResult("Utrecht", "GL", 1200);
        MunicipalityResult result2 = new MunicipalityResult("Utrecht", "VVD", 500);

        // Act & Assert
        assertNotEquals(result1.hashCode(), result2.hashCode(), "Hash codes should differ for different data");
    }
}