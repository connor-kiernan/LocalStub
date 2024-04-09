package uk.co.withingtonhopecf.localstub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.withingtonhopecf.localstub.config.LocalStubConfigurationProperties;
import uk.co.withingtonhopecf.localstub.model.cognito.TokenRequest;
import uk.co.withingtonhopecf.localstub.model.cognito.TokenResponse;

@RestController
@RequestMapping("/cognito")
@RequiredArgsConstructor
public class AwsCognitoController {

	private final LocalStubConfigurationProperties config;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping("/")
	public String listUsers() throws IOException {
		return new ClassPathResource("output/userList.json").getContentAsString(StandardCharsets.UTF_8);
	}

	@GetMapping("/oauth2/authorize")
	public void redirect(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, HttpServletResponse response)
		throws IOException {
		response.sendRedirect(redirect_uri + "?code=code123");
	}

	@PostMapping(path = "/oauth2/token", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public TokenResponse token(@RequestBody MultiValueMap<String, String> tokenRequestMap)
		throws IOException {
		TokenRequest tokenRequest = objectMapper.convertValue(tokenRequestMap.toSingleValueMap(), TokenRequest.class);

		String token = Jwts.builder().setIssuer(config.issuerUrl())
			.setClaims(Map.of("cognito:groups", List.of("admin"), "username", "connor.kiernan"))
			.setIssuedAt(new Date())
			.setExpiration(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli())).compact();

		return switch (tokenRequest.grantType()) {
			case AUTHORIZATION_CODE -> {
				if (tokenRequest.code().equals("code123")) {
					yield new TokenResponse(token, "refreshTokenValue123");
				}
				throw new BadRequestException();
			}
			case REFRESH_TOKEN -> {
				if (tokenRequest.refreshToken().equals("refreshTokenValue123")) {
					yield  new TokenResponse(token);
				}
				throw new BadRequestException();
			}
		};
	}
}
