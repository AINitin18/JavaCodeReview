package com.yourorg.billing; // ← change to your package

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {
    // For the demo we new-up the service. In a real app, add @Service on InvoiceService and inject it.
    private final InvoiceService service = new InvoiceService();

    // REQ-INV-001: Create Invoice (POST)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String customerName = Objects.toString(body.get("customerName"), null);
            BigDecimal amount = parseAmount(body.get("amount"));
            InvoiceService.Invoice inv = service.create(customerName, amount);
            return ResponseEntity.status(HttpStatus.CREATED).body(inv);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // REQ-INV-002: Get Invoice by ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        InvoiceService.Invoice inv = service.get(id);
        return (inv == null) ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found") : ResponseEntity.ok(inv);
    }

    // REQ-INV-003: List & Filter (GET)
    @GetMapping
    public ResponseEntity<List<InvoiceService.Invoice>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "customer") String customerName) {
        return ResponseEntity.ok(service.list(status, customerName));
    }

    // REQ-INV-004: Update Invoice Status (PATCH)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object val = body.get("status");
        String status = val == null ? null : val.toString();
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body("status is required");
        }
        try {
            InvoiceService.Invoice inv = service.updateStatus(id, status);
            if (inv == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            return ResponseEntity.ok(inv);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    private BigDecimal parseAmount(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(o.toString());
    }
}
