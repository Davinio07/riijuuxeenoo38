package nl.hva.elections.persistence.model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "KIESKRING")
@XmlRootElement(name = "Kieskring")
@XmlAccessorType(XmlAccessType.FIELD)
public class Kieskring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kieskring_id")
    private Integer kieskring_id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // --- RELATIONSHIP ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id") // This creates the foreign key column
    @XmlTransient // Prevents XML circular reference issues
    private Province province;
    // --- END RELATIONSHIP ---

    /**
     * JPA requires a no-argument constructor.
     */
    public Kieskring() {
    }

    /**
     * Constructor used for seeding the data.
     * @param name The name of the kieskring
     */
    public Kieskring(String name) {
        this.name = name;
    }

    // --- GETTERS AND SETTERS (INCLUDING THE MISSING ONES) ---

    public Integer getKieskring_id() {
        return kieskring_id;
    }

    public void setKieskring_id(Integer kieskring_id) {
        this.kieskring_id = kieskring_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    // --- END GETTERS AND SETTERS ---

    @Override
    public String toString() {
        // IMPORTANT: Do not include 'province' in toString() to avoid infinite loops
        return "Kieskring{" +
                "kieskring_id=" + kieskring_id +
                ", name='" + name + '\'' +
                '}';
    }
}