package com.iptvmanager.service;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Klient;
import com.iptvmanager.model.TypAbonamentu;
import com.iptvmanager.repository.AbonamentRepository;
import com.iptvmanager.repository.KlientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonamentService {
    
    private final AbonamentRepository abonamentRepository;
    private final KlientRepository klientRepository;
    
    /**
     * Dodaje nowy abonament dla klienta
     */
    @Transactional
    public Abonament dodajAbonament(Long klientId, TypAbonamentu typAbonamentu, LocalDate dataRozpoczecia) {
        Klient klient = klientRepository.findById(klientId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono klienta o ID: " + klientId));
        
        Abonament abonament = new Abonament(typAbonamentu, dataRozpoczecia);
        klient.dodajAbonament(abonament);
        
        return abonamentRepository.save(abonament);
    }
    
    /**
     * Dezaktywuje abonament
     */
    @Transactional
    public Abonament dezaktywujAbonament(Long abonamentId, LocalDate dataZakonczenia) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        abonament.setAktywny(false);
        abonament.setDataZakonczenia(dataZakonczenia);
        
        return abonamentRepository.save(abonament);
    }
    
    /**
     * Zmienia typ abonamentu
     */
    @Transactional
    public Abonament zmienTypAbonamentu(Long abonamentId, TypAbonamentu nowyTyp) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        abonament.setTypAbonamentu(nowyTyp);
        
        return abonamentRepository.save(abonament);
    }
    
    /**
     * Pobiera wszystkie abonamenty klienta
     */
    public List<Abonament> pobierzAbonamentyKlienta(Long klientId) {
        Klient klient = klientRepository.findById(klientId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono klienta o ID: " + klientId));
        
        return abonamentRepository.findByKlient(klient);
    }
    
    /**
     * Pobiera abonament po ID
     */
    public Abonament znajdzAbonamentPoId(Long abonamentId) {
        return abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
    }
    
    /**
     * Pobiera wszystkie aktywne abonamenty
     */
    public List<Abonament> pobierzAktywneAbonamenty() {
        return abonamentRepository.findByAktywnyIsTrue();
    }
}