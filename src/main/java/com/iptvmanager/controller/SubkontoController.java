package com.iptvmanager.controller;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Subkonto;
import com.iptvmanager.service.AbonamentService;
import com.iptvmanager.service.SymulatorCzasuService;
import com.iptvmanager.service.SubkontoService;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Kontroler do zarządzania subkontami (uproszczony - większość funkcji w
 * AbonamentController)
 */
@Controller
@RequestMapping("/subkonta")
@RequiredArgsConstructor
public class SubkontoController {

    private final SubkontoService subkontoService;
    private final AbonamentService abonamentService;
    private final SymulatorCzasuService symulatorCzasuService;

    @PostMapping("/dodaj/{id}")
    public String dodajSubkonto(@PathVariable Long id,
            @RequestParam String login,
            @RequestParam String haslo,
            RedirectAttributes redirectAttributes) {
        try {
            subkontoService.dodajSubkonto(id, login, haslo);
            redirectAttributes.addFlashAttribute("sukces", "Subkonto zostało dodane pomyślnie!");
            return "redirect:/abonamenty/szczegoly/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania subkonta: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }

    @GetMapping("/zmien-haslo/{id}")
    public String zmienHaslo(@PathVariable Long id,
            @RequestParam String noweHaslo,
            RedirectAttributes redirectAttributes) {
        try {
            Subkonto subkonto = subkontoService.zmienHaslo(id, noweHaslo);
            redirectAttributes.addFlashAttribute("sukces", "Hasło zostało zmienione pomyślnie!");
            return "redirect:/abonamenty/szczegoly/" + subkonto.getAbonament().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas zmiany hasła: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }

    @GetMapping("/dezaktywuj/{id}")
    public String dezaktywujSubkonto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Subkonto subkonto = subkontoService.dezaktywujSubkonto(id);
            redirectAttributes.addFlashAttribute("sukces", "Subkonto zostało dezaktywowane pomyślnie!");
            return "redirect:/abonamenty/szczegoly/" + subkonto.getAbonament().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dezaktywacji subkonta: " + e.getMessage());
            return "redirect:/abonamenty";
        }
    }

    @GetMapping("/dodaj/{id}")
    public String formularzDodawania(@PathVariable Long id, Model model) {
        try {
            Abonament abonament = abonamentService.znajdzAbonamentPoId(id);
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("pokazFormularzDodawaniaSubkonta", true);
            model.addAttribute("abonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("content", "abonamenty");
            return "abonamenty";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono abonamentu o ID: " + id);
            return "redirect:/abonamenty";
        }
    }
}