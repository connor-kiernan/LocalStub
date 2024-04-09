package uk.co.withingtonhopecf.localstub.service;

import static uk.co.withingtonhopecf.localstub.model.enums.PitchType.ASTRO;
import static uk.co.withingtonhopecf.localstub.model.enums.PitchType.GRASS;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

	private static final List<String> USERNAMES = List.of(
		"sean.dyche",
		"pearson.david",
		"bertha.maxwell",
		"marci.barnett",
		"whitney.hunt",
		"margie.chambers",
		"rico.johnson",
		"mcbride.kirkland",
		"sanchez.sims",
		"neva.wolf",
		"petersen.burt",
		"heath.wagner",
		"benita.justice",
		"rachael.hammond",
		"elsie.case",
		"gwendolyn.dennis",
		"james.smith",
		"huff.jenkins",
		"connor.kiernan"
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
			.playerAvailability(createPlayerAvailability())
			.build();
	}

	private static Map<String, Availability> createPlayerAvailability() {
		return USERNAMES.stream()
		.collect(Collectors.toMap(Function.identity(), DynamoDbInitService::createAvailability));
	}

	private static Availability createAvailability(String username) {
		int enumIndex = username.length() % 4;
		int reasonIndex = (username.length() * 7) % 4;

		AvailabilityStatus status = AvailabilityStatus.values()[enumIndex];

		List<String> goodReasons = List.of("", "", "First 45", "ankle better");
		List<String> badReasons = List.of("", "", "On holiday", "Injured");
		String comment = switch (status) {
			case AVAILABLE -> goodReasons.get(reasonIndex).isEmpty() ? null : goodReasons.get(reasonIndex);
			case UNAVAILABLE, IF_DESPERATE -> badReasons.get(reasonIndex).isEmpty() ? null : badReasons.get(reasonIndex);
			default -> null;
		};

		return Availability.builder()
			.status(status)
			.comment(comment)
			.build();
	}

}
