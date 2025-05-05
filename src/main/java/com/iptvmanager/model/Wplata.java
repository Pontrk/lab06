package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encja reprezentująca wpłatę za abonament
 */
@Entity
@Table(name = "wplaty")
@Data
@NoArgsConstructor
public class Wplata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "data_wplaty", nullable = false)
    private LocalDate dataWplaty;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kwota;
    
    @Column
    private String opis;
    
    @Column(name = "czy_korekta")
    private boolean czyKorekta = false;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonament_id")
    private Abonament abonament;
    
    public Wplata(LocalDate dataWplaty, BigDecimal kwota) {
        this.dataWplaty = dataWplaty;
        this.kwota = kwota;
    }
    
    public Wplata(LocalDate dataWplaty, BigDecimal kwota, boolean czyKorekta, String opis) {
        this.dataWplaty = dataWplaty;
        this.kwota = kwota;
        this.czyKorekta = czyKorekta;
        this.opis = opis;
    }
}