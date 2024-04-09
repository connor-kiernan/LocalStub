package uk.co.withingtonhopecf.localstub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "local-stub")
public record LocalStubConfigurationProperties(
	String dynamoDbUrl,
	String issuerUrl
) {}