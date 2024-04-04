package uk.co.withingtonhopecf.localstub.converter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime> {


	@Override
	public AttributeValue transformFrom(ZonedDateTime zonedDateTime) {
		return AttributeValue.fromS(String.valueOf(zonedDateTime.toInstant().getEpochSecond()));
	}

	@Override
	public ZonedDateTime transformTo(AttributeValue attributeValue) {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(attributeValue.s())), ZoneId.of("Europe/London"));
	}

	@Override
	public EnhancedType<ZonedDateTime> type() {
		return EnhancedType.of(ZonedDateTime.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}
