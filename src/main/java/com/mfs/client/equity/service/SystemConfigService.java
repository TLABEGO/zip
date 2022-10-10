package com.mfs.client.equity.service;

import static com.mfs.client.equity.utils.MFSEquityConstants.EQUITY_BANK_RW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mfs.client.equity.exception.MissingConfigValueException;
import com.mfs.client.equity.persistance.SystemConfigRepository;
import com.mfs.client.equity.persistance.model.SystemConfig;
import com.mfs.client.equity.persistance.model.SystemConfig.SystemConfigId;

import lombok.RequiredArgsConstructor;

/**
 * System configuration service used to acquire all the system configuration information
 * needed within the application
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

	final SystemConfigRepository configRepository;

	/**Adding a new system config key and value
	 *
	 * @param key text used to identify the value saved in the config table
	 * @param value text is the actual value which is to be added alongside the key
	 */
	public void addConfig(String key, String value) {
        Optional<SystemConfig> optional = configRepository.findByConfigKey(key);
        if(optional.isPresent())
            throw new RuntimeException("System configuration already contains value for key:" + key);

		SystemConfig config = new SystemConfig();
		SystemConfigId configId = SystemConfigId.builder().systemName(EQUITY_BANK_RW).build();
		config.setId(configId);
		config.setConfigKey(key);
		config.setConfigValue(value);
		configRepository.save(config);
	}

    /**
     * Updating the system configuration table
     *
     * @param key is the text used to find the system configuration value in the table
     * @param value is the text which carries the data that is used to update the configuration value
     */
    public void updateConfig(String key, String value) {
        Optional<SystemConfig> optional = configRepository.findByConfigKey(key);
        if(!optional.isPresent())
            throw new MissingConfigValueException("No such value defined in System configuration for key:" + key);

        SystemConfig config = optional.get();
        config.setConfigValue(value);
        configRepository.save(config);
    }

    /**
     * Deleting an existing configuration value in the database
     *
     * @param key is the text used to find the existing system configuration key and value
     */
    public void deleteConfig(String key) {
        Optional<SystemConfig> optional = configRepository.findByConfigKey(key);
        if(!optional.isPresent())
            throw new MissingConfigValueException("No such value defined in System configuration for key:" + key);

        SystemConfig config = optional.get();
        configRepository.delete(config);
    }


    /**
     * Getting configuration values by key
     *
     *
     * @param key represents the text which will used to retrieve the configuration value
     * @return configuration value
     */
    public String getConfigByKey(String key) {
        Optional<SystemConfig> config = configRepository.findByConfigKey(key);
        if(!config.isPresent())
            throw new MissingConfigValueException("No such value defined in System configuration for key:"+key);
        return config.get().getConfigValue();
    }


    /**
     * Getting configuration values
     *
     * @return a map of configuration values
     */
    public Map<String, String> getConfig() {
        final Map<String, String> systemConfigsMap = new HashMap<>();
        List<SystemConfig> systemConfigs = configRepository.findAll();
        systemConfigs.forEach(config -> systemConfigsMap.put(config.getConfigKey(), config.getConfigValue()));
        return systemConfigsMap;
    }

}
