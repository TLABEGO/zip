package com.mfs.client.equity.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * This represents the various fields that will compose a transactionResponse
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
	
	//@Length(max = 100)
	//@NotBlank(message = "Account number should not be blank")
	//@NotEmpty(message = "Account number should not be empty")
	private String paymentReference1;
	
	private String responseCode;
	private String responseMessage;
	private String referenceNumber;
	private String msgid;

}
