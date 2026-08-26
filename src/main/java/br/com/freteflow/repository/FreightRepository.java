package br.com.freteflow.repository;

import br.com.freteflow.entity.Freight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FreightRepository extends JpaRepository<Freight, UUID> {

    List<Freight> findByDriverIdAndFreightDateBetweenOrderByFreightDateAsc(UUID driverId, LocalDateTime startDate, LocalDateTime endDate);
}