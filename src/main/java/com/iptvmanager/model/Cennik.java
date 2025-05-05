package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encja reprezentująca pozycję w cenniku
 */
@Entity
@Table(name = "cennik")
@Data
@NoArgsConstructor
public class Cennik {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "typ_abonamentu", nullable = false)
    private TypAbonamentu typAbonamentu;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cena;
    
    @Column(name = "data_od", nullable = false)
    private LocalDate dataOd;
    
    @Column(name = "data_do")
    private LocalDate dataDo;
    
    @Column(nullable = false)
    private boolean aktywny = true;
    
    public Cennik(TypAbonamentu typAbonamentu, BigDecimal cena, LocalDate dataOd) {
        this.typAbonamentu = typAbonamentu;
        this.cena = cena;
        this.dataOd = dataOd;
    }
    
    /**
     * Sprawdza czy cennik jest aktualny na podaną datę
     */
    public boolean jestAktualnyNaDate(LocalDate data) {
        return aktywny && !data.isBefore(dataOd) && (dataDo == null || !data.isAfter(dataDo));
    }
}