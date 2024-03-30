package uk.co.withingtonhopecf.localstub.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cognito")
public class AwsCognitoController {

	@PostMapping("/")
	public String listUsers() throws IOException {
		return new ClassPathResource("output/userList.json").getContentAsString(StandardCharsets.UTF_8);
	}
}
