package com.iptvmanager.repository;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Wplata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WplataRepository extends JpaRepository<Wplata, Long> {
    
    List<Wplata> findByAbonament(Abonament abonament);
    
    List<Wplata> findByAbonamentAndDataWplatyBetween(Abonament abonament, LocalDate dataOd, LocalDate dataDo);
    
    List<Wplata> findByCzyKorektaIsTrue();
}