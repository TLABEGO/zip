package com.mfs.client.equity.persistance.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An entity responsible for storing data for the Transaction log
 */
@Entity
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equity_bank_transaction_log")
public class TransactionLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_log_id")
    private Long transactionLogId;

    @Column(name = "date_logged", nullable = false)
    private Date dateLogged;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "beneficiary_name", nullable = false)
    private String beneficiaryName;

    @Column(name = "payment_ref_1", nullable = false)
    private String paymentRef1;

    @Column(name = "payment_ref_2", nullable = false)
    private String paymentRef2;

    @Column(name = "payment_ref_3", nullable = false)
    private String paymentRef3;

    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "data")
    private String data;

    @Column(name = "insta_code", nullable = false)
    private String instaCode;

    @Column(name = "fee", nullable = false)
    private Double fee;

    @Column(name = "bic_code")
    private String bicCode;

    @Column(name = "wallet_name")
    private String walletName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_country_code")
    private String senderCountryCode;

    @Column(name = "service_status")
    private String serviceStatus;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "response_desc")
    private String responseDesc;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

}
