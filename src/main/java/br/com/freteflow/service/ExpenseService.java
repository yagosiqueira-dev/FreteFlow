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
}