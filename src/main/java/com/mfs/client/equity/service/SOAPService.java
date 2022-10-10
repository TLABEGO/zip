package com.mfs.client.equity.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfs.client.equity.persistance.model.EventLog;
import com.mfs.client.equity.persistance.model.TransactionLog;
import com.mfs.client.equity.rest.dto.MFSResponse;
import com.mfs.client.equity.rest.dto.TransactionResponse;

import equitybank.ws.ObjectFactory;
import equitybank.ws.TranApplyRequest;
import equitybank.ws.TranApplyRequestStatus;

/**
 * A contract of the transaction SOAP client
 */
public interface SOAPService {

    ObjectFactory factory = new ObjectFactory();
    ObjectMapper mapper = new ObjectMapper();

    MFSResponse<TransactionResponse> transferMoney(TransactionLog transactionLog);

    MFSResponse<TransactionResponse> getTransactionStatus(TransactionLog transactionLog);

    default TranApplyRequest prepareTranApplyRequest(TransactionLog transactionLog) {

        BigDecimal amount = BigDecimal.valueOf(transactionLog.getAmount()).setScale(2, RoundingMode.HALF_DOWN);

        TranApplyRequest request = factory.createTranApplyRequest();

        if (!isEmpty(transactionLog.getAccountNumber())) {
            request.setAccountNumber(transactionLog.getAccountNumber());
            request.setBicCode(transactionLog.getBicCode());
            request.setBankCode(transactionLog.getBankCode());
            request.setBranchCode(transactionLog.getBranchCode());
        }else if (!isEmpty(transactionLog.getMobileNumber())) {
            request.setMobileNumber(transactionLog.getMobileNumber());
            request.setWalletName(transactionLog.getWalletName());
        }

        request.setInstCode(transactionLog.getInstaCode());
        request.setPaymentReference1(transactionLog.getPaymentRef1());
        request.setPaymentReference2(transactionLog.getPaymentRef2());
        request.setPaymentReference3(transactionLog.getPaymentRef3());
        request.setPaymentType(transactionLog.getPaymentType());
        request.setRemarks(transactionLog.getRemarks());
        request.setAgentID(transactionLog.getAgentId());
        request.setFees(BigDecimal.valueOf(transactionLog.getFee()));
        request.setTranAmount(amount);
        request.setData(transactionLog.getData());
        request.setMessageID(transactionLog.getMessageId());
        request.setCountryCode(transactionLog.getCountryCode());
        request.setCurrencyCode(transactionLog.getCurrencyCode());

        request.setSenderName(transactionLog.getSenderName());
        request.setSenderCountryCode(transactionLog.getSenderCountryCode());

        return request;
    }

    default TranApplyRequestStatus prepareTranApplyStatusRequest(TransactionLog transactionLog) {
        TranApplyRequestStatus request = factory.createTranApplyRequestStatus();
        request.setMessageID(transactionLog.getPaymentRef1());
        request.setAgentID(transactionLog.getAgentId());
        request.setPaymentReference1(transactionLog.getPaymentRef1());
        return request;
    }

    /**
     * A method to update the transactionLog after the webService call
     *
     * @param transactionLog saved transactionLog to be updated after WS call
     * @param responseCode   WS response code
     * @param responseDesc   WS response description
     * @return transactionLog object
     */
    default TransactionLog updateTransactionLog(TransactionLog transactionLog, String responseCode, String responseDesc) {
        transactionLog.setResponseCode(responseCode);
        transactionLog.setResponseDesc(responseDesc);
        return transactionLog;
    }

    default TransactionLog updateErrorResponse(TransactionLog transactionLog, String responseCode, String responseDesc) {
        transactionLog.setResponseCode(responseCode);
        transactionLog.setResponseDesc(responseDesc);
        transactionLog.setErrorCode(responseCode);
        transactionLog.setErrorMessage(responseDesc);
        return transactionLog;
    }

    /**
     * Saving an event log in eventLogRepository
     *
     * @param serviceName       represents the name of the service to be saved
     * @param request           represents the SOAP request that will be used for creating the event log
     * @param paymentReference1 represents a unique reference number from MFS
     * @return saved event log
     */
    default EventLog createEventLog(String serviceName, String request, String paymentReference1) {
        return EventLog.builder()
                .mfsReferenceId(paymentReference1)
                .request(request)
                .serviceName(serviceName)
                .dateLogged(new Date())
                .build();
    }

    /**
     * Updating an existing event log
     *
     * @param eventLog represents the event log that will be updated
     * @param response represents the response obtained after invoking the client service
     */
    default EventLog updateEventLog(EventLog eventLog, String response) {
        eventLog.setResponse(response);
        eventLog.setDateLogged(new Date());
        return eventLog;
    }
}
