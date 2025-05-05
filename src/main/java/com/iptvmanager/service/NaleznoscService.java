package com.iptvmanager.service;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Naleznosc;
import com.iptvmanager.model.TypAbonamentu;
import com.iptvmanager.repository.AbonamentRepository;
import com.iptvmanager.repository.NaleznoscRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaleznoscService {
    
    private final NaleznoscRepository naleznoscRepository;
    private final AbonamentRepository abonamentRepository;
    private final CennikService cennikService;
    
    /**
     * Nalicza należność dla abonamentu
     */
    @Transactional
    public Naleznosc naliczNaleznoscAbonamentu(Long abonamentId, LocalDate terminPlatnosci, String okresRozliczeniowy) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        if (!abonament.isAktywny()) {
            throw new IllegalStateException("Nie można naliczyć należności dla nieaktywnego abonamentu");
        }
        
        // Sprawdź czy należność już istnieje
        Optional<Naleznosc> istniejacaNaleznosc = naleznoscRepository.findByAbonamentAndOkresRozliczeniowy(
                abonament, okresRozliczeniowy);
        
        if (istniejacaNaleznosc.isPresent()) {
            throw new IllegalStateException("Należność za okres " + okresRozliczeniowy + " już istnieje");
        }
        
        TypAbonamentu typAbonamentu = abonament.getTypAbonamentu();
        BigDecimal kwota = cennikService.pobierzCeneNaDate(typAbonamentu, LocalDate.now());
        
        Naleznosc naleznosc = new Naleznosc(terminPlatnosci, kwota, okresRozliczeniowy);
        abonament.dodajNaleznosc(naleznosc);
        
        return naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Automatycznie nalicza należności dla wszystkich aktywnych abonamentów
     */
    @Transactional
    public void automatyczneNaliczanieNaleznosci(LocalDate data) {
        String okresRozliczeniowy = data.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate terminPlatnosci = data.plusDays(14); // 14 dni na zapłatę
        
        List<Abonament> aktywneAbonamenty = abonamentRepository.findByAktywnyIsTrue();
        log.info("Rozpoczęto automatyczne naliczanie należności za okres {} dla {} abonamentów", 
                okresRozliczeniowy, aktywneAbonamenty.size());
        
        for (Abonament abonament : aktywneAbonamenty) {
            try {
                // Sprawdź czy należność już istnieje
                Optional<Naleznosc> istniejacaNaleznosc = naleznoscRepository.findByAbonamentAndOkresRozliczeniowy(
                        abonament, okresRozliczeniowy);
                
                if (istniejacaNaleznosc.isPresent()) {
                    log.debug("Należność za okres {} dla abonamentu ID: {} już istnieje", 
                            okresRozliczeniowy, abonament.getId());
                    continue;
                }
                
                TypAbonamentu typAbonamentu = abonament.getTypAbonamentu();
                BigDecimal kwota = cennikService.pobierzCeneNaDate(typAbonamentu, data);
                
                Naleznosc naleznosc = new Naleznosc(terminPlatnosci, kwota, okresRozliczeniowy);
                abonament.dodajNaleznosc(naleznosc);
                naleznoscRepository.save(naleznosc);
                
                log.debug("Naliczono należność za okres {} dla abonamentu ID: {}, kwota: {}", 
                        okresRozliczeniowy, abonament.getId(), kwota);
            } catch (Exception e) {
                log.error("Błąd podczas naliczania należności dla abonamentu ID: " + abonament.getId(), e);
            }
        }
        
        log.info("Zakończono automatyczne naliczanie należności za okres {}", okresRozliczeniowy);
    }
    
    /**
     * Oznacza należność jako zapłaconą
     */
    @Transactional
    public Naleznosc oznaczJakoOplacona(Long naleznoscId) {
        Naleznosc naleznosc = naleznoscRepository.findById(naleznoscId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono należności o ID: " + naleznoscId));
        
        naleznosc.setOplacona(true);
        
        return naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Koryguje kwotę należności
     */
    @Transactional
    public Naleznosc korygujKwoteNaleznosci(Long naleznoscId, BigDecimal nowaKwota) {
        Naleznosc naleznosc = naleznoscRepository.findById(naleznoscId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono należności o ID: " + naleznoscId));
        
        naleznosc.setKwota(nowaKwota);
        
        log.info("Skorygowano kwotę należności ID: {} do wartości: {}", naleznoscId, nowaKwota);
        
        return naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Pobiera wszystkie należności dla abonamentu
     */
    public List<Naleznosc> pobierzNaleznosciAbonamentu(Long abonamentId) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        return naleznoscRepository.findByAbonament(abonament);
    }
    
    /**
     * Pobiera niezapłacone należności dla abonamentu
     */
    public List<Naleznosc> pobierzNiezaplaconeNaleznosciAbonamentu(Long abonamentId) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        return naleznoscRepository.findByAbonamentAndOplaconaIsFalse(abonament);
    }
    
    /**
     * Znajdź należność po ID
     */
    public Naleznosc znajdzNaleznoscPoId(Long naleznoscId) {
        return naleznoscRepository.findById(naleznoscId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono należności o ID: " + naleznoscId));
    }
    
    /**
     * Pobiera wszystkie niezapłacone należności po terminie płatności
     */
    public List<Naleznosc> pobierzPrzeterminowaneNaleznosci(LocalDate data) {
        return naleznoscRepository.findNiezaplaconePoTerminie(data);
    }
}