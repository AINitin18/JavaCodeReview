package com.yourorg.billing; // ← change to your package

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** Lightweight in-memory service used for requirement validation demo. */
public class InvoiceService {
    private final Map<Long, Invoice> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    // REQ-INV-001: Create invoice business logic
    public Invoice create(String customerName, BigDecimal amount) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName is required");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        Invoice inv = new Invoice(seq.getAndIncrement(), customerName, amount, InvoiceStatus.DRAFT, Instant.now());
        store.put(inv.id, inv);
        return inv;
    }

    // REQ-INV-002: Get invoice by id
    public Invoice get(Long id) { return store.get(id); }

    // REQ-INV-003: List & filter invoices
    public List<Invoice> list(String status, String customerLike) {
        InvoiceStatus st = parseStatus(status);
        String needle = customerLike == null ? null : customerLike.toLowerCase();
        return store.values().stream()
                .filter(i -> st == null || i.status == st)
                .filter(i -> needle == null || i.customerName.toLowerCase().contains(needle))
                .sorted(Comparator.comparing(i -> i.createdAt))
                .collect(Collectors.toList());
    }

    // REQ-INV-004: Update invoice status
    public Invoice updateStatus(Long id, String status) {
        Invoice inv = store.get(id);
        if (inv == null) return null;
        InvoiceStatus st = parseStatus(status);
        if (st == null) throw new IllegalArgumentException("invalid status");
        inv.status = st;
        store.put(inv.id, inv);
        return inv;
    }

    private InvoiceStatus parseStatus(String s) {
        if (s == null) return null;
        try { return InvoiceStatus.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    // --- Minimal data types kept here to stay within 2 files ---
    public enum InvoiceStatus { DRAFT, SENT, PAID, CANCELLED }

    public static class Invoice {
        public Long id;
        public String customerName;
        public BigDecimal amount;
        public InvoiceStatus status;
        public Instant createdAt;
        public Invoice() {}
        public Invoice(Long id, String customerName, BigDecimal amount, InvoiceStatus status, Instant createdAt) {
            this.id = id; this.customerName = customerName; this.amount = amount; this.status = status; this.createdAt = createdAt;
        }
    }
}
