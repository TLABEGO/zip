package com.mfs.client.equity.persistance.model;

import java.io.Serializable;

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
 * Contains fields to do with a bank
 */
@Entity
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equity_bank_master")
public class BankMaster implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_master_id")
    private int bankMasterId;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bic_code")
    private String bicCode;

    @Column(name = "inst_code")
    private String instCode;
}
