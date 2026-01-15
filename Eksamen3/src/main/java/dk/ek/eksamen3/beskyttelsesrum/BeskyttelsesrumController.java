package dk.ek.eksamen3.beskyttelsesrum;

import dk.ek.eksamen3.kommune.Kommune;
import dk.ek.eksamen3.kommune.KommuneRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class BeskyttelsesrumController {

    private final BeskyttelsesrumRepository beskyttelsesrumRepository;
    private final KommuneRepository kommuneRepository;

    public BeskyttelsesrumController(BeskyttelsesrumRepository beskyttelsesrumRepository, KommuneRepository kommuneRepository) {
        this.beskyttelsesrumRepository = beskyttelsesrumRepository;
        this.kommuneRepository = kommuneRepository;
    }

    // POST /rooms - Create a new shelter
    @PostMapping("/rooms")
    public ResponseEntity<Beskyttelsesrum> createRoom(@RequestBody Beskyttelsesrum room) {
        if (room.getKommune() == null || room.getKommune().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Kommune> kommuneOpt = kommuneRepository.findById(room.getKommune().getId());
        if (kommuneOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        room.setKommune(kommuneOpt.get());
        return ResponseEntity.ok(beskyttelsesrumRepository.save(room));
    }

    // PUT /rooms/{roomId} - Update a shelter
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<Beskyttelsesrum> updateRoom(@PathVariable Long roomId, @RequestBody Beskyttelsesrum updatedRoom) {
        Optional<Beskyttelsesrum> existingRoomOpt = beskyttelsesrumRepository.findById(roomId);
        if (existingRoomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Beskyttelsesrum existingRoom = existingRoomOpt.get();
        existingRoom.setNavn(updatedRoom.getNavn());
        existingRoom.setKapacitet(updatedRoom.getKapacitet());

        if (updatedRoom.getKommune() != null && updatedRoom.getKommune().getId() != null) {
            Optional<Kommune> kommuneOpt = kommuneRepository.findById(updatedRoom.getKommune().getId());
            kommuneOpt.ifPresent(existingRoom::setKommune);
        }

        return ResponseEntity.ok(beskyttelsesrumRepository.save(existingRoom));
    }

    // GET /kommuner/{kommuneId}/rooms - Get all shelters for a kommune
    @GetMapping("/kommuner/{kommuneId}/rooms")
    public ResponseEntity<List<Beskyttelsesrum>> getRoomsByKommune(@PathVariable Long kommuneId) {
        List<Beskyttelsesrum> rooms = beskyttelsesrumRepository.findByKommuneId(kommuneId);
        return ResponseEntity.ok(rooms);
    }

    // DELETE /rooms/{roomId} - Delete a shelter
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        if (!beskyttelsesrumRepository.existsById(roomId)) {
            return ResponseEntity.notFound().build();
        }

        beskyttelsesrumRepository.deleteById(roomId);
        return ResponseEntity.noContent().build();
    }
}
