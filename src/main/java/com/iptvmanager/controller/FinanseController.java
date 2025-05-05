package com.iptvmanager.controller;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Naleznosc;
import com.iptvmanager.service.AbonamentService;
import com.iptvmanager.service.NaleznoscService;
import com.iptvmanager.service.SymulatorCzasuService;
import com.iptvmanager.service.WplataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kontroler do zarządzania finansami (należności i wpłaty)
 */
@Controller
@RequestMapping("/finanse")
@RequiredArgsConstructor
public class FinanseController {

    private final NaleznoscService naleznoscService;
    private final WplataService wplataService;
    private final AbonamentService abonamentService;
    private final SymulatorCzasuService symulatorCzasuService;

    @GetMapping
    public String pokazFinanse(Model model) {
        model.addAttribute("zaleglosci",
                naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()));
        model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("content", "finanse");
        return "finanse";
    }

    @GetMapping("/abonament/{id}")
    public String pokazFinanseAbonamentu(@PathVariable Long id, Model model) {
        try {
            Abonament abonament = abonamentService.znajdzAbonamentPoId(id);
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("zaleglosci",
                    naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()));
            model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("content", "finanse");
            return "finanse";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono abonamentu o ID: " + id);
            return "redirect:/finanse";
        }
    }

    @PostMapping("/dodaj-naleznosc")
    public String dodajNaleznosc(@RequestParam Long abonamentId,
            @RequestParam String okresRozliczeniowy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminPlatnosci,
            RedirectAttributes redirectAttributes) {
        try {
            naleznoscService.naliczNaleznoscAbonamentu(abonamentId, terminPlatnosci, okresRozliczeniowy);
            redirectAttributes.addFlashAttribute("sukces", "Należność została dodana pomyślnie!");
            return "redirect:/finanse/abonament/" + abonamentId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania należności: " + e.getMessage());
            return "redirect:/finanse";
        }
    }

    @PostMapping("/dodaj-wplate")
    public String dodajWplate(@RequestParam Long abonamentId,
            @RequestParam BigDecimal kwota,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataWplaty,
            RedirectAttributes redirectAttributes) {
        try {
            wplataService.zarejestrujWplate(abonamentId, kwota, dataWplaty);
            redirectAttributes.addFlashAttribute("sukces", "Wpłata została zarejestrowana pomyślnie!");
            return "redirect:/finanse/abonament/" + abonamentId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania wpłaty: " + e.getMessage());
            return "redirect:/finanse";
        }
    }

    @PostMapping("/dodaj-korekte")
    public String dodajKorekteWplaty(@RequestParam Long abonamentId,
            @RequestParam BigDecimal kwota,
            @RequestParam String opis,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataKorekty,
            RedirectAttributes redirectAttributes) {
        try {
            wplataService.dodajKorekteWplaty(abonamentId, kwota, dataKorekty, opis);
            redirectAttributes.addFlashAttribute("sukces", "Korekta wpłaty została dodana pomyślnie!");
            return "redirect:/finanse/abonament/" + abonamentId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania korekty wpłaty: " + e.getMessage());
            return "redirect:/finanse";
        }
    }

    @GetMapping("/oplacona/{id}")
    public String oznaczNaleznoscJakoOplacona(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Naleznosc naleznosc = naleznoscService.oznaczJakoOplacona(id);
            redirectAttributes.addFlashAttribute("sukces", "Należność została oznaczona jako opłacona!");
            return "redirect:/finanse/abonament/" + naleznosc.getAbonament().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad",
                    "Błąd podczas oznaczania należności jako opłaconej: " + e.getMessage());
            return "redirect:/finanse";
        }
    }

    @GetMapping("/koryguj-naleznosc/{id}")
    public String pokazFormulaKorektyNaleznosci(@PathVariable Long id, Model model) {
        try {
            Naleznosc naleznosc = naleznoscService.znajdzNaleznoscPoId(id);
            Abonament abonament = naleznosc.getAbonament();
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("naleznoscDoKorekty", naleznosc);
            model.addAttribute("zaleglosci",
                    naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()));
            model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("content", "finanse");
            return "finanse";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono należności o ID: " + id);
            return "redirect:/finanse";
        }
    }

    @PostMapping("/koryguj-naleznosc/{id}")
    public String korygujKwoteNaleznosci(@PathVariable Long id,
            @RequestParam BigDecimal nowaKwota,
            RedirectAttributes redirectAttributes) {
        try {
            Naleznosc naleznosc = naleznoscService.korygujKwoteNaleznosci(id, nowaKwota);
            redirectAttributes.addFlashAttribute("sukces", "Kwota należności została skorygowana pomyślnie!");
            return "redirect:/finanse/abonament/" + naleznosc.getAbonament().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad",
                    "Błąd podczas korygowania kwoty należności: " + e.getMessage());
            return "redirect:/finanse";
        }
    }

    @GetMapping("/dodaj-naleznosc")
    public String formularzDodawaniaNaleznosci(Model model) {
        model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("pokazFormularzDodawaniaNaleznosci", true);
        model.addAttribute("content", "finanse");
        return "finanse";
    }

    @GetMapping("/dodaj-wplate")
    public String formularzDodawaniaWplaty(Model model) {
        model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
        model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
        model.addAttribute("pokazFormularzDodawaniaWplaty", true);
        model.addAttribute("content", "finanse");
        return "finanse";
    }

    @GetMapping("/dodaj-naleznosc-abonament/{id}")
    public String formularzDodawaniaNaleznosciAbonamentu(@PathVariable Long id, Model model) {
        try {
            Abonament abonament = abonamentService.znajdzAbonamentPoId(id);
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("zaleglosci",
                    naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()));
            model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("pokazFormularzDodawaniaNaleznosciAbonamentu", true);
            model.addAttribute("content", "finanse");
            return "finanse";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono abonamentu o ID: " + id);
            return "redirect:/finanse";
        }
    }

    @GetMapping("/dodaj-wplate-abonament/{id}")
    public String formularzDodawaniaWplatyAbonamentu(@PathVariable Long id, Model model) {
        try {
            Abonament abonament = abonamentService.znajdzAbonamentPoId(id);
            model.addAttribute("wybranyAbonament", abonament);
            model.addAttribute("zaleglosci",
                    naleznoscService.pobierzPrzeterminowaneNaleznosci(symulatorCzasuService.getAktualnaDatSymulacji()));
            model.addAttribute("aktywneAbonamenty", abonamentService.pobierzAktywneAbonamenty());
            model.addAttribute("dzisiaj", symulatorCzasuService.getAktualnaDatSymulacji());
            model.addAttribute("pokazFormularzDodawaniaWplatyAbonamentu", true);
            model.addAttribute("content", "finanse");
            return "finanse";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono abonamentu o ID: " + id);
            return "redirect:/finanse";
        }
    }
}