package com.iptvmanager.repository;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonamentRepository extends JpaRepository<Abonament, Long> {
    
    List<Abonament> findByKlient(Klient klient);
    
    List<Abonament> findByAktywnyIsTrue();
}