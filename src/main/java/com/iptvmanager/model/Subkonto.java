package com.iptvmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Encja reprezentująca subkonto użytkownika
 */
@Entity
@Table(name = "subkonta")
@Data
@NoArgsConstructor
public class Subkonto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    @Column(nullable = false)
    private String haslo;
    
    @Column(nullable = false)
    private boolean aktywne = true;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonament_id")
    private Abonament abonament;
    
    public Subkonto(String login, String haslo) {
        this.login = login;
        this.haslo = haslo;
    }
}