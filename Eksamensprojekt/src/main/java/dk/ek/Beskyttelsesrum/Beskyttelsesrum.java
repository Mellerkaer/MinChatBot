package dk.ek.Beskyttelsesrum;

import dk.ek.Kommune.Kommune;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Beskyttelsesrum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String navn;
    private int kapacitet;

    @ManyToOne
    @JoinColumn(name = "kommune_id")
    private Kommune kommune;

    // Getters and Setters
}