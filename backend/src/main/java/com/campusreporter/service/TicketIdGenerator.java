package com.campusreporter.service;

import com.campusreporter.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TicketIdGenerator {

    private final ComplaintRepository complaintRepository;
    private final AtomicLong counter = new AtomicLong(0);
    private volatile boolean initialized = false;

    public TicketIdGenerator(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            try {
                Long maxId = complaintRepository.findMaxId();
                if (maxId != null && maxId > 0) {
                    counter.set(maxId);
                } else {
                    counter.set(0);
                }
            } catch (Exception e) {
                counter.set(System.currentTimeMillis() % 100000);
            }
            initialized = true;
        }
    }

    /**
     * Concurrency-safe ticket ID generator
     * Format: SCR-YYYY-XXXXXX (e.g. SCR-2026-000001)
     */
    public String generateTicketId() {
        ensureInitialized();
        int year = LocalDate.now().getYear();

        String ticketId;
        do {
            long nextVal = counter.incrementAndGet();
            ticketId = String.format("SCR-%d-%06d", year, nextVal);
        } while (complaintRepository.existsByTicketId(ticketId));

        return ticketId;
    }
}
