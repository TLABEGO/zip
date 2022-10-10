package com.mfs.client.equity.service.impl;

import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_ALIAS;
import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_PATH;
import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_PWD;
import static com.mfs.client.equity.utils.MFSEquityConstants.MOCK_STATUS;
import static com.mfs.client.equity.utils.MFSEquityConstants.MOCK_TRANSFER;
import static com.mfs.client.equity.utils.MFSEquityConstants.SALT;
import static com.mfs.client.equity.utils.MFSEquityConstants.TRANSFER_MONEY_SERVICE;
import static org.springframework.util.StringUtils.isEmpty;

import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.ws.client.WebServiceIOException;

import com.mfs.client.equity.persistance.EventLogRepository;
import com.mfs.client.equity.persistance.TransactionLogRepository;
import com.mfs.client.equity.persistance.model.EventLog;
import com.mfs.client.equity.persistance.model.TransactionLog;
import com.mfs.client.equity.rest.dto.MFSResponse;
import com.mfs.client.equity.rest.dto.TransactionResponse;
import com.mfs.client.equity.service.SOAPService;
import com.mfs.client.equity.service.SystemConfigService;
import com.mfs.client.equity.utils.EquityBankResponseCode;
import com.mfs.client.equity.utils.MFSEquityEncryption;

import equitybank.ws.TranApplyRequest;
import equitybank.ws.TranApplyRequestResponse;
import equitybank.ws.TranApplyRequestStatus;
import equitybank.ws.TranApplyRequestStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * A mock implementation for the SOAP web service
 */
@Log4j2
@RequiredArgsConstructor
public class MockSOAPService implements SOAPService {

    final SystemConfigService configService;
    final TransactionLogRepository transactionLogRepository;
    final EventLogRepository eventLogRepository;

