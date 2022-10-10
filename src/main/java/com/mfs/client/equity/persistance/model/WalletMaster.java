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
 * Contains fields to do with a mobile wallet
 */
@Entity
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equity_wallet_master")
public class WalletMaster implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_master_id")
    private int walletMasterId;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "wallet_name")
    private String walletName;

    @Column(name = "insta_code")
    private String instaCode;
}
