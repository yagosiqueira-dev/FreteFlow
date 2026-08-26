package br.com.freteflow.controller;

import br.com.freteflow.dto.expense.ExpenseRequestDTO;
import br.com.freteflow.dto.expense.ExpenseResponseDTO;
import br.com.freteflow.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody ExpenseRequestDTO request) {
        ExpenseResponseDTO created = expenseService.createExpense(request);
        URI location = URI.create("/api/expenses/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ExpenseResponseDTO>> listByVehicle(@PathVariable UUID vehicleId) {
        List<ExpenseResponseDTO> expenses = expenseService.listExpensesByVehicle(vehicleId);
        return ResponseEntity.ok(expenses);
    }
}