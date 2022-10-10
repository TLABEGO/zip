package com.mfs.client.equity.rest.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * This represents the various fields that will compose a transactionRequest as well as the associated custom
 * validation messages to be validated using JSR validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Payment reference should not be blank")
    private String paymentReference1;

    @NotBlank(message = "Payment reference should not be blank")
    private String paymentReference2;

    @NotBlank(message = "Beneficiary name should not be blank")
    private String beneficiaryName;

    @NotBlank(message = "Payment reference should not be blank")
    private String paymentReference3;

    @NotNull(message = "Fees should not be null")
    private Double fees;

    @NotBlank(message = "Sender country code should not be blank")
    private String senderCountryCode;

    @NotBlank(message = "Currency code should not be blank")
    private String currencyCode;

    @NotBlank(message = "Country code should not be blank")
    private String countryCode;

    @NotBlank(message = "Sender name should not be blank")
    private String senderName;

    @NotNull(message = "Transfer amount should not be null")
    private Double transAmount;

    private String accountNumber;
    private String bankName;
    
    private String operatorName;
    private String mobileNumber;
    
    private String bankCode;
    private String branchCode;
    private String remarks;
    private String data;

}
