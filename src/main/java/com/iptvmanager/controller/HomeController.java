package com.iptvmanager.controller;

import com.iptvmanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Kontroler obsługujący stronę główną i symulator czasu
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final KlientService klientService;
    private final AbonamentService abonamentService;
    private final CennikService cennikService;
    private final NaleznoscService naleznoscService;
    private final SymulatorCzasuService symulatorCzasuService;

    @GetMapping("/")
    public String home(Model model) {
        // Dodaj podsumowanie danych do strony głównej
        model.addAttribute("liczbaKlientow", klientService.pobierzWszystkichKlientow().size());
        model.addAttribute("liczbaAktywnychAbonamentow", abonamentService.pobierzAktywneAbonamenty().size());
        model.addAttribute("aktualnyCennik", cennikService.pobierzAktualnyCennik());
        model.addAttribute("liczbaPrzeterminowanychNaleznosci", 
                naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()).size());
        model.addAttribute("dataSym", symulatorCzasuService.getAktualnaDatSymulacji());
        
        return "home";
    }
    
    // Obsługa symulacji czasu - przeniesiona z SymulatorController
    
    @PostMapping("/przesun-date")
    public String przesunDate(@RequestParam int liczbaDni, RedirectAttributes redirectAttributes) {
        try {
            LocalDate nowaData = symulatorCzasuService.przesunDateSymulacji(liczbaDni);
            redirectAttributes.addFlashAttribute("sukces", 
                    "Data symulacji została przesunięta o " + liczbaDni + " dni na: " + nowaData);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas przesuwania daty symulacji: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @PostMapping("/ustaw-date")
    public String ustawDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nowaData, 
                          RedirectAttributes redirectAttributes) {
        try {
            symulatorCzasuService.ustawDateSymulacji(nowaData);
            redirectAttributes.addFlashAttribute("sukces", "Data symulacji została ustawiona na: " + nowaData);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas ustawiania daty symulacji: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @PostMapping("/symuluj-miesiac")
    public String symulujMiesiac(@RequestParam int liczbaMiesiecy, RedirectAttributes redirectAttributes) {
        try {
            LocalDate nowaData = symulatorCzasuService.symulujMiesiace(liczbaMiesiecy);
            redirectAttributes.addFlashAttribute("sukces", 
                    "Zasymulowano upływ " + liczbaMiesiecy + " miesięcy. Nowa data: " + nowaData);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas symulacji upływu miesiąca: " + e.getMessage());
        }
        
        return "redirect:/";
    }
}