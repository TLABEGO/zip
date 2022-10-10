package com.mfs.client.equity.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This the enum of all the response codes that state the nature of the request being sent for processing
 */
@Getter
@AllArgsConstructor
public enum EquityBankResponseCode {

    SYSTEM_ERROR("-1", "Validation: System error"),
    MISSING_PARAMETERS("-2", "Validation: failed Missing Parameters"),
    CORE_BANKING_ERROR("-3", "Transaction Failed- Core banking system general error"),
    FAILED_3RD_PARTY("-4", "Transaction Failed- Failed to reach third party system"),
    DUPLICATE_TRANSACTION("-7", "Validation:Transaction Duplicate"),
    INVALID_WALLET_CODE("-9", "Invalid or Unrecognized Wallet code"),
    DEPOSIT_LIMIT_EXCEEDS("-10", "Validation Deposit Limits exceeded"),
    AML_FAILURE("-12", "AML Failure"),
    TRANSACTION_INTERGITY_FAILED("-99", "Validation: Transaction Integrity Failed"),

    TRANSACTION_APPROVED("000", "Transaction Approved"),

    PAYMENT_TRANSACTION_NOT_FOUND("77", "Payment Transaction Not found"),
    INVALID_ACCOUNT_NO("114", "Invalid Account Number"),
    INVALID_PROCESSING_CODE("115", "Invalid processing code"),
    INSUFFICIENT_FUNDS("116", "Insufficient Funds"),
    TRANSFER_NOT_PERMITTED("119", "Transaction Not Permitted"),
    WITHDRAWAL_LIMIT_EXCEEDED("121", "Withdrawal limit exceeded"),
    TRANSFER_LIMIT_EXCEEDED("180", "Transfer limit exceeded"),

    INVALID_FUNCTION_CODE("902", "Invalid function code"),
    MSG_INCORRECT_FORMAT("904", "Message incorrectly formatted"),
    CUT_OVER_IN_PROGESS("906", "Cut over (end of day) in progress"),
    HOST_DOWN("907", "Processing host is down"),
    HOST_NOT_REACHABLE("908", "Processing host cannot be reached"),
    SYSTEM_MAL_FUNCTION("909", "System Malfunction"),
    HOST_TIMEOUT("911", "Host timed out"),

    TRANSACTION_REJECT_AML_REASONS("992", "Transaction rejected because of AML reasons"),
    TRANSACTION_HELD_AML_INVESTIGATION("993", "Transaction Held for AML investigation"),
    EQUITY_CLIENT_EXCEPTION("404", "Equity client exception"),
    
    //MPESA error
    MPESA_ERROR_2("2","Limit rule: less than the minimum transaction amount"),
    MPESA_ERROR_5("5","IMT MPESA validation failed (Includes invalid mobile number, MPESA AML checks. and any other validations)"),
    MPESA_ERROR_7("7","Reciever mobile number is in invalid"),
    MPESA_ERROR_8("8","Would exceed the maximum balance"),
    MPESA_ERROR_14("14","Reciever mobile number is in an invalid state"),
    MPESA_ERROR_2040("2040","Reciever mobile number is unregistered in MPesa");


    private String code;
    private String text;
    
    

    String getDescription() {
        return "[" + code + "]" + text;
    }
    
    public static EquityBankResponseCode getResponse(String code) {
    	for (EquityBankResponseCode bankResponseCode : EquityBankResponseCode.values()) {
			if(bankResponseCode.getCode().equals(code)) {
				return bankResponseCode;
			}
		}
    	throw new IllegalArgumentException("No response code defined!");
    }

}
