package com.gcp.storage.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GoogleCloudConfig {

	@Bean
	Storage storage() throws IOException {
		InputStream credentialsStream = new ClassPathResource("credentials.json").getInputStream();
		GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
		return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
	}
}
