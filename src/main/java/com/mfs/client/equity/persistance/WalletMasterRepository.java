package com.mfs.client.equity.persistance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mfs.client.equity.persistance.model.WalletMaster;

/**
 * Holds queries to do with a wallet
 */
@Repository
public interface WalletMasterRepository extends JpaRepository<WalletMaster, Integer> {
    Optional<WalletMaster> findByCountryCodeAndOperatorName(String countryCode, String operatorName);
}
