package br.com.freteflow.controller;

import br.com.freteflow.dto.expense.ExpenseRequestDTO;
import br.com.freteflow.dto.expense.ExpenseResponseDTO;
import br.com.freteflow.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDTO>> list(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "expenseDate") org.springframework.data.domain.Pageable pageable) {

        Page<ExpenseResponseDTO> expenses = expenseService.listExpenses(vehicleId, description, startDate, endDate, pageable);
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        expenseService.deactivateExpense(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ExpenseResponseDTO> activate(@PathVariable UUID id) {
        ExpenseResponseDTO activated = expenseService.activateExpense(id);
        return ResponseEntity.ok(activated);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ExpenseResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequestDTO request) {
        ExpenseResponseDTO updated = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(updated);
    }
}