package dk.ek.eksamen3.beskyttelsesrum;


import dk.ek.eksamen3.kommune.Kommune;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Beskyttelsesrum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String navn;
    private int kapacitet;

    @ManyToOne
    @JoinColumn(name = "kommune_id")
    private Kommune kommune;


}
