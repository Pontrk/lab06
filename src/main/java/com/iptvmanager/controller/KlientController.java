package com.iptvmanager.controller;

import com.iptvmanager.model.Klient;
import com.iptvmanager.service.KlientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Kontroler do zarządzania klientami
 */
@Controller
@RequestMapping("/klienci")
@RequiredArgsConstructor
public class KlientController {

    private final KlientService klientService;

    @GetMapping
    public String listaKlientow(Model model) {
        model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
        model.addAttribute("content", "klienci");
        return "klienci";
    }

    @GetMapping("/edytuj/{id}")
    public String formularzEdycji(@PathVariable Long id, Model model) {
        try {
            Klient klient = klientService.znajdzKlientaPoId(id);
            model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
            model.addAttribute("klientEdit", klient);
            model.addAttribute("content", "klienci");
            return "klienci";
        } catch (Exception e) {
            model.addAttribute("blad", "Nie znaleziono klienta o ID: " + id);
            return "redirect:/klienci";
        }
    }

    @PostMapping("/dodaj")
    public String dodajKlienta(@RequestParam String imie,
            @RequestParam String nazwisko,
            @RequestParam String numerKlienta,
            RedirectAttributes redirectAttributes) {
        try {
            klientService.dodajKlienta(imie, nazwisko, numerKlienta);
            redirectAttributes.addFlashAttribute("sukces", "Klient został dodany pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas dodawania klienta: " + e.getMessage());
        }

        return "redirect:/klienci";
    }

    @PostMapping("/edytuj/{id}")
    public String aktualizujKlienta(@PathVariable Long id,
            @RequestParam String imie,
            @RequestParam String nazwisko,
            RedirectAttributes redirectAttributes) {
        try {
            klientService.aktualizujKlienta(id, imie, nazwisko);
            redirectAttributes.addFlashAttribute("sukces", "Klient został zaktualizowany pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas aktualizacji klienta: " + e.getMessage());
        }

        return "redirect:/klienci";
    }

    @GetMapping("/usun/{id}")
    public String usunKlienta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            klientService.usunKlienta(id);
            redirectAttributes.addFlashAttribute("sukces", "Klient został usunięty pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("blad", "Błąd podczas usuwania klienta: " + e.getMessage());
        }

        return "redirect:/klienci";
    }

    @GetMapping("/dodaj")
    public String formularzDodawania(Model model) {
        model.addAttribute("klienci", klientService.pobierzWszystkichKlientow());
        model.addAttribute("pokazFormularzDodawania", true);
        model.addAttribute("content", "klienci");
        return "klienci";
    }
}