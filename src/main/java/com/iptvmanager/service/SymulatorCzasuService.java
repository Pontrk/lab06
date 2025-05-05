package com.iptvmanager.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Serwis odpowiedzialny za symulację upływu czasu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SymulatorCzasuService {
    
    private final NaleznoscService naleznoscService;
    private final MonitService monitService;
    
    @Getter
    private LocalDate aktualnaDatSymulacji = LocalDate.now();
    
    /**
     * Przesuwa datę symulacji o określoną liczbę dni
     */
    public LocalDate przesunDateSymulacji(int liczbaDni) {
        LocalDate poprzedniaData = aktualnaDatSymulacji;
        aktualnaDatSymulacji = aktualnaDatSymulacji.plusDays(liczbaDni);
        
        log.info("Symulacja: przesunięto datę z {} na {} (o {} dni)", 
                poprzedniaData, aktualnaDatSymulacji, liczbaDni);
        
        // Jeśli przesunięto do nowego miesiąca, generujemy należności
        if (poprzedniaData.getMonth() != aktualnaDatSymulacji.getMonth()) {
            log.info("Symulacja: rozpoczęto nowy miesiąc - generowanie należności za {}", 
                    aktualnaDatSymulacji.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            
            // Naliczanie należności na nowy miesiąc (1. dzień miesiąca)
            LocalDate pierwszyDzienMiesiaca = aktualnaDatSymulacji.withDayOfMonth(1);
            naleznoscService.automatyczneNaliczanieNaleznosci(pierwszyDzienMiesiaca);
        }
        
        // Sprawdzenie monitów (codziennie)
        monitService.wyslijMonity(aktualnaDatSymulacji);
        
        return aktualnaDatSymulacji;
    }
    
    /**
     * Ustawia konkretną datę symulacji
     */
    public LocalDate ustawDateSymulacji(LocalDate nowaData) {
        LocalDate poprzedniaData = aktualnaDatSymulacji;
        aktualnaDatSymulacji = nowaData;
        
        log.info("Symulacja: ustawiono datę z {} na {}", poprzedniaData, aktualnaDatSymulacji);
        
        return aktualnaDatSymulacji;
    }
    
    /**
     * Symuluje upływ czasu o określoną liczbę miesięcy
     */
    public LocalDate symulujMiesiace(int liczbaMiesiecy) {
        for (int i = 0; i < liczbaMiesiecy; i++) {
            LocalDate pierwszyDzienMiesiaca = aktualnaDatSymulacji.plusMonths(1).withDayOfMonth(1);
            aktualnaDatSymulacji = pierwszyDzienMiesiaca;
            
            log.info("Symulacja: przesunięto do początku miesiąca: {}", aktualnaDatSymulacji);
            
            // Naliczanie należności na nowy miesiąc (1. dzień miesiąca)
            naleznoscService.automatyczneNaliczanieNaleznosci(aktualnaDatSymulacji);
            
            // Symulacja dni w miesiącu i wysyłanie monitów
            symulujDniWMiesiacu();
        }
        
        return aktualnaDatSymulacji;
    }
    
    /**
     * Symuluje upływ czasu dzień po dniu przez cały miesiąc
     */
    private void symulujDniWMiesiacu() {
        LocalDate pierwszyDzien = aktualnaDatSymulacji;
        LocalDate ostatniDzien = aktualnaDatSymulacji.plusMonths(1).withDayOfMonth(1).minusDays(1);
        
        log.info("Symulacja: symulowanie dni w miesiącu od {} do {}", pierwszyDzien, ostatniDzien);
        
        LocalDate aktualnyDzien = pierwszyDzien;
        while (!aktualnyDzien.isAfter(ostatniDzien)) {
            aktualnaDatSymulacji = aktualnyDzien;
            
            // Sprawdzenie monitów (codziennie)
            monitService.wyslijMonity(aktualnaDatSymulacji);
            
            aktualnyDzien = aktualnyDzien.plusDays(1);
        }
    }
}