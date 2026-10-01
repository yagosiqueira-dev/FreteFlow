package br.com.freteflow.service;

import br.com.freteflow.dto.expense.ExpenseRequestDTO;
import br.com.freteflow.dto.expense.ExpenseResponseDTO;
import br.com.freteflow.entity.Expense;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.exception.VehicleNotFoundException;
import br.com.freteflow.repository.ExpenseRepository;
import br.com.freteflow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        Expense expense = Expense.builder()
                .vehicle(vehicle)
                .description(request.description())
                .amount(request.amount())
                .expenseDate(request.expenseDate())
                .build();

        Expense saved = expenseRepository.save(expense);

        return ExpenseResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> listExpensesByVehicle(UUID vehicleId) {
        return expenseRepository.findByVehicleId(vehicleId).stream()
                .map(ExpenseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDTO> listExpenses(
            UUID vehicleId, String description, LocalDate startDate, LocalDate endDate, org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Expense> spec = org.springframework.data.jpa.domain.Specification
                .where(br.com.freteflow.repository.specification.ExpenseSpecification.vehicleIdEquals(vehicleId))
                .and(br.com.freteflow.repository.specification.ExpenseSpecification.descriptionEquals(description))
                .and(br.com.freteflow.repository.specification.ExpenseSpecification.expenseDateBetween(startDate, endDate));

        return expenseRepository.findAll(spec, pageable).map(ExpenseResponseDTO::fromEntity);
    }

    @Transactional
    public void deactivateExpense(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));
        expense.setEnabled(false);
        expenseRepository.save(expense);
    }

    @Transactional
    public ExpenseResponseDTO activateExpense(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));
        expense.setEnabled(true);
        Expense updated = expenseRepository.save(expense);
        return ExpenseResponseDTO.fromEntity(updated);
    }
    @Transactional
    public ExpenseResponseDTO updateExpense(UUID id, ExpenseRequestDTO request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada com ID: " + id));

        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());

        if (request.vehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                    .orElseThrow(() -> new RuntimeException("Veículo não encontrado com ID: " + request.vehicleId()));
            expense.setVehicle(vehicle);
        }

        Expense updated = expenseRepository.save(expense);

        // Retorna o DTO de resposta (confirme se a ordem dos parâmetros do construtor bate com o seu DTO)
        return new ExpenseResponseDTO(
                updated.getId(),
                updated.getVehicle().getId(),
                updated.getDescription(),
                updated.getAmount(),
                updated.getExpenseDate(),
                updated.isEnabled(),
                updated.getCreatedAt()
        );
    }
}