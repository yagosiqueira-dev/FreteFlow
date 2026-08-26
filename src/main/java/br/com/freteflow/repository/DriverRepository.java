package br.com.freteflow.repository;

import br.com.freteflow.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByCpf(String cpf);

    Optional<Driver> findByCpf(String cpf);
    Page<Driver> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
