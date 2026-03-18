package com.java.community.assistant.config;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OciConfig {

	@Value("${oci.config.path}")
	private String configFilePath;

	@Value("${oci.config.profile}")
	private String profile;

	/**
	 * Configura o provedor de autenticação usando o arquivo local da OCI.
	 * Isso demonstra o uso de práticas recomendadas de segurança.
	 */
	@Bean
	public AuthenticationDetailsProvider authenticationDetailsProvider() throws IOException {
		return new ConfigFileAuthenticationDetailsProvider(configFilePath, profile);
	}

	/**
	 * Inicializa o cliente de IA Generativa da OCI.
	 * Este bean será injetado nos seus serviços para realizar chamadas à IA.
	 */
	@Bean
	public GenerativeAiInferenceClient generativeAiInferenceClient(AuthenticationDetailsProvider provider) {
		return GenerativeAiInferenceClient.builder()
				.build(provider);
	}
}
