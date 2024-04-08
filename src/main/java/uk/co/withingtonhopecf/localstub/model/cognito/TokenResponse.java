package uk.co.withingtonhopecf.localstub.model.cognito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(SnakeCaseStrategy.class)
public record TokenResponse(String accessToken, String refreshToken) {

	public TokenResponse(String accessToken) {
		this(accessToken, null);
	}
}