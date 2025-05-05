package com.iptvmanager.service;

import com.iptvmanager.model.Klient;
import com.iptvmanager.repository.KlientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KlientService {
    
    private final KlientRepository klientRepository;
    
    /**
     * Zwraca wszystkich klientów
     */
    public List<Klient> pobierzWszystkichKlientow() {
        return klientRepository.findAll();
    }
    
    /**
     * Dodaje nowego klienta
     */
    @Transactional
    public Klient dodajKlienta(String imie, String nazwisko, String numerKlienta) {
        if (klientRepository.existsByNumerKlienta(numerKlienta)) {
            throw new IllegalArgumentException("Klient o podanym numerze już istnieje");
        }
        
        Klient klient = new Klient(imie, nazwisko, numerKlienta);
        return klientRepository.save(klient);
    }
    
    /**
     * Aktualizuje dane klienta
     */
    @Transactional
    public Klient aktualizujKlienta(Long id, String imie, String nazwisko) {
        Klient klient = klientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono klienta o ID: " + id));
        
        klient.setImie(imie);
        klient.setNazwisko(nazwisko);
        
        return klientRepository.save(klient);
    }
    
    /**
     * Wyszukuje klienta po ID
     */
    public Klient znajdzKlientaPoId(Long id) {
        return klientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono klienta o ID: " + id));
    }
    
    /**
     * Wyszukuje klienta po numerze
     */
    public Klient znajdzKlientaPoNumerze(String numerKlienta) {
        return klientRepository.findByNumerKlienta(numerKlienta)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono klienta o numerze: " + numerKlienta));
    }
    
    /**
     * Usuwa klienta
     */
    @Transactional
    public void usunKlienta(Long id) {
        if (!klientRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono klienta o ID: " + id);
        }
        klientRepository.deleteById(id);
    }
}