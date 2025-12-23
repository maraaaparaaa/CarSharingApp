package com.carsharing.backend.controller;

import com.carsharing.backend.model.Ride;
import com.carsharing.backend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RideController {

    private final RideRepository rideRepository;

    // GET /api/rides
    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = rideRepository.findAll();
        return ResponseEntity.ok(rides);
    }

    // GET /api/rides/5 - get specific ride
    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable Long id) {
        return rideRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rides - creates new ride
    @PostMapping
    public ResponseEntity<Ride> createRide(@RequestBody Ride ride) {
        // sets available seats = all seats in the beginning
        ride.setAvailableSeats(ride.getTotalSeats());

        // implicit status
        if (ride.getStatus() == null) {
            ride.setStatus(Ride.RideStatus.ACTIVE);
        }

        Ride savedRide = rideRepository.save(ride);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRide);
    }

    // GET /api/rides/search?from=Cluj&to=Bucuresti
    @GetMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(
            @RequestParam String from,
            @RequestParam String to) {

        List<Ride> rides = rideRepository.findByStartLocationAndEndLocation(from, to);
        return ResponseEntity.ok(rides);
    }

    // GET /api/rides/driver/5 - rides of a certain driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getRidesByDriver(@PathVariable Long driverId) {
        List<Ride> rides = rideRepository.findByDriverId(driverId);
        return ResponseEntity.ok(rides);
    }

    // GET /api/rides/upcoming - future rides
    @GetMapping("/upcoming")
    public ResponseEntity<List<Ride>> getUpcomingRides() {
        LocalDateTime now = LocalDateTime.now();
        List<Ride> rides = rideRepository.findByDepartureTimeAfter(now);
        return ResponseEntity.ok(rides);
    }

    // PUT /api/rides/5 - update ride
    @PutMapping("/{id}")
    public ResponseEntity<Ride> updateRide(@PathVariable Long id, @RequestBody Ride rideDetails) {
        return rideRepository.findById(id)
                .map(ride -> {
                    ride.setStartLocation(rideDetails.getStartLocation());
                    ride.setEndLocation(rideDetails.getEndLocation());
                    ride.setDepartureTime(rideDetails.getDepartureTime());
                    ride.setTotalSeats(rideDetails.getTotalSeats());
                    ride.setPricePerSeat(rideDetails.getPricePerSeat());
                    ride.setCarModel(rideDetails.getCarModel());
                    ride.setCarColor(rideDetails.getCarColor());
                    ride.setDescription(rideDetails.getDescription());

                    Ride updatedRide = rideRepository.save(ride);
                    return ResponseEntity.ok(updatedRide);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/rides/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable Long id) {
        if (!rideRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        rideRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}