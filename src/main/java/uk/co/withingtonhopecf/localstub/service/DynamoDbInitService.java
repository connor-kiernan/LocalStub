package uk.co.withingtonhopecf.localstub.service;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import uk.co.withingtonhopecf.localstub.model.Match;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbInitService {

	private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private final DynamoDbClient dynamoDbClient;

	private static final List<String> opponents = List.of(
		"Thames Valley Rovers FC",
		"Highland United FC",
		"Albion Rangers FC",
		"Lancashire Lions FC",
		"Yorkshire Royals FC",
		"Mersey City Wanderers",
		"Sussex Strikers FC",
		"Bristol City Blues",
		"Highbury Borough FC",
		"Celtic Valley FC"
	);

	@PostConstruct
	public void postConstruct() {
		initMatches();
	}

	public void initMatches() {
		DynamoDbTable<Match> table = dynamoDbEnhancedClient.table("matches", TableSchema.fromImmutableClass(Match.class));
		table.createTable();

		try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) {
			ResponseOrException<DescribeTableResponse> response = waiter
				.waitUntilTableExists(builder -> builder.tableName("matches").build())
				.matched();

			DescribeTableResponse tableDescription = response.response().orElseThrow(
				() -> new RuntimeException("matches table was not created."));

			log.info("matches table was created: {}", tableDescription);

			createMatches().forEach(table::putItem);
		}
	}

	private List<Match> createMatches() {
		List<Match> matches = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			for (int j = 1; j <= 5; j++) {
				matches.add(
					createMatch(j, i == 1)
				);
			}
		}

		return matches;
	}

	private Match createMatch(int seed, boolean played) {
		final ZonedDateTime baseZonedDateTime = ZonedDateTime.now()
			.withHour(10)
			.withMinute(15)
			.withSecond(0)
			.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
		ZonedDateTime kickOffTime = played ?
			baseZonedDateTime.minusWeeks(seed) :
			baseZonedDateTime.plusWeeks(seed);

		return Match.builder()
			.id(String.valueOf(played ? seed : seed * 5))
			.address(Map.of(
				"line1", seed + " Fake Street",
				"line2", "Sport Field",
				"postcode", "A%d%d %dBC".formatted((seed * 7) % 10, seed, seed)
			))
			.isHomeKit((seed % 2) == 0)
			.isHomeGame((seed % 2) == 0)
			.played(played)
			.kickOffDateTime(kickOffTime)
			.opponent(opponents.get(seed % opponents.size()))
			.build();
	}

}
