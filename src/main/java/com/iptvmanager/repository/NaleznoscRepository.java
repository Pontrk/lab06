package com.iptvmanager.repository;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Naleznosc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NaleznoscRepository extends JpaRepository<Naleznosc, Long> {
    
    List<Naleznosc> findByAbonament(Abonament abonament);
    
    List<Naleznosc> findByAbonamentAndOplaconaIsFalse(Abonament abonament);
    
    @Query("SELECT n FROM Naleznosc n WHERE n.oplacona = false AND n.terminPlatnosci < :data")
    List<Naleznosc> findNiezaplaconePoTerminie(LocalDate data);
    
    @Query("SELECT n FROM Naleznosc n WHERE n.oplacona = false AND n.statusMonitu = :status AND n.terminPlatnosci < :data")
    List<Naleznosc> findNiezaplaconePoTerminieZeStatusem(Naleznosc.StatusMonitu status, LocalDate data);
    
    Optional<Naleznosc> findByAbonamentAndOkresRozliczeniowy(Abonament abonament, String okresRozliczeniowy);
}