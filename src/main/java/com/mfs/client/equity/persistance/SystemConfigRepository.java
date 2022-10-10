package com.mfs.client.equity.persistance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mfs.client.equity.persistance.model.SystemConfig;

/**
 * System Config repository used to retrieve the system config values needed for the application
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, SystemConfig.SystemConfigId> {
	/**
	 * Retrieving an existing system configuration objects using the system name
	 *
	 * @param equityBankRw it is the text used to find the existing system config value
	 * @param key it is the text used to identify the config value in the system config db table
	 * @return a list of System Configuration objects
	 */
	SystemConfig findByIdSystemNameAndConfigKey(String equityBankRw, String key);
	/**
	 *Retrieving an existing system configuration objects using the config key.
	 *
	 * @param configKey it is the text used to find the existing system configuration.
	 * @return System Configuration objects
	 */
	Optional<SystemConfig> findByConfigKey(String configKey);
}
