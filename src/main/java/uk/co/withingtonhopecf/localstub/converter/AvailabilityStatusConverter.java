package uk.co.withingtonhopecf.localstub.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus;

public class AvailabilityStatusConverter implements AttributeConverter<AvailabilityStatus> {

	@Override
	public AttributeValue transformFrom(AvailabilityStatus availabilityStatus) {
		return AttributeValue.fromS(availabilityStatus.toString());
	}

	@Override
	public AvailabilityStatus transformTo(AttributeValue attributeValue) {
		return AvailabilityStatus.valueOf(attributeValue.s());
	}

	@Override
	public EnhancedType<AvailabilityStatus> type() {
		return EnhancedType.of(AvailabilityStatus.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}
