package dk.ek.eksamensprojekt3semester.kommune;


import dk.ek.Beskyttelsesrum.Beskyttelsesrum;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Kommune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String navn;

    @OneToMany(mappedBy = "kommune", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Beskyttelsesrum> beskyttelsesrum;

    // Getters and Setters
}

}

