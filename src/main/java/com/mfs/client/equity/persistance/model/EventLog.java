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
 * An entity used to store data about the requests made within the application
 */
@Entity
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equity_bank_event_log")
public class EventLog implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "mfs_reference_id")
    private String mfsReferenceId;

    @Column(name = "request")
    private String request;

    @Column(name = "response")
    private String response;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "date_logged", nullable = false)
    private Date dateLogged;

}