    /**
     * @param transactionLog has attributes required by the actual web service
     * @return tranApplyRequestResponse
     */
    @Override
    public MFSResponse<TransactionResponse> transferMoney(TransactionLog transactionLog) {
        EventLog eventLog = null;
        TransactionResponse transactionResponse = null;
        TranApplyRequest request = prepareTranApplyRequest(transactionLog);
        request.setCheck(moneyRequestChecksum(request));

        try {
            eventLog = eventLogRepository.save(createEventLog(TRANSFER_MONEY_SERVICE, mapper.writeValueAsString(request),
                    transactionLog.getPaymentRef1()));
            log.info("Equity submit transaction request for {}: {}", request.getPaymentReference1(), request);
            JAXBElement<TranApplyRequest> element = factory.createTranApplyRequest(request);
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(TranApplyRequest.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(element, writer);
            log.debug(writer.toString());

            TranApplyRequestResponse tranApplyRequestResponse = factory.createTranApplyRequestResponse();
            
            String response = configService.getConfigByKey(MOCK_TRANSFER);
            tranApplyRequestResponse.setReturn(response);
            log.info("Equity submit transaction response: {}: {}", transactionLog.getPaymentRef1(), tranApplyRequestResponse);
            eventLogRepository.save(updateEventLog(eventLog, mapper.writeValueAsString(tranApplyRequestResponse)));
            
            // Updating Transaction Log if web service invoking is a success
            List<String> returnCodes = Arrays.asList(tranApplyRequestResponse.getReturn().split("\\|"));
            String status = returnCodes.get(0);
            
            String referenceNumber = null;
            String responseCode = null;
            String responseDesc = null;
            String msgId = null;
            //Handling Success response
            if("OK".equalsIgnoreCase(status)) {
            	 referenceNumber = returnCodes.get(1);
            	 responseDesc = returnCodes.size() >= 3 ? returnCodes.get(2) : null;
            	 msgId = returnCodes.size() >= 4 ? returnCodes.get(3) : null;
            	 msgId = msgId!=null ? msgId.replaceAll("MSGID=", ""):msgId;
                 responseCode = String.valueOf(EquityBankResponseCode.TRANSACTION_APPROVED.getCode());
                 transactionLogRepository.save(updateTransactionLog(transactionLog, responseCode, responseDesc));
            }else {
            	EquityBankResponseCode bankResponseCode = EquityBankResponseCode.getResponse(returnCodes.get(1));
            	responseCode = String.valueOf(bankResponseCode.getCode());
            	responseDesc = bankResponseCode.getText();
            	transactionLogRepository.save(updateErrorResponse(transactionLog, responseCode, responseDesc));
            }
            
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .referenceNumber(referenceNumber)
                    .responseMessage(responseDesc)
                    .msgid(msgId)
                    .build();
        } catch (WebServiceIOException exception) {
            log.info("Equity submit transaction error response for {}: {}", transactionLog.getPaymentRef1(), exception.getLocalizedMessage());
            String responseDesc=exception.getLocalizedMessage();
            String responseCode=String.valueOf(HttpStatus.BAD_REQUEST.value());
            if (exception.getCause() instanceof ConnectTimeoutException) {
            	responseCode = EquityBankResponseCode.HOST_TIMEOUT.getCode();
            	responseDesc = EquityBankResponseCode.HOST_TIMEOUT.getText();
            } else if (exception.getCause() instanceof SocketTimeoutException) {
            	responseCode = EquityBankResponseCode.HOST_DOWN.getCode();
            	responseDesc = EquityBankResponseCode.HOST_DOWN.getText();
            } 
            eventLogRepository.save(updateEventLog(eventLog, responseDesc));
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .responseMessage(responseDesc)
                    .build();
            
        } catch (Exception exception) {
        	eventLogRepository.save(updateEventLog(eventLog, exception.getLocalizedMessage()));
            log.info("Equity submit transaction error response for {}: {}", transactionLog.getPaymentRef1(), exception.getCause());
            
            transactionResponse = TransactionResponse.builder()
                    .responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .build();
            
        }
        MFSResponse<TransactionResponse> mfsResponse = new MFSResponse<>();
        mfsResponse.setStatusCode(HttpStatus.OK.value());
        mfsResponse.setResponse(transactionResponse);
        return mfsResponse;
    }

    /**
     * Method accepts a SOAP request status and then returns a SOAP request
     * status response
     *
     * @param transactionLog has attributes required by the actual web
     *                       service
     * @return tranApplyRequestResponse
     */
    @Override
    public MFSResponse<TransactionResponse> getTransactionStatus(TransactionLog transactionLog) {
        EventLog eventLog = null;
        TransactionResponse transactionResponse = null;
        TranApplyRequestStatus request = prepareTranApplyStatusRequest(transactionLog);
        request.setCheck(statusRequestChecksum(request));
        try {
            eventLog = eventLogRepository.save(createEventLog(TRANSFER_MONEY_SERVICE, mapper.writeValueAsString(request),
                    transactionLog.getPaymentRef1()));
            log.info("Equity query transaction request for {}: {}", request.getPaymentReference1(), request);
            JAXBElement<TranApplyRequestStatus> element = factory.createTranApplyRequestStatus(request);
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(TranApplyRequestStatus.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(element, writer);
            log.debug(writer.toString());

            TranApplyRequestStatusResponse tranApplyRequestStatusResponse = factory.createTranApplyRequestStatusResponse();

            String response = configService.getConfigByKey(MOCK_STATUS);
            tranApplyRequestStatusResponse.setReturn(response);
            log.info("Equity query transaction response: {}: {}", transactionLog.getPaymentRef1(), tranApplyRequestStatusResponse);
            eventLogRepository.save(updateEventLog(eventLog, mapper.writeValueAsString(tranApplyRequestStatusResponse)));
         // Updating Transaction Log if web service invoking is a success
            List<String> returnCodes = Arrays.asList(tranApplyRequestStatusResponse.getReturn().split("\\|"));
            String status = returnCodes.get(0);
            
            String referenceNumber = null;
            String responseCode = null;
            String responseDesc = null;
            String msgId = null;
            //Handling Success response
            if("OK".equalsIgnoreCase(status)) {
            	 referenceNumber = returnCodes.get(1);
            	 responseDesc = returnCodes.size() >= 3 ? returnCodes.get(2) : null;
            	 msgId = returnCodes.size() >= 4 ? returnCodes.get(3) : null;
            	 msgId = msgId!=null ? msgId.replaceAll("MSGID=", ""):msgId;
                 responseCode = String.valueOf(EquityBankResponseCode.TRANSACTION_APPROVED.getCode());
                 transactionLogRepository.save(updateTransactionLog(transactionLog, responseCode, responseDesc));
            }else {
            	EquityBankResponseCode bankResponseCode = EquityBankResponseCode.getResponse(returnCodes.get(1));
            	responseCode = String.valueOf(bankResponseCode.getCode());
            	responseDesc = bankResponseCode.getText();
            }
            
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .referenceNumber(referenceNumber)
                    .responseMessage(responseDesc)
                    .msgid(msgId)
                    .build();
            
        } catch (WebServiceIOException exception) {
            log.info("Equity query transaction error response for {}: {}", transactionLog.getPaymentRef1(), exception.getLocalizedMessage());
            String responseDesc=exception.getLocalizedMessage();
            String responseCode=String.valueOf(HttpStatus.BAD_REQUEST.value());
            if (exception.getCause() instanceof ConnectTimeoutException) {
            	responseCode = EquityBankResponseCode.HOST_TIMEOUT.getCode();
            	responseDesc = EquityBankResponseCode.HOST_TIMEOUT.getText();
            } else if (exception.getCause() instanceof SocketTimeoutException) {
            	responseCode = EquityBankResponseCode.HOST_DOWN.getCode();
            	responseDesc = EquityBankResponseCode.HOST_DOWN.getText();
            } 
            eventLogRepository.save(updateEventLog(eventLog, responseDesc));
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .responseMessage(responseDesc)
                    .build();
        } catch (Exception exception) {
        	eventLogRepository.save(updateEventLog(eventLog, exception.getLocalizedMessage()));
            log.info("Equity query transaction error response for {}: {}", transactionLog.getPaymentRef1(), exception.getCause());
            transactionResponse = TransactionResponse.builder()
                    .responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .build();
        }
        MFSResponse<TransactionResponse> mfsResponse = new MFSResponse<>();
        mfsResponse.setStatusCode(HttpStatus.OK.value());
        mfsResponse.setResponse(transactionResponse);
        return mfsResponse;
    }

    private String statusRequestChecksum(TranApplyRequestStatus request) {
        /*
         * salt + MessageID + AgentID + PaymentReference1
         */
        StringBuilder builder = new StringBuilder();
        builder.append(configService.getConfigByKey(SALT)).append(request.getMessageID()).append(request.getAgentID())
                .append(request.getPaymentReference1());

        // Private key to be provided here for signing

        log.debug("Generated checksum: {}", builder.toString());

        String privateKeyPath = configService.getConfigByKey(KEY_PATH);
        String privateKeyAlias = configService.getConfigByKey(KEY_ALIAS);
        String privateKeyPwd = configService.getConfigByKey(KEY_PWD);
        String signedText = MFSEquityEncryption.signData(privateKeyPath, privateKeyAlias, privateKeyPwd, builder.toString());
        return signedText;
    }

    private String moneyRequestChecksum(TranApplyRequest request) {
        /*
         * This field is a checksum. The transaction acquiring organization/agent will
         * sign this field with their private Key. First the following fields are
         * concatenated. (SALT + (AccountNumber +BIC) + paymentReference1+ paymentType+
         * agentID+ tranAmount+ currencyCode+messageID) in that order (Salt to be
         * shared) (Mandatory). The resulting text is then signed with Private Key and
         * Base64 encoded.(Mandatory)
         */

        // Concatenating checksum fields
        StringBuilder builder = new StringBuilder();
        builder.append(configService.getConfigByKey(SALT));
        if (!isEmpty(request.getAccountNumber())) {
            builder.append(request.getAccountNumber());
            builder.append(request.getBicCode());
        }

        if (!isEmpty(request.getMobileNumber())) {
            builder.append(request.getWalletName());
            builder.append(request.getMobileNumber());
        }

                /*
        '{
    "mobileNumber": "+254711888555",
    "paymentReference1": "MFSTest2021121701",
    "paymentReference2": "Joy Obianaba",
    "beneficiaryName": "Test beneficiary",
    "paymentReference3": "Anusha Garlapati",
    "fees": 0.0,
    "senderCountryCode": "NG",
    "currencyCode": "KES",
    "countryCode": "KE",
    "senderName": "Joy Obianaba",
    "transAmount": 5000.00,
    "operatorName": "MPESA"
}
         */
        builder.append(request.getPaymentReference1());
        builder.append(request.getPaymentReference2());
        builder.append(request.getPaymentReference3());
        builder.append(request.getBeneficiaryName());
        builder.append(request.getTranAmount().toPlainString());
        builder.append(request.getCountryCode());
        builder.append(request.getCurrencyCode());
        builder.append(request.getSenderName());
        builder.append(request.getFees());


//        builder.append(request.getPaymentType());
//        builder.append(request.getAgentID());
//
//        builder.append(request.getCurrencyCode());
//        builder.append(request.getMessageID());

        log.debug("Generated checksum text:{}", builder.toString());

        // Private key to be provided here for signing
        String privateKeyPath = configService.getConfigByKey(KEY_PATH);
        String privateKeyAlias = configService.getConfigByKey(KEY_ALIAS);
        String privateKeyPwd = configService.getConfigByKey(KEY_PWD);
        String signedText = MFSEquityEncryption.signData(privateKeyPath, privateKeyAlias, privateKeyPwd, builder.toString());
        return signedText;
    }

}
