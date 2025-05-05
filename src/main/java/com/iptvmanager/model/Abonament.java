package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca abonament
 */
@Entity
@Table(name = "abonamenty")
@Data
@NoArgsConstructor
public class Abonament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypAbonamentu typAbonamentu;
    
    @Column(nullable = false)
    private LocalDate dataRozpoczecia;
    
    @Column
    private LocalDate dataZakonczenia;
    
    @Column(nullable = false)
    private boolean aktywny = true;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id")
    private Klient klient;
    
    @OneToMany(mappedBy = "abonament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subkonto> subkonta = new ArrayList<>();
    
    @OneToMany(mappedBy = "abonament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Naleznosc> naleznosci = new ArrayList<>();
    
    @OneToMany(mappedBy = "abonament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wplata> wplaty = new ArrayList<>();
    
    public Abonament(TypAbonamentu typAbonamentu, LocalDate dataRozpoczecia) {
        this.typAbonamentu = typAbonamentu;
        this.dataRozpoczecia = dataRozpoczecia;
    }
    
    /**
     * Dodaje subkonto do abonamentu
     */
    public void dodajSubkonto(Subkonto subkonto) {
        subkonta.add(subkonto);
        subkonto.setAbonament(this);
    }
    
    /**
     * Usuwa subkonto z abonamentu
     */
    public void usunSubkonto(Subkonto subkonto) {
        subkonta.remove(subkonto);
        subkonto.setAbonament(null);
    }
    
    /**
     * Dodaje należność do abonamentu
     */
    public void dodajNaleznosc(Naleznosc naleznosc) {
        naleznosci.add(naleznosc);
        naleznosc.setAbonament(this);
    }
    
    /**
     * Dodaje wpłatę do abonamentu
     */
    public void dodajWplate(Wplata wplata) {
        wplaty.add(wplata);
        wplata.setAbonament(this);
    }
}