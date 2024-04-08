package uk.co.withingtonhopecf.localstub.model.cognito;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record TokenRequest(
	GrantType grantType,
	String clientId,
	String redirectUri,
	String code,
	String refreshToken
) {
	public enum GrantType {
		AUTHORIZATION_CODE, REFRESH_TOKEN;

		@Override
		@JsonValue
		public String toString() {
			return this.name().toLowerCase();
		}
	}
}
