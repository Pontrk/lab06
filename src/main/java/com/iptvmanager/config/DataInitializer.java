package com.iptvmanager.config;

import com.iptvmanager.model.*;
import com.iptvmanager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Inicjalizator danych początkowych aplikacji
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final KlientService klientService;
    private final AbonamentService abonamentService;
    private final SubkontoService subkontoService;
    private final CennikService cennikService;
    private final NaleznoscService naleznoscService;
    private final WplataService wplataService;
    private final SymulatorCzasuService symulatorCzasuService;

    @Override
    public void run(String... args) {
        inicjalizujDanePoczatkowe();
    }

    /**
     * Inicjalizuje podstawowe dane w systemie
     */
    private void inicjalizujDanePoczatkowe() {
        log.info("Inicjalizacja danych początkowych...");
        
        try {
            // Sprawdź czy już istnieją dane
            if (!klientService.pobierzWszystkichKlientow().isEmpty()) {
                log.info("Dane już istnieją w bazie, pomijam inicjalizację");
                return;
            }
            
            // Dodaj pozycje cennika
            log.info("Dodawanie pozycji cennika...");
            Cennik cennikPodstawowy = cennikService.dodajPozycjeCennika(TypAbonamentu.PODSTAWOWY, 
                    new BigDecimal("29.99"), LocalDate.now());
            Cennik cennikRozszerzony = cennikService.dodajPozycjeCennika(TypAbonamentu.ROZSZERZONY, 
                    new BigDecimal("49.99"), LocalDate.now());
            Cennik cennikPremium = cennikService.dodajPozycjeCennika(TypAbonamentu.PREMIUM, 
                    new BigDecimal("79.99"), LocalDate.now());
            Cennik cennikSport = cennikService.dodajPozycjeCennika(TypAbonamentu.SPORT, 
                    new BigDecimal("39.99"), LocalDate.now());
            Cennik cennikFilm = cennikService.dodajPozycjeCennika(TypAbonamentu.FILM, 
                    new BigDecimal("34.99"), LocalDate.now());
            Cennik cennikDzieci = cennikService.dodajPozycjeCennika(TypAbonamentu.DZIECI, 
                    new BigDecimal("24.99"), LocalDate.now());
            Cennik cennikAll = cennikService.dodajPozycjeCennika(TypAbonamentu.ALL_INCLUSIVE, 
                    new BigDecimal("99.99"), LocalDate.now());
            
            // Dodaj kilku klientów
            log.info("Dodawanie przykładowych klientów...");
            Klient klient1 = klientService.dodajKlienta("Jan", "Kowalski", "KOW001");
            Klient klient2 = klientService.dodajKlienta("Anna", "Nowak", "NOW001");
            Klient klient3 = klientService.dodajKlienta("Piotr", "Wiśniewski", "WIS001");
            
            // Dodaj abonamenty
            log.info("Dodawanie przykładowych abonamentów...");
            Abonament abonament1 = abonamentService.dodajAbonament(klient1.getId(), 
                    TypAbonamentu.PODSTAWOWY, LocalDate.now().minusMonths(2));
            Abonament abonament2 = abonamentService.dodajAbonament(klient1.getId(), 
                    TypAbonamentu.SPORT, LocalDate.now().minusMonths(1));
            Abonament abonament3 = abonamentService.dodajAbonament(klient2.getId(), 
                    TypAbonamentu.PREMIUM, LocalDate.now().minusMonths(3));
            Abonament abonament4 = abonamentService.dodajAbonament(klient3.getId(), 
                    TypAbonamentu.ALL_INCLUSIVE, LocalDate.now().minusMonths(1));
            
            // Dodaj subkonta
            log.info("Dodawanie przykładowych subkont...");
            subkontoService.dodajSubkonto(abonament1.getId(), "jankowalski1", "haslo123");
            subkontoService.dodajSubkonto(abonament1.getId(), "jankowalski2", "haslo123");
            subkontoService.dodajSubkonto(abonament2.getId(), "jankowalski3", "haslo123");
            subkontoService.dodajSubkonto(abonament3.getId(), "annanowak1", "haslo123");
            subkontoService.dodajSubkonto(abonament3.getId(), "annanowak2", "haslo123");
            subkontoService.dodajSubkonto(abonament4.getId(), "piotrwisniewski1", "haslo123");
            
            // Nalicz należności za poprzednie miesiące
            log.info("Naliczanie przykładowych należności...");
            LocalDate dzisiaj = LocalDate.now();
            
            // Należności dla abonamentu 1 (2 miesiące wstecz)
            String okres1 = dzisiaj.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            naleznoscService.naliczNaleznoscAbonamentu(abonament1.getId(), 
                    dzisiaj.minusMonths(2).plusDays(14), okres1);
            
            String okres2 = dzisiaj.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            naleznoscService.naliczNaleznoscAbonamentu(abonament1.getId(), 
                    dzisiaj.minusMonths(1).plusDays(14), okres2);
            
            // Należności dla abonamentu 2 (1 miesiąc wstecz)
            naleznoscService.naliczNaleznoscAbonamentu(abonament2.getId(), 
                    dzisiaj.minusMonths(1).plusDays(14), okres2);
            
            // Należności dla abonamentu 3 (3 miesiące wstecz)
            String okres3 = dzisiaj.minusMonths(3).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            naleznoscService.naliczNaleznoscAbonamentu(abonament3.getId(), 
                    dzisiaj.minusMonths(3).plusDays(14), okres3);
            
            String okres4 = dzisiaj.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            naleznoscService.naliczNaleznoscAbonamentu(abonament3.getId(), 
                    dzisiaj.minusMonths(2).plusDays(14), okres4);
            
            naleznoscService.naliczNaleznoscAbonamentu(abonament3.getId(), 
                    dzisiaj.minusMonths(1).plusDays(14), okres2);
            
            // Należności dla abonamentu 4 (1 miesiąc wstecz)
            naleznoscService.naliczNaleznoscAbonamentu(abonament4.getId(), 
                    dzisiaj.minusMonths(1).plusDays(14), okres2);
            
            // Dodaj kilka wpłat
            log.info("Dodawanie przykładowych wpłat...");
            wplataService.zarejestrujWplate(abonament1.getId(), new BigDecimal("29.99"), 
                    dzisiaj.minusMonths(2).plusDays(10));
            wplataService.zarejestrujWplate(abonament1.getId(), new BigDecimal("29.99"), 
                    dzisiaj.minusMonths(1).plusDays(10));
            wplataService.zarejestrujWplate(abonament2.getId(), new BigDecimal("39.99"), 
                    dzisiaj.minusMonths(1).plusDays(12));
            wplataService.zarejestrujWplate(abonament3.getId(), new BigDecimal("79.99"), 
                    dzisiaj.minusMonths(3).plusDays(5));
            wplataService.zarejestrujWplate(abonament3.getId(), new BigDecimal("79.99"), 
                    dzisiaj.minusMonths(2).plusDays(5));
            
            // Abonament 4 bez wpłaty - do testowania monitów
            
            log.info("Inicjalizacja danych zakończona pomyślnie");
            
        } catch (Exception e) {
            log.error("Błąd podczas inicjalizacji danych: ", e);
        }
    }
}