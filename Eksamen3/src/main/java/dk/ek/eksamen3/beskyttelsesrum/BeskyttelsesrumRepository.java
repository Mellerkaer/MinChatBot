package dk.ek.eksamen3.beskyttelsesrum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeskyttelsesrumRepository extends JpaRepository<Beskyttelsesrum, Long> {

    // Returnerer en liste af Beskyttelsesrum ud fra KommuneID
    List<Beskyttelsesrum> findByKommuneId(long kommuneId);
}
