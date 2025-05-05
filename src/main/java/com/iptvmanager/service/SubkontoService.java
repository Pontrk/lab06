package com.iptvmanager.service;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Subkonto;
import com.iptvmanager.repository.AbonamentRepository;
import com.iptvmanager.repository.SubkontoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubkontoService {
    
    private final SubkontoRepository subkontoRepository;
    private final AbonamentRepository abonamentRepository;
    
    /**
     * Dodaje nowe subkonto do abonamentu
     */
    @Transactional
    public Subkonto dodajSubkonto(Long abonamentId, String login, String haslo) {
        if (subkontoRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Subkonto o podanym loginie już istnieje");
        }
        
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        if (!abonament.isAktywny()) {
            throw new IllegalStateException("Nie można dodać subkonta do nieaktywnego abonamentu");
        }
        
        Subkonto subkonto = new Subkonto(login, haslo);
        abonament.dodajSubkonto(subkonto);
        
        return subkontoRepository.save(subkonto);
    }
    
    /**
     * Zmienia hasło do subkonta
     */
    @Transactional
    public Subkonto zmienHaslo(Long subkontoId, String noweHaslo) {
        Subkonto subkonto = subkontoRepository.findById(subkontoId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono subkonta o ID: " + subkontoId));
        
        subkonto.setHaslo(noweHaslo);
        
        return subkontoRepository.save(subkonto);
    }
    
    /**
     * Dezaktywuje subkonto
     */
    @Transactional
    public Subkonto dezaktywujSubkonto(Long subkontoId) {
        Subkonto subkonto = subkontoRepository.findById(subkontoId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono subkonta o ID: " + subkontoId));
        
        subkonto.setAktywne(false);
        
        return subkontoRepository.save(subkonto);
    }
    
    /**
     * Pobiera subkonta dla abonamentu
     */
    public List<Subkonto> pobierzSubkontaAbonamentu(Long abonamentId) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        return subkontoRepository.findByAbonament(abonament);
    }
    
    /**
     * Znajdź subkonto po ID
     */
    public Subkonto znajdzSubkontoPoId(Long subkontoId) {
        return subkontoRepository.findById(subkontoId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono subkonta o ID: " + subkontoId));
    }
}