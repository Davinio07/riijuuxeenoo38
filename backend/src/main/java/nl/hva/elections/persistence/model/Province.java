package nl.hva.elections.persistence.model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PROVINCE")
@XmlRootElement(name = "Province")
@XmlAccessorType(XmlAccessType.FIELD)
public class Province {

    @Id
    @Column(name = "province_id")
    private Integer province_id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // --- RELATIONSHIP ---
    // mappedBy="province" refers to the private variable name in the Kieskring class
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Kieskring> kieskringen = new ArrayList<>();
    // --- END RELATIONSHIP ---

    /**
     * JPA requires a no-argument constructor.
     */
    public Province() {
    }

    /**
     * Constructor used for seeding the data.
     * @param province_id The manual ID (1-12)
     * @param name The name of the province
     */
    public Province(Integer province_id, String name) {
        this.province_id = province_id;
        this.name = name;
    }

    // --- GETTERS AND SETTERS (INCLUDING THE MISSING ONES) ---

    public Integer getProvince_id() {
        return province_id;
    }

    public void setProvince_id(Integer province_id) {
        this.province_id = province_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Kieskring> getKieskringen() {
        return kieskringen;
    }

    public void setKieskringen(List<Kieskring> kieskringen) {
        this.kieskringen = kieskringen;
    }

    // --- END GETTERS AND SETTERS ---

    @Override
    public String toString() {
        // IMPORTANT: Do NOT include the 'kieskringen' list in toString()
        // or you will trigger an infinite loop / stack overflow
        return "Province{" +
                "province_id=" + province_id +
                ", name='" + name + '\'' +
                '}';
    }
}