package nl.hva.elections.dtos;

// A simple container. No @Entity, no @Table.
public class KieskringDTO {

    private Integer id;
    private String name;

    // Instead of the whole Province object, we just send the name!
    private String provinceName;

    // Constructor to easily convert from Model -> DTO
    public KieskringDTO(Integer id, String name, String provinceName) {
        this.id = id;
        this.name = name;
        this.provinceName = provinceName;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
}