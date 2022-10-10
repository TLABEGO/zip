package com.mfs.client.equity.persistance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mfs.client.equity.persistance.model.BankMaster;

/**
 * Holds queries to do with a bank
 */
@Repository
public interface BankMasterRepository extends JpaRepository<BankMaster, Integer> {
    Optional<BankMaster> findByCountryCodeAndBankName(String countryCode,String bankName);
}
