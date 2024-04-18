package uk.co.withingtonhopecf.localstub.service;

import static java.util.Map.entry;
import static uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus.AVAILABLE;
import static uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus.FAN_CLUB;
import static uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus.IF_DESPERATE;
import static uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus.UNAVAILABLE;
import static uk.co.withingtonhopecf.localstub.model.enums.PitchType.ASTRO;
import static uk.co.withingtonhopecf.localstub.model.enums.PitchType.GRASS;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import uk.co.withingtonhopecf.localstub.model.Availability;
import uk.co.withingtonhopecf.localstub.model.Match;
import uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbInitService {

	private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private final DynamoDbClient dynamoDbClient;

	private static final List<String> OPPONENTS = List.of(
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

	private static final Map<String, String> HOME_ADDRESS = Map.of(
		"line1", "Hough End Playing Fields",
		"line2", " 480 Princess Rd",
		"postcode", "M20 1HP"
	);

	@PostConstruct
	public void postConstruct() {
		initMatches();
	}

	public void initMatches() {
		log.info("Initialising matches table");

		DynamoDbTable<Match> table = dynamoDbEnhancedClient.table("matches", TableSchema.fromImmutableClass(Match.class));

		try {
			table.createTable();
		} catch (ResourceInUseException ex) {
			log.info("Table already exists, resetting data anyway...");
		}

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

	private static List<Match> createMatches() {
		List<Match> matches = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			for (int j = 1; j <= 5; j++) {
				matches.add(
					createMatch(j, i == 1)
				);

				if(i != 1) {
					matches.add(createTraining(j));
				}
			}
		}

		return matches;
	}

	private static Match createMatch(int seed, boolean played) {
		final ZonedDateTime baseZonedDateTime = ZonedDateTime.now()
			.withHour(10)
			.withMinute(15)
			.withSecond(0)
			.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
		ZonedDateTime kickOffTime = played ?
			baseZonedDateTime.minusWeeks(seed) :
			baseZonedDateTime.plusWeeks(seed);

		final boolean isHomeGame = (seed % 2) != 0;

		Map<String, String> address = isHomeGame ?
			HOME_ADDRESS :
			Map.of(
				"line1", seed + " Fake Street",
				"line2", "Sport Field",
				"postcode", "A%d%d %dBC".formatted((seed * 7) % 10, seed, seed)
			);

		final int homeGoalsIfPlayed = isHomeGame ? 2 : 0;
		final int awayGoalsIfPlayer = isHomeGame ? 0 : 2;

		int homeGoals = played ? homeGoalsIfPlayed : -1;
		int awayGoals = played ? awayGoalsIfPlayer : -1;

		return Match.builder()
			.id(String.valueOf(!played ? seed : seed + 5))
			.address(address)
			.isHomeKit(isHomeGame)
			.isHomeGame(isHomeGame)
			.played(played)
			.kickOffDateTime(kickOffTime)
			.opponent(OPPONENTS.get(seed % OPPONENTS.size()))
			.homeGoals(homeGoals)
			.awayGoals(awayGoals)
			.withyGoalScorers(played ? Map.of("Kiernan", 2) : null)
			.pitchType(isHomeGame ? GRASS : ASTRO)
			.playerAvailability(createPlayerAvailability(seed))
			.eventType("GAME")
			.build();
	}

	private static Map<String, Availability> createPlayerAvailability(int seed) {
		return IntStream.range(1, 20)
			.mapToObj(String::valueOf)
			.map(sub -> createAvailability(sub, seed))
			.filter(Objects::nonNull)
		.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private static Entry<String, Availability> createAvailability(String sub, int seed) {
		int enumIndex = (Integer.parseInt(sub) + seed) % 6;

		if(enumIndex == 5) {
			return null;
		}

		int reasonIndex = (int) ((Instant.now().toEpochMilli() * seed) % 4);

		AvailabilityStatus status = List.of(AVAILABLE, AVAILABLE, UNAVAILABLE, IF_DESPERATE, FAN_CLUB).get(enumIndex);

		List<String> goodReasons = List.of("", "", "", "First 45", "ankle better");
		List<String> badReasons = List.of("", "", "Injured", "On holiday");
		String comment = switch (status) {
			case AVAILABLE -> goodReasons.get(reasonIndex).isEmpty() ? null : goodReasons.get(reasonIndex);
			case UNAVAILABLE, IF_DESPERATE -> badReasons.get(reasonIndex).isEmpty() ? null : badReasons.get(reasonIndex);
			default -> null;
		};

		return entry(sub, Availability.builder()
			.status(status)
			.comment(comment)
			.build());
	}

	private static Match createTraining(int seed) {
		final ZonedDateTime baseZonedDateTime = ZonedDateTime.now()
			.withHour(20)
			.withMinute(0)
			.withSecond(0)
			.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
		ZonedDateTime kickOffTime = baseZonedDateTime.plusWeeks(seed);

		Map<String, String> address = Map.of(
				"line1", "The Armitage Center",
				"line2", "Moseley Rd",
				"postcode", "M14 6PA"
			);

		return Match.builder()
			.id("TRAINING" + seed)
			.address(address)
			.kickOffDateTime(kickOffTime)
			.opponent("Training")
			.pitchType(ASTRO)
			.playerAvailability(createPlayerAvailability(seed))
			.eventType("TRAINING")
			.build();
	}

}
