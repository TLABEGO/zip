package com.mfs.client.equity.rest;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mfs.client.equity.service.SystemConfigService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for refreshing System configuration.
 * 
 */
@RequiredArgsConstructor
@RestController
public class ManagementController {
	
	final SystemConfigService configService;
	/**
	 * An endpoint used to show/retrieve all the system config values
	 *
	 * @return Map object with a list of system config values
	 */
    @GetMapping(value = "/equity/showConfigs", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> listConfig() {
		return configService.getConfig();
	}

	/**
	 * An endpoint for adding new system config values
	 *
	 * @param key is used to offer the name of the value to be entered
	 * @param configValue is the actual data that is inserted alongside the key
	 * @return Map object with a list of all the system config values including the newly added
	 */
    @PostMapping(value = "/equity/", produces = MediaType.APPLICATION_JSON_VALUE)
   	public Map<String, String> addConfig(@RequestParam String key,@RequestParam String configValue) {
   		configService.addConfig(key,configValue);
   		return configService.getConfig();
   	}

	/**
	 * An endpoint for updating the system config values
	 *
	 * @param key used to identify the stored key and value pair
	 * @param configValue the actual value of the key that is going to be changed
	 * @return Map object with a list of the system config values including the newly updated
	 */
    @PutMapping(value = "/equity/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
   	public Map<String, String> updateConfig(@PathVariable String key,@RequestParam String configValue) {
   		configService.updateConfig(key,configValue);
   		return configService.getConfig();
   	}

	/**
	 * An endpoint used for deleting an existing system config value
	 *
	 * @param key is used for finding the existing system config value that will be deleted
	 * @return Map object with a list of the system config values excluding the recently deleted
	 */
    @DeleteMapping(value = "/equity/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
   	public Map<String, String> deleteConfig(@PathVariable String key) {
   		configService.deleteConfig(key);
   		return configService.getConfig();
   	}
}
