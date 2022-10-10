package com.mfs.client.equity.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mfs.client.equity.persistance.model.TransactionLog;
/**
 * Transaction repository responsible for saving and retrieving transaction details
 */
@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog,Long> {

    /**
     * Retrieving an existing transaction log object using the payment reference 1
     *
     * @param paymentRef1 it is the text used to find the existing event log
     *
     * @return Transaction Log object
     */
    TransactionLog findByPaymentRef1(String paymentRef1);
}
