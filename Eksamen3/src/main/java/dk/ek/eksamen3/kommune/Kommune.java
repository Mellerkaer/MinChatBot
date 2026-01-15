package dk.ek.eksamen3.kommune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.ek.eksamen3.beskyttelsesrum.Beskyttelsesrum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kommune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String navn;

    @OneToMany(mappedBy = "kommune", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Beskyttelsesrum> beskyttelsesrum;

}
