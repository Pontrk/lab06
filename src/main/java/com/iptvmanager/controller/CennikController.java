package com.iptvmanager.controller;

import com.iptvmanager.model.Cennik;
import com.iptvmanager.model.TypAbonamentu;
import com.iptvmanager.service.CennikService;
import com.iptvmanager.service.SymulatorCzasuService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kontroler do zarządzania cennikiem
 */
@Controller
@RequestMapping("/cennik")
@RequiredArgsConstructor
public class CennikController {

    private final CennikService cennikService;
    private final SymulatorCzasuService symulatorCzasuService;

    @GetMapping
    public String pokazCennik(Model model) {
        model.addAttribute("cennikAktualny", cennikService.pobierzAktualnyCennik());
        model.addAttribute("cennikHistoria", cennikService.pobierzWszystkiePozycjeCennika());
        model.addAttribute("typyAbonamentu", TypAbonamentu.values());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("content", "cennik");
        return "cennik";
    }

    @GetMapping("/dezaktywuj/{id}")
    public String formularzDezaktywacji(@PathVariable Long id, Model model) {
        try {
            Cennik cennik = cennikService.pobierzWszystkiePozycjeCennika().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pozycji cennika o ID: " + id));

            model.addAttribute("cennikAktualny", cennikService.pobierzAktualnyCennik());
            model.addAttribute("cennikHistoria", cennikService.pobierzWszystkiePozycjeCennika());
            model.addAttribute("cennikEdit", cennik);
            model.addAttribute("typyAbonamentu", TypAbonamentu.values());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("content", "cennik");
            return "cennik";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono pozycji cennika o ID: " + id);
            return "redirect:/cennik";
        }
    }

    @PostMapping("/dodaj")
    public String dodajPozycjeCennika(@RequestParam TypAbonamentu typAbonamentu,
            @RequestParam BigDecimal cena,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataOd,
            RedirectAttributes redirectAttributes) {
        try {
            cennikService.dodajPozycjeCennika(typAbonamentu, cena, dataOd);
            redirectAttributes.addFlashAttribute("sukces", "Pozycja cennika została dodana pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania pozycji cennika: " + e.getMessage());
        }

        return "redirect:/cennik";
    }

    @PostMapping("/dezaktywuj/{id}")
    public String dezaktywujPozycjeCennika(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDo,
            RedirectAttributes redirectAttributes) {
        try {
            cennikService.dezaktywujPozycjeCennika(id, dataDo);
            redirectAttributes.addFlashAttribute("sukces", "Pozycja cennika została dezaktywowana pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad",
                    "Błąd podczas dezaktywacji pozycji cennika: " + e.getMessage());
        }

        return "redirect:/cennik";
    }

    @GetMapping("/dodaj")
    public String formularzDodawania(Model model) {
        model.addAttribute("cennikAktualny", cennikService.pobierzAktualnyCennik());
        model.addAttribute("cennikHistoria", cennikService.pobierzWszystkiePozycjeCennika());
        model.addAttribute("typyAbonamentu", TypAbonamentu.values());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("pokazFormularzDodawania", true);
        model.addAttribute("content", "cennik");
        return "cennik";
    }
}