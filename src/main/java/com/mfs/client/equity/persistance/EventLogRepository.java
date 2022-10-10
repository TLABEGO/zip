package com.mfs.client.equity.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mfs.client.equity.persistance.model.EventLog;

/**
 * Event log repository responsible for capturing data of a transaction
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    /**
     * Retrieving an existing event log object using the payment reference 1
     *
     * @param mfsReferenceId it is the text used to find the existing event log
     * @return Event Log object
     */
    EventLog findByMfsReferenceId(String mfsReferenceId);

}
