package com.mfs.client.equity.infrastructure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import com.mfs.client.equity.persistance.EventLogRepository;
import com.mfs.client.equity.persistance.TransactionLogRepository;
import com.mfs.client.equity.service.SOAPLogHandler;
import com.mfs.client.equity.service.SOAPService;
import com.mfs.client.equity.service.SystemConfigService;
import com.mfs.client.equity.service.impl.MockSOAPService;
import com.mfs.client.equity.service.impl.SOAPServiceImpl;

/**
 * This class is used for the configuration for the application
 */
@Configuration
public class MfsEquityConfig {

	/**
	 * Configuration of Jaxb2Marshaller
	 *
	 * @return Jax2BMarshaller object
	 */
	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		Map<String, Object> properties = new HashMap<>();
		properties.put("jaxb.formatted.output", true);
		marshaller.setMarshallerProperties(properties);
		marshaller.setContextPath("equitybank.ws");
		return marshaller;
	}

	/**
	 * Configuring mock soap service profile
	 *
	 * @param configService is the system config values used with the profile build
	 * @return SOAPService object
	 */
	@Bean
	@Profile("mock-soap")
	public SOAPService mockSoapService(final SystemConfigService configService, final EventLogRepository eventLogRepository,
			final TransactionLogRepository transactionLogRepository) {
		return new MockSOAPService(configService, transactionLogRepository, eventLogRepository);
	}

	/**
	 * Configuration for the actual SOAPService client
	 *
	 * @param configService      the system config values that will be used within
	 *                           the profile
	 * @param eventLogRepository database table in which transaction occurrences
	 *                           will be persisted
	 * @return bean of type SOAPService
	 */
	@Bean
	@Profile("!mock-soap")
	public SOAPService soapService(WebServiceTemplate template, final SystemConfigService configService, final EventLogRepository eventLogRepository,
			final TransactionLogRepository transactionLogRepository) {

		SOAPServiceImpl soapService = new SOAPServiceImpl(configService, transactionLogRepository, eventLogRepository);
		soapService.setWebServiceTemplate(template);
		return soapService;
	}

	@Bean
	@Profile("!mock-soap")
	public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, SOAPLogHandler logHandler, final SystemConfigService configService,
			final EventLogRepository eventLogRepository, final TransactionLogRepository transactionLogRepository) {
		WebServiceTemplate template = new WebServiceTemplate();
		template.setMarshaller(marshaller);
		template.setUnmarshaller(marshaller);
		template.setInterceptors(new ClientInterceptor[] { logHandler });
		return template;
	}

}
