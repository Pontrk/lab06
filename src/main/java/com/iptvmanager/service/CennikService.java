package com.iptvmanager.service;

import com.iptvmanager.model.Cennik;
import com.iptvmanager.model.TypAbonamentu;
import com.iptvmanager.repository.CennikRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CennikService {
    
    private final CennikRepository cennikRepository;
    
    /**
     * Dodaje nową pozycję do cennika
     */
    @Transactional
    public Cennik dodajPozycjeCennika(TypAbonamentu typAbonamentu, BigDecimal cena, LocalDate dataOd) {
        // Dezaktywujemy wcześniejsze cenniki dla tego typu usługi
        List<Cennik> aktualnePozyje = cennikRepository.findByTypAbonamentu(typAbonamentu);
        for (Cennik pozycja : aktualnePozyje) {
            if (pozycja.isAktywny() && (pozycja.getDataDo() == null || !pozycja.getDataDo().isBefore(dataOd))) {
                pozycja.setDataDo(dataOd.minusDays(1));
                cennikRepository.save(pozycja);
            }
        }
        
        Cennik cennik = new Cennik(typAbonamentu, cena, dataOd);
        return cennikRepository.save(cennik);
    }
    
    /**
     * Dezaktywuje pozycję cennika
     */
    @Transactional
    public Cennik dezaktywujPozycjeCennika(Long cennikId, LocalDate dataDo) {
        Cennik cennik = cennikRepository.findById(cennikId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono pozycji cennika o ID: " + cennikId));
        
        cennik.setAktywny(false);
        cennik.setDataDo(dataDo);
        
        return cennikRepository.save(cennik);
    }
    
    /**
     * Pobiera aktualną cenę dla danego typu abonamentu
     */
    public BigDecimal pobierzAktualnaCene(TypAbonamentu typAbonamentu) {
        LocalDate dzisiaj = LocalDate.now();
        Cennik cennik = cennikRepository.findAktualnyDlaTypu(typAbonamentu, dzisiaj)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono aktualnej ceny dla typu: " + typAbonamentu));
        
        return cennik.getCena();
    }
    
    /**
     * Pobiera aktualną cenę dla danego typu abonamentu na podaną datę
     */
    public BigDecimal pobierzCeneNaDate(TypAbonamentu typAbonamentu, LocalDate data) {
        Cennik cennik = cennikRepository.findAktualnyDlaTypu(typAbonamentu, data)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono ceny dla typu: " + typAbonamentu + " na datę: " + data));
        
        return cennik.getCena();
    }
    
    /**
     * Pobiera wszystkie aktywne pozycje cennika
     */
    public List<Cennik> pobierzAktualnyCennik() {
        return cennikRepository.findByAktywnyIsTrue();
    }
    
    /**
     * Pobiera wszystkie pozycje cennika
     */
    public List<Cennik> pobierzWszystkiePozycjeCennika() {
        return cennikRepository.findAll();
    }
}