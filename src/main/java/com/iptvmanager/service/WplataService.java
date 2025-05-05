package com.iptvmanager.service;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Naleznosc;
import com.iptvmanager.model.Wplata;
import com.iptvmanager.repository.AbonamentRepository;
import com.iptvmanager.repository.NaleznoscRepository;
import com.iptvmanager.repository.WplataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WplataService {
    
    private final WplataRepository wplataRepository;
    private final AbonamentRepository abonamentRepository;
    private final NaleznoscRepository naleznoscRepository;
    
    /**
     * Rejestruje nową wpłatę dla abonamentu
     */
    @Transactional
    public Wplata zarejestrujWplate(Long abonamentId, BigDecimal kwota, LocalDate dataWplaty) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        Wplata wplata = new Wplata(dataWplaty, kwota);
        abonament.dodajWplate(wplata);
        wplataRepository.save(wplata);
        
        rozliczWplate(abonament, kwota);
        
        log.info("Zarejestrowano wpłatę dla abonamentu ID: {}, kwota: {}, data: {}", 
                abonamentId, kwota, dataWplaty);
        
        return wplata;
    }
    
    /**
     * Dodaje korektę wpłaty
     */
    @Transactional
    public Wplata dodajKorekteWplaty(Long abonamentId, BigDecimal kwota, LocalDate dataKorekty, String opis) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        Wplata korekta = new Wplata(dataKorekty, kwota, true, opis);
        abonament.dodajWplate(korekta);
        wplataRepository.save(korekta);
        
        // Jeśli korekta jest ujemna (zmniejszenie wpłaty), to aktualizujemy status należności
        if (kwota.compareTo(BigDecimal.ZERO) < 0) {
            aktualizujStatusNaleznosciPoKorekcie(abonament);
        } else {
            rozliczWplate(abonament, kwota);
        }
        
        log.info("Dodano korektę wpłaty dla abonamentu ID: {}, kwota: {}, data: {}, opis: {}", 
                abonamentId, kwota, dataKorekty, opis);
        
        return korekta;
    }
    
    /**
     * Rozlicza wpłatę z należnościami abonamentu
     */
    @Transactional
    protected void rozliczWplate(Abonament abonament, BigDecimal kwotaWplaty) {
        BigDecimal pozostalaKwota = kwotaWplaty;
        
        // Pobierz niezapłacone należności, posortowane wg terminu płatności (najstarsze pierwsze)
        List<Naleznosc> niezaplaconeNaleznosci = naleznoscRepository
                .findByAbonamentAndOplaconaIsFalse(abonament).stream()
                .sorted(Comparator.comparing(Naleznosc::getTerminPlatnosci))
                .toList();
        
        for (Naleznosc naleznosc : niezaplaconeNaleznosci) {
            if (pozostalaKwota.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            if (pozostalaKwota.compareTo(naleznosc.getKwota()) >= 0) {
                // Wpłata wystarczy na pokrycie całej należności
                naleznosc.setOplacona(true);
                naleznoscRepository.save(naleznosc);
                pozostalaKwota = pozostalaKwota.subtract(naleznosc.getKwota());
                log.debug("Należność ID: {} została w pełni opłacona", naleznosc.getId());
            } else {
                // Wpłata pokrywa tylko część należności - nie oznaczamy jako opłacona
                log.debug("Należność ID: {} została częściowo opłacona (brakuje {})", 
                        naleznosc.getId(), naleznosc.getKwota().subtract(pozostalaKwota));
                break;
            }
        }
    }
    
    /**
     * Aktualizuje status należności po korekcie wpłaty
     */
    @Transactional
    protected void aktualizujStatusNaleznosciPoKorekcie(Abonament abonament) {
        // Pobierz wszystkie wpłaty i korekty abonamentu
        List<Wplata> wszystkieWplaty = wplataRepository.findByAbonament(abonament);
        BigDecimal sumaWplat = wszystkieWplaty.stream()
                .map(Wplata::getKwota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Pobierz wszystkie należności abonamentu, posortowane wg terminu
        List<Naleznosc> wszystkieNaleznosci = naleznoscRepository.findByAbonament(abonament).stream()
                .sorted(Comparator.comparing(Naleznosc::getTerminPlatnosci))
                .toList();
        
        // Resetujemy status opłaconych i rozliczamy od nowa
        for (Naleznosc naleznosc : wszystkieNaleznosci) {
            naleznosc.setOplacona(false);
        }
        
        BigDecimal pozostalaKwota = sumaWplat;
        for (Naleznosc naleznosc : wszystkieNaleznosci) {
            if (pozostalaKwota.compareTo(naleznosc.getKwota()) >= 0) {
                naleznosc.setOplacona(true);
                naleznoscRepository.save(naleznosc);
                pozostalaKwota = pozostalaKwota.subtract(naleznosc.getKwota());
            } else {
                break;
            }
        }
    }
    
    /**
     * Pobiera wszystkie wpłaty dla abonamentu
     */
    public List<Wplata> pobierzWplatyAbonamentu(Long abonamentId) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        return wplataRepository.findByAbonament(abonament);
    }
    
    /**
     * Pobiera wpłaty dla abonamentu w danym okresie
     */
    public List<Wplata> pobierzWplatyAbonamentuWOkresie(Long abonamentId, LocalDate dataOd, LocalDate dataDo) {
        Abonament abonament = abonamentRepository.findById(abonamentId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono abonamentu o ID: " + abonamentId));
        
        return wplataRepository.findByAbonamentAndDataWplatyBetween(abonament, dataOd, dataDo);
    }
    
    /**
     * Znajdź wpłatę po ID
     */
    public Wplata znajdzWplatePoId(Long wplataId) {
        return wplataRepository.findById(wplataId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wpłaty o ID: " + wplataId));
    }
    
    /**
     * Pobiera wszystkie korekty wpłat
     */
    public List<Wplata> pobierzWszystkieKorekty() {
        return wplataRepository.findByCzyKorektaIsTrue();
    }
}