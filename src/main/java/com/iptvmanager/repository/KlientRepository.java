package com.iptvmanager.repository;

import com.iptvmanager.model.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KlientRepository extends JpaRepository<Klient, Long> {
    
    Optional<Klient> findByNumerKlienta(String numerKlienta);
    
    boolean existsByNumerKlienta(String numerKlienta);
}