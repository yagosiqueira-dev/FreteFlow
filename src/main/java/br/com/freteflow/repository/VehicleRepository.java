package br.com.freteflow.repository;

import br.com.freteflow.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {


    boolean existsByLicensePlate(String licensePlate);


    Optional<Vehicle> findByLicensePlate(String licensePlate);
}