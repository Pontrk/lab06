package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca klienta
 */
@Entity
@Table(name = "klienci")
@Data
@NoArgsConstructor
public class Klient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String imie;
    
    @Column(nullable = false)
    private String nazwisko;
    
    @Column(name = "numer_klienta", nullable = false, unique = true)
    private String numerKlienta;
    
    @OneToMany(mappedBy = "klient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Abonament> abonamenty = new ArrayList<>();
    
    public Klient(String imie, String nazwisko, String numerKlienta) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.numerKlienta = numerKlienta;
    }
    
    /**
     * Dodaje abonament do klienta
     */
    public void dodajAbonament(Abonament abonament) {
        abonamenty.add(abonament);
        abonament.setKlient(this);
    }
    
    /**
     * Usuwa abonament klienta
     */
    public void usunAbonament(Abonament abonament) {
        abonamenty.remove(abonament);
        abonament.setKlient(null);
    }
}