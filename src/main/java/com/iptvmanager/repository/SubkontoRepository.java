package com.iptvmanager.repository;

import com.iptvmanager.model.Abonament;
import com.iptvmanager.model.Subkonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubkontoRepository extends JpaRepository<Subkonto, Long> {
    
    List<Subkonto> findByAbonament(Abonament abonament);
    
    Optional<Subkonto> findByLogin(String login);
    
    boolean existsByLogin(String login);
}