package com.mfs.client.equity.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

import com.mfs.client.equity.exception.EquityClientException;
import com.mfs.client.equity.persistance.EventLogRepository;
import com.mfs.client.equity.persistance.TransactionLogRepository;
import com.mfs.client.equity.persistance.model.EventLog;
import com.mfs.client.equity.utils.MFSEquityConstants;

import equitybank.ws.TranApplyRequest;
import equitybank.ws.TranApplyRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * This class handles logic to do with logging the SOAP request and response objects in the event log table
 * as well as exceptions thrown from the web service
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class SOAPLogHandler implements ClientInterceptor {

    final EventLogRepository eventLogRepository;
    final Jaxb2Marshaller marshaller;
    final TransactionLogRepository transactionLogRepository;

    /**
     * Writing the SOAP request into the out stream
     *
     * @param messageContext it has attributes required by the Message Context
     *                       object
     * @return boolean
     * @throws WebServiceClientException web service exception
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        log.debug("### SOAP REQUEST ###");
        try {
            String requestPayload = getRequestPayload(messageContext);
            JAXBElement<?> request = (JAXBElement<?>) marshaller.unmarshal(messageContext.getRequest().getPayloadSource());
            if (request.getValue() instanceof TranApplyRequest) {
                TranApplyRequest tranApplyRequest = (TranApplyRequest) request.getValue();
                log.info("Equity submit transaction request for {}: {}",
                        tranApplyRequest.getPaymentReference1(), requestPayload);
            } else if (request.getValue() instanceof TranApplyRequestStatus) {
                TranApplyRequestStatus tranApplyRequest = (TranApplyRequestStatus) request.getValue();
                log.info("Equity query transaction request for {}: {}",
                        tranApplyRequest.getPaymentReference1(), requestPayload);
            }
        } catch (Exception e) {
            throw new EquityClientException("Equity client exception", e);
        }

        return true;
    }

    /**
     * Writing the SOAP response into the out stream
     *
     * @param messageContext it has attributes required by the Message Context
     *                       object
     * @return boolean
     * @throws WebServiceClientException web service exception
     */
    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        log.debug("### SOAP RESPONSE ###");
        try {
            log.debug("Equity response: {}", getResponsePayload(messageContext));
        } catch (Exception e) {
            throw new EquityClientException("Equity client exception", e);
        }
        return true;
    }

    /**
     * Handling errors caused by the client service call
     *
     * @param messageContext is the request payload sent
     * @return boolean
     * @throws WebServiceClientException web service exception
     */
    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        log.debug("### SOAP FAULT ###");
        try {
            log.debug("SOAP response error {}", getResponsePayload(messageContext));
        } catch (Exception e) {
            throw new EquityClientException("Equity client exception", e);
        }
        return true;
    }

    /**
     * Handling the process after the request has been sent
     *
     * @param messageContext the request payload that is sent
     * @param ex             is the exception to be used within the method logic
     * @throws WebServiceClientException SOAP exception
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        EventLog eventLog = null;
        try {
            String requestPayload = getRequestPayload(messageContext);
            String responsePayload = getResponsePayload(messageContext);
            JAXBElement request = (JAXBElement) marshaller.unmarshal(messageContext.getRequest().getPayloadSource());
            if (request.getValue() instanceof TranApplyRequest) {

                TranApplyRequest tranApplyRequest = (TranApplyRequest) request.getValue();
                eventLog = EventLog.builder()
                        .mfsReferenceId(tranApplyRequest.getPaymentReference1())
                        .request(requestPayload)
                        .serviceName(MFSEquityConstants.TRANSFER_MONEY_SERVICE)
                        .dateLogged(new Date())
                        .build();
                eventLog = eventLogRepository.save(eventLog);
                log.info("Equity submit transaction response: {}: {}", tranApplyRequest.getPaymentReference1(), responsePayload);
            } else if (request.getValue() instanceof TranApplyRequestStatus) {

                TranApplyRequestStatus tranApplyRequest = (TranApplyRequestStatus) request.getValue();
                eventLog = EventLog.builder()
                        .mfsReferenceId(tranApplyRequest.getMessageID())
                        .request(requestPayload)
                        .serviceName(MFSEquityConstants.REMITTANCE_STATUS_SERVICE)
                        .dateLogged(new Date())
                        .build();
                eventLog = eventLogRepository.save(eventLog);
                log.info("Equity query transaction response for {}: {}", tranApplyRequest.getPaymentReference1(), responsePayload);
            }

            eventLog.setResponse(responsePayload);
            eventLogRepository.save(eventLog);

        } catch (Exception e) {
            eventLog.setResponse(e.getMessage());
            eventLogRepository.save(eventLog);
            throw new EquityClientException("Equity client exception", e);
        }
    }

    /**
     * TAcquiring the request payload from the message context
     *
     * @param messageContext the carrier of the of the request payload
     * @return a request payload as String object
     * @throws IOException                  IO exception
     * @throws UnsupportedEncodingException encoding exception
     */
    private String getRequestPayload(MessageContext messageContext) throws
            IOException, UnsupportedEncodingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        messageContext.getRequest().writeTo(buffer);
        return buffer.toString(java.nio.charset.StandardCharsets.UTF_8.name());
    }

    /**
     * Retrieving the response payload from calling the client service
     *
     * @param messageContext the carrier of the request payload
     * @return a response payload as String object
     * @throws IOException                  IO exception
     * @throws UnsupportedEncodingException encoding exception
     */
    private String getResponsePayload(MessageContext messageContext) throws
            IOException, UnsupportedEncodingException {
        SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
        if (soapMessage.hasFault()) {
            QName code = soapMessage.getSoapBody().getFault().getFaultCode();
            log.error("SOAP Fault",code);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        messageContext.getResponse().writeTo(buffer);
        return buffer.toString(java.nio.charset.StandardCharsets.UTF_8.name());
    }

}
