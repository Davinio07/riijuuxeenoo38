package nl.hva.elections.xml.model;

/**
 * Model class representing a political party, adapted to be more compatible
 * with XML parsing frameworks (like JAXB) which typically require
 * a default constructor and mutable fields (non-final).
 */
public class Party {
    private String id;
    private String name;

    /**
     * Default constructor required for many persistence/serialization frameworks.
     */
    public Party() {
        // Initialize fields if necessary, or leave for setters
    }

    public Party(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Party{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
