package com.iptvmanager.controller;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Klient;
import com.iptvmanager.model.TypAbonamentu;
import com.iptvmanager.service.AbonamentService;
import com.iptvmanager.service.KlientService;
import com.iptvmanager.service.NaleznoscService;
import com.iptvmanager.service.SymulatorCzasuService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Kontroler do zarządzania abonamentami i subkontami
 */
@Controller
@RequestMapping("/abonamenty")
@RequiredArgsConstructor
public class AbonamentController {

    private final AbonamentService abonamentService;
    private final KlientService klientService;
    private final NaleznoscService naleznoscService;
    private final SymulatorCzasuService symulatorCzasuService;

    @GetMapping
    public String listaAbonamentow(Model model) {
        model.addAttribute("abonamenty", abonamentService.pobierzAktywneAbonamenty());
        model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
        model.addAttribute("typyAbonamentu", TypAbonamentu.values());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("content", "abonamenty");
        return "abonamenty";
    }

    @GetMapping("/klient/{klientId}")
    public String listaAbonamentowKlienta(@PathVariable Long klientId, Model model) {
        Klient klient = klientService.znajdzKlientaPoId(klientId);
        model.addAttribute("wybranyKlient", klient);
        model.addAttribute("abonamenty", abonamentService.pobierzAbonamentyKlienta(klientId));
        model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
        model.addAttribute("typyAbonamentu", TypAbonamentu.values());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("content", "abonamenty");
        return "abonamenty";
    }

    @GetMapping("/szczegoly/{id}")
    public String szczegolyAbonamentu(@PathVariable Long id, Model model) {
        try {
            Abonament abonament = abonamentService.znajdzAbonamentPoId(id);
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("abonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
            model.addAttribute("typyAbonamentu", TypAbonamentu.values());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("content", "abonamenty");
            return "abonamenty";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono abonamentu o ID: " + id);
            return "redirect:/abonamenty";
        }
    }

    @PostMapping("/dodaj")
    public String dodajAbonament(@RequestParam Long klientId,
                              @RequestParam TypAbonamentu typAbonamentu,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRozpoczecia,
                              RedirectAttributes redirectAttributes) {
        try {
            Abonament abonament = abonamentService.dodajAbonament(klientId, typAbonamentu, dataRozpoczecia);
            
            // Naliczenie pierwszej należności
            String okresRozliczeniowy = dataRozpoczecia.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate terminPlatnosci = dataRozpoczecia.plusDays(14); // 14 dni na zapłatę
            
            naleznoscService.naliczNaleznoscAbonamentu(abonament.getId(), terminPlatnosci, okresRozliczeniowy);
            
            redirectAttributes.addFlashAttribute("sukces", "Abonament został dodany pomyślnie!");
            return "redirect:/abonamenty/klient/" + klientId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania abonamentu: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }

    @PostMapping("/zmien-typ/{id}")
    public String zmienTypAbonamentu(@PathVariable Long id, 
                                     @RequestParam TypAbonamentu nowyTyp,
                                     RedirectAttributes redirectAttributes) {
        try {
            Abonament abonament = abonamentService.zmienTypAbonamentu(id, nowyTyp);
            redirectAttributes.addFlashAttribute("sukces", "Typ abonamentu został zmieniony pomyślnie!");
            return "redirect:/abonamenty/szczegoly/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas zmiany typu abonamentu: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }

    @PostMapping("/dezaktywuj/{id}")
    public String dezaktywujAbonament(@PathVariable Long id, 
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataZakonczenia,
                                   RedirectAttributes redirectAttributes) {
        try {
            Abonament abonament = abonamentService.dezaktywujAbonament(id, dataZakonczenia);
            redirectAttributes.addFlashAttribute("sukces", "Abonament został dezaktywowany pomyślnie!");
            return "redirect:/abonamenty/klient/" + abonament.getKlient().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dezaktywacji abonamentu: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }
}