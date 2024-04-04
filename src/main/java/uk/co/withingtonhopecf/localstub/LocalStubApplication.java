package uk.co.withingtonhopecf.localstub;

import java.net.URI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootApplication
public class LocalStubApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalStubApplication.class, args);
	}

	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
			.credentialsProvider(DefaultCredentialsProvider.create())
			.region(Region.EU_WEST_1)
			.endpointOverride(URI.create("http://host.docker.internal:8000"))
			.build();
	}

	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
		return DynamoDbEnhancedClient.builder()
			.dynamoDbClient(dynamoDbClient())
			.build();
	}
}
