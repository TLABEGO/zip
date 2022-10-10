package com.mfs.client.equity.persistance.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An entity responsible for storing necessary data for the application
 * default system values
 */
@Entity
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_config")
public class SystemConfig implements Serializable {

    @EmbeddedId
    private SystemConfigId id;

    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    @Data
    public static class SystemConfigId implements Serializable {
    	
        @NotNull(message = "config id is required")
        @Column(name = "config_id", nullable = false)
        private Integer configId;
        @Column(name = "system_name")
        private String systemName;
    }


}
