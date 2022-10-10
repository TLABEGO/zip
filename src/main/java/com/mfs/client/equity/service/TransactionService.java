package com.mfs.client.equity.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.stereotype.Service;

import com.mfs.client.equity.exception.DuplicateRequestException;
import com.mfs.client.equity.exception.NoSuchTransactionExistsException;
import com.mfs.client.equity.persistance.BankMasterRepository;
import com.mfs.client.equity.persistance.TransactionLogRepository;
import com.mfs.client.equity.persistance.WalletMasterRepository;
import com.mfs.client.equity.persistance.model.BankMaster;
import com.mfs.client.equity.persistance.model.TransactionLog;
import com.mfs.client.equity.persistance.model.WalletMaster;
import com.mfs.client.equity.rest.dto.MFSResponse;
import com.mfs.client.equity.rest.dto.TransactionRequest;
import com.mfs.client.equity.rest.dto.TransactionResponse;
import com.mfs.client.equity.utils.MFSEquityConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * This class performs all activities to do with a Transaction
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

    final SystemConfigService configService;
    final SOAPService soapService;
    final TransactionLogRepository repository;
    final BankMasterRepository bankRepository;
    final WalletMasterRepository walletRepository;

    /**
     * Creates money transfer request and calls EQUITY bank for generating Money transfer request
     *
     * @param transactionRequest request containing details for money transfer
     *                           request
     * @return response for money transfer request
     */
    public MFSResponse<TransactionResponse> transferMoney(TransactionRequest transactionRequest) {
        // Validating the request
        validateRequest(transactionRequest);

        // If transaction is a duplicate
        TransactionLog transactionLog = null;
        try {
             transactionLog = repository.findByPaymentRef1(transactionRequest.getPaymentReference1());
            if (transactionLog != null) {
                throw new DuplicateRequestException("Transaction exists for payment reference " + transactionRequest.getPaymentReference1());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Creating a log in Transaction Log
        transactionLog = createTransactionLog(transactionRequest);

        // Invoking processTransaction web service if transaction is not a duplicate
        MFSResponse<TransactionResponse> response = soapService.transferMoney(transactionLog);
        log.info("Response: {}", response.getResponse());
        return response;
    }

    /**
     * Method helps to checking status of remittance (money transfer request) created by client.
     *
     * @param paymentReference unique payment reference for a transaction
     * @return transaction response returned after calling the client
     */
    public MFSResponse<TransactionResponse> getTransactionStatus(String paymentReference) {
        // check for valid transaction
        TransactionLog transactionLog = repository.findByPaymentRef1(paymentReference);
        if (transactionLog == null) {
            throw new NoSuchTransactionExistsException(paymentReference);
        }
        // calling remittance web service to get status of transaction
        MFSResponse<TransactionResponse> response = soapService.getTransactionStatus(transactionLog);
        log.info("Response: {}", response.getResponse());
        return response;

    }

    /**
     * Persisting the transactionRequest to the transactionLog table
     *
     * @param transactionRequest request containing details for a money transfer transaction
     * @return Transaction log object
     */
    private TransactionLog createTransactionLog(TransactionRequest transactionRequest) {
        String bicCode = null;
        String instaCode = null;
        String walletName = null;
        if (!isEmpty(transactionRequest.getAccountNumber())) {
            BankMaster bankMaster = bankRepository.findByCountryCodeAndBankName(transactionRequest.getCountryCode(),
                    transactionRequest.getBankName())
                    .orElseThrow(() -> new ConstraintViolationException("Bank record could not be found", null));
            bicCode = bankMaster.getBicCode();
            instaCode = bankMaster.getInstCode();
        }
        if (!isEmpty(transactionRequest.getOperatorName())) {
            WalletMaster walletMaster = walletRepository.findByCountryCodeAndOperatorName(
                    transactionRequest.getCountryCode(), transactionRequest.getOperatorName())
                    .orElseThrow(() -> new ConstraintViolationException("Wallet record could not be found", null));
            instaCode = walletMaster.getInstaCode();
            walletName = walletMaster.getWalletName();
        }

        TransactionLog transactionLog = TransactionLog.builder()
                .accountNumber(transactionRequest.getAccountNumber())
                .agentId(configService.getConfigByKey(MFSEquityConstants.AGENT_ID))
                .amount(transactionRequest.getTransAmount())
                .beneficiaryName(transactionRequest.getBeneficiaryName())
                .bicCode(bicCode)
                .instaCode(instaCode)
                .branchCode(transactionRequest.getBranchCode())
                .countryCode(transactionRequest.getCountryCode())
                .data(transactionRequest.getData())
                .currencyCode(transactionRequest.getCurrencyCode())
                .dateLogged(new Date())
                .fee(transactionRequest.getFees())
                .messageId(transactionRequest.getPaymentReference1())
                .mobileNumber(transactionRequest.getMobileNumber())
                .walletName(walletName)
                .paymentRef1(transactionRequest.getPaymentReference1())
                .paymentType(configService.getConfigByKey(MFSEquityConstants.PAYMENT_TYPE))
                .paymentRef2(transactionRequest.getPaymentReference2())
                .paymentRef3(transactionRequest.getPaymentReference3())
                .transactionDate(new Date())
                .remarks(transactionRequest.getRemarks())
                .senderCountryCode(transactionRequest.getSenderCountryCode())
                .senderName(transactionRequest.getSenderName()).build();
        return repository.save(transactionLog);
    }

    /**
     * Checks mandatory fields for request
     *
     * @param request transaction request object
     */
    private void validateRequest(TransactionRequest request) {
        if (request == null) {
            throw new ConstraintViolationException("Request should not null", null);
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (request.getTransAmount() <= 0) {
            throw new ConstraintViolationException("Amount should be greater than zero", null);
        }

        if (!isEmpty(request.getAccountNumber()) && isEmpty(request.getBankName())) {
            throw new ConstraintViolationException("Bank name is required", null);
        }

        if (!isEmpty(request.getOperatorName()) && isEmpty(request.getMobileNumber())) {
            throw new ConstraintViolationException("Mobile number is required", null);
        }
    }

}
