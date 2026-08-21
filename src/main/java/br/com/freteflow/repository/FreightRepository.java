package br.com.freteflow.repository;

import br.com.freteflow.entity.Freight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FreightRepository extends JpaRepository<Freight, UUID> {
}