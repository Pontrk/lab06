package com.iptvmanager.repository;

import com.iptvmanager.model.Cennik;
import com.iptvmanager.model.TypAbonamentu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CennikRepository extends JpaRepository<Cennik, Long> {
    
    List<Cennik> findByAktywnyIsTrue();
    
    @Query("SELECT c FROM Cennik c WHERE c.typAbonamentu = :typ AND c.aktywny = true AND " +
           "(c.dataOd <= :data AND (c.dataDo IS NULL OR c.dataDo >= :data))")
    Optional<Cennik> findAktualnyDlaTypu(TypAbonamentu typ, LocalDate data);
    
    List<Cennik> findByTypAbonamentu(TypAbonamentu typAbonamentu);
}