package com.mfs.client.equity.service.impl;

import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_ALIAS;
import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_PATH;
import static com.mfs.client.equity.utils.MFSEquityConstants.KEY_PWD;
import static com.mfs.client.equity.utils.MFSEquityConstants.SALT;
import static com.mfs.client.equity.utils.MFSEquityConstants.WS_URL;
import static org.springframework.util.StringUtils.isEmpty;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBElement;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.HttpStatus;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender.RemoveSoapHeadersInterceptor;

import com.mfs.client.equity.persistance.EventLogRepository;
import com.mfs.client.equity.persistance.TransactionLogRepository;
import com.mfs.client.equity.persistance.model.TransactionLog;
import com.mfs.client.equity.rest.dto.MFSResponse;
import com.mfs.client.equity.rest.dto.TransactionResponse;
import com.mfs.client.equity.service.SOAPService;
import com.mfs.client.equity.service.SystemConfigService;
import com.mfs.client.equity.utils.EquityBankResponseCode;
import com.mfs.client.equity.utils.MFSEquityEncryption;

import equitybank.ws.ObjectFactory;
import equitybank.ws.TranApplyRequest;
import equitybank.ws.TranApplyRequestResponse;
import equitybank.ws.TranApplyRequestStatus;
import equitybank.ws.TranApplyRequestStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * This class will handle the actual SOAP web service calls for a transaction
 */
@Log4j2
@RequiredArgsConstructor
public class SOAPServiceImpl extends WebServiceGatewaySupport implements SOAPService {

    final SystemConfigService configService;
    final TransactionLogRepository transactionLogRepository;
    final EventLogRepository eventLogRepository;

    /**
     * Calling the web service for a transaction
     *
     * @param transactionLog attributes required by the transApplyRequest object
     * @return tranApplyRequestResponse which carries a return method with
     * HttpStatus and message
     */
    @SuppressWarnings("unchecked")
    @Override
    public MFSResponse<TransactionResponse> transferMoney(TransactionLog transactionLog) {
    	TransactionResponse transactionResponse = null;
        TranApplyRequest request = prepareTranApplyRequest(transactionLog);
        request.setCheck(moneyRequestChecksum(request));
        try {
            getWebServiceTemplate().setDefaultUri(configService.getConfigByKey(WS_URL));
            skipSSLVerification();
            
            JAXBElement<TranApplyRequestResponse> element = (JAXBElement<TranApplyRequestResponse>) getWebServiceTemplate().marshalSendAndReceive(factory.createTranApplyRequest(request));
            TranApplyRequestResponse response = element.getValue();

            // Updating Transaction Log if web service invoking is a success
            List<String> returnCodes = Arrays.asList(response.getReturn().split("\\|"));
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
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .responseMessage(responseDesc)
                    .build();
            
        } catch (Exception exception) {
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
     * Calling the web service for a transaction status
     *
     * @param transactionLog it has attributes required by the transApplyRequestStatus
     *                       object
     * @return tranApplyRequestStatusResponse
     */
    @SuppressWarnings("unchecked")
    @Override
    public MFSResponse<TransactionResponse> getTransactionStatus(TransactionLog transactionLog) {
    	TransactionResponse transactionResponse = null;
        TranApplyRequestStatus request = prepareTranApplyStatusRequest(transactionLog);
        request.setCheck(statusRequestChecksum(request));
        try {
        	getWebServiceTemplate().setDefaultUri(configService.getConfigByKey(WS_URL));
        	skipSSLVerification();

            JAXBElement<TranApplyRequestStatusResponse> element = (JAXBElement<TranApplyRequestStatusResponse>) getWebServiceTemplate()
                    .marshalSendAndReceive(new ObjectFactory().createTranApplyRequestStatus(request));

            TranApplyRequestStatusResponse response = element.getValue();

            List<String> returnCodes = Arrays.asList(response.getReturn().split("\\|"));
            String referenceNumber = null;
            String responseCode = null;
            String responseDesc = null;
            String msgId = null;
            
            String status = returnCodes.get(0);
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
            transactionResponse = TransactionResponse.builder()
                    .paymentReference1(transactionLog.getPaymentRef1())
                    .responseCode(responseCode)
                    .responseMessage(responseDesc)
                    .build();
        } catch (Exception exception) {
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
        builder.append(request.getPaymentReference1());
        builder.append(request.getPaymentType());
        builder.append(request.getAgentID());
        builder.append(request.getTranAmount().toPlainString());
        builder.append(request.getCurrencyCode());
        builder.append(request.getMessageID());
        ;

        log.debug("Generated checksum text:{}", builder.toString());

        // Private key to be provided here for signing
        String privateKeyPath = configService.getConfigByKey(KEY_PATH);
        String privateKeyAlias = configService.getConfigByKey(KEY_ALIAS);
        String privateKeyPwd = configService.getConfigByKey(KEY_PWD);
        String signedText = MFSEquityEncryption.signData(privateKeyPath, privateKeyAlias, privateKeyPwd, builder.toString());
        return signedText;
    }
    
	private void skipSSLVerification() throws Exception {

		String urlPattern = "^(http|https|ftp)://.*$";
		if (getWebServiceTemplate().getDefaultUri().matches(urlPattern)) {
			final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(csf).addInterceptorFirst(new RemoveSoapHeadersInterceptor()).build();
			getWebServiceTemplate().setMessageSender(new HttpComponentsMessageSender(httpClient));
		}
 }


}
