package nl.hva.elections.repositories;

import nl.hva.elections.models.Kieskring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface KieskringRepository extends JpaRepository<Kieskring, Integer> {

    boolean existsByName(String name);

    Optional<Kieskring> findByName(String name);

    List<Kieskring> findAllByOrderByNameAsc();

    @Query("SELECT k FROM Kieskring k WHERE k.province.province_id = :provinceId")
    List<Kieskring> findByProvinceId(Integer provinceId);
}