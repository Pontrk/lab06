package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encja reprezentująca należność za abonament
 */
@Entity
@Table(name = "naleznosci")
@Data
@NoArgsConstructor
public class Naleznosc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate terminPlatnosci;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kwota;
    
    @Column(nullable = false)
    private boolean oplacona = false;
    
    @Column(name = "data_wystawienia", nullable = false)
    private LocalDate dataWystawienia;
    
    @Column(name = "okres_rozliczeniowy", nullable = false)
    private String okresRozliczeniowy; // np. "2023-05" dla maja 2023
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_monitu")
    private StatusMonitu statusMonitu = StatusMonitu.BRAK;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonament_id")
    private Abonament abonament;
    
    public Naleznosc(LocalDate terminPlatnosci, BigDecimal kwota, String okresRozliczeniowy) {
        this.terminPlatnosci = terminPlatnosci;
        this.kwota = kwota;
        this.okresRozliczeniowy = okresRozliczeniowy;
        this.dataWystawienia = LocalDate.now();
    }
    
    /**
     * Enum reprezentujący status monitu dotyczącego należności
     */
    public enum StatusMonitu {
        BRAK,
        MONIT_PIERWSZY,
        MONIT_DRUGI,
        MONIT_TRZECI,
        WINDYKACJA
    }
}