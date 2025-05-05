package com.iptvmanager.service;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Klient;
import com.iptvmanager.model.Naleznosc;
import com.iptvmanager.repository.NaleznoscRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitService {
    
    private final NaleznoscRepository naleznoscRepository;
    
    /**
     * Wysyła monity do klientów z przeterminowanymi płatnościami
     */
    @Transactional
    public void wyslijMonity(LocalDate dataAktualna) {
        // Pobierz wszystkie niezapłacone należności po terminie płatności
        List<Naleznosc> przeterminowaneNaleznosci = naleznoscRepository.findNiezaplaconePoTerminie(dataAktualna);
        
        log.info("Rozpoczęto wysyłanie monitów na dzień {}. Znaleziono {} przeterminowanych należności.", 
                dataAktualna, przeterminowaneNaleznosci.size());
        
        for (Naleznosc naleznosc : przeterminowaneNaleznosci) {
            long dniPoTerminie = ChronoUnit.DAYS.between(naleznosc.getTerminPlatnosci(), dataAktualna);
            Abonament abonament = naleznosc.getAbonament();
            Klient klient = abonament.getKlient();
            
            // Pierwszy monit - 3 dni po terminie
            if (dniPoTerminie >= 3 && naleznosc.getStatusMonitu() == Naleznosc.StatusMonitu.BRAK) {
                wyslijPierwszyMonit(naleznosc, klient, abonament, dataAktualna);
            }
            // Drugi monit - 10 dni po terminie
            else if (dniPoTerminie >= 10 && naleznosc.getStatusMonitu() == Naleznosc.StatusMonitu.MONIT_PIERWSZY) {
                wyslijDrugiMonit(naleznosc, klient, abonament, dataAktualna);
            }
            // Trzeci monit - 20 dni po terminie
            else if (dniPoTerminie >= 20 && naleznosc.getStatusMonitu() == Naleznosc.StatusMonitu.MONIT_DRUGI) {
                wyslijTrzeciMonit(naleznosc, klient, abonament, dataAktualna);
            }
            // Windykacja - 30 dni po terminie
            else if (dniPoTerminie >= 30 && naleznosc.getStatusMonitu() == Naleznosc.StatusMonitu.MONIT_TRZECI) {
                przekazDoWindykacji(naleznosc, klient, abonament, dataAktualna);
            }
        }
        
        log.info("Zakończono wysyłanie monitów na dzień {}", dataAktualna);
    }
    
    /**
     * Wysyła pierwszy monit
     */
    private void wyslijPierwszyMonit(Naleznosc naleznosc, Klient klient, Abonament abonament, LocalDate dataAktualna) {
        // Tworzenie treści monitu
        String trescMonitu = String.format(
                "PIERWSZY MONIT: Szanowny Kliencie %s %s (nr: %s), przypominamy o nieuregulowanej należności " +
                "za abonament %s za okres %s w kwocie %.2f zł. Termin płatności minął dnia %s. " +
                "Prosimy o niezwłoczne uregulowanie należności.",
                klient.getImie(), klient.getNazwisko(), klient.getNumerKlienta(), 
                abonament.getTypAbonamentu(), naleznosc.getOkresRozliczeniowy(), 
                naleznosc.getKwota(), naleznosc.getTerminPlatnosci());
        
        // Logowanie monitu
        log.info(trescMonitu);
        
        // Aktualizacja statusu monitu
        naleznosc.setStatusMonitu(Naleznosc.StatusMonitu.MONIT_PIERWSZY);
        naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Wysyła drugi monit
     */
    private void wyslijDrugiMonit(Naleznosc naleznosc, Klient klient, Abonament abonament, LocalDate dataAktualna) {
        // Tworzenie treści monitu
        String trescMonitu = String.format(
                "DRUGI MONIT: Szanowny Kliencie %s %s (nr: %s), przypominamy ponownie o nieuregulowanej należności " +
                "za abonament %s za okres %s w kwocie %.2f zł. Termin płatności minął dnia %s. " +
                "Prosimy o niezwłoczne uregulowanie należności. W przypadku braku wpłaty w ciągu 10 dni " +
                "zostanie wysłany ostateczny monit przed przekazaniem sprawy do windykacji.",
                klient.getImie(), klient.getNazwisko(), klient.getNumerKlienta(), 
                abonament.getTypAbonamentu(), naleznosc.getOkresRozliczeniowy(), 
                naleznosc.getKwota(), naleznosc.getTerminPlatnosci());
        
        // Logowanie monitu
        log.info(trescMonitu);
        
        // Aktualizacja statusu monitu
        naleznosc.setStatusMonitu(Naleznosc.StatusMonitu.MONIT_DRUGI);
        naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Wysyła trzeci monit
     */
    private void wyslijTrzeciMonit(Naleznosc naleznosc, Klient klient, Abonament abonament, LocalDate dataAktualna) {
        // Tworzenie treści monitu
        String trescMonitu = String.format(
                "OSTATECZNY MONIT: Szanowny Kliencie %s %s (nr: %s), informujemy, że pomimo wcześniejszych monitów " +
                "nie uregulowano należności za abonament %s za okres %s w kwocie %.2f zł. Termin płatności minął dnia %s. " +
                "Jest to ostateczne wezwanie do zapłaty. W przypadku braku wpłaty w ciągu 10 dni sprawa zostanie " +
                "przekazana do windykacji, a usługa zostanie zawieszona.",
                klient.getImie(), klient.getNazwisko(), klient.getNumerKlienta(), 
                abonament.getTypAbonamentu(), naleznosc.getOkresRozliczeniowy(), 
                naleznosc.getKwota(), naleznosc.getTerminPlatnosci());
        
        // Logowanie monitu
        log.info(trescMonitu);
        
        // Aktualizacja statusu monitu
        naleznosc.setStatusMonitu(Naleznosc.StatusMonitu.MONIT_TRZECI);
        naleznoscRepository.save(naleznosc);
    }
    
    /**
     * Przekazuje sprawę do windykacji
     */
    private void przekazDoWindykacji(Naleznosc naleznosc, Klient klient, Abonament abonament, LocalDate dataAktualna) {
        // Tworzenie treści informacji o windykacji
        String trescWindykacji = String.format(
                "WINDYKACJA: Klient %s %s (nr: %s), należność za abonament %s za okres %s w kwocie %.2f zł " +
                "została przekazana do windykacji. Termin płatności minął dnia %s. Usługa została zawieszona.",
                klient.getImie(), klient.getNazwisko(), klient.getNumerKlienta(), 
                abonament.getTypAbonamentu(), naleznosc.getOkresRozliczeniowy(), 
                naleznosc.getKwota(), naleznosc.getTerminPlatnosci());
        
        // Logowanie informacji o windykacji
        log.info(trescWindykacji);
        
        // Aktualizacja statusu monitu i zawieszenie abonamentu
        naleznosc.setStatusMonitu(Naleznosc.StatusMonitu.WINDYKACJA);
        naleznoscRepository.save(naleznosc);
        
        // Zawieszenie abonamentu
        abonament.setAktywny(false);
    }
}