package com.mfs.client.equity.rest;

import com.mfs.client.equity.rest.dto.MFSResponse;
import com.mfs.client.equity.rest.dto.TransactionRequest;
import com.mfs.client.equity.rest.dto.TransactionResponse;
import com.mfs.client.equity.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This controller takes care of all endpoints to do with a transaction or for
 * the Remittance API
 */
@RequiredArgsConstructor
@RestController
@Log4j2
public class TransactionController {

    final TransactionService transactionService;

    /**
     * End-point for initiating money transfer transaction
     *
     * @param transactionRequest which represent a transactionRequest
     * @return ResponseEntity object
     */
    @ResponseBody
    @PostMapping(value = "/equity/transferMoney", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transferMoney(@RequestBody TransactionRequest transactionRequest) {
        log.info("Request: {}", transactionRequest);
        MFSResponse<TransactionResponse> response = transactionService.transferMoney(transactionRequest);
        return new ResponseEntity<>(response.getResponse(), HttpStatus.valueOf(response.getStatusCode()));
    }

    /**
     * End-point for initiating money transfer status
     *
     * @param paymentReference money transfer status request as a string
     * @return response of transfer status
     */
    @GetMapping(value = "/equity/getStatus/{paymentReference}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> statusAPI(@PathVariable String paymentReference) {
        log.info("Payment reference: {}", paymentReference);
        MFSResponse<TransactionResponse> response = transactionService.getTransactionStatus(paymentReference);
        return new ResponseEntity<>(response.getResponse(), HttpStatus.valueOf(response.getStatusCode()));
    }
}
