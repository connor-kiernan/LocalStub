package uk.co.withingtonhopecf.localstub.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import uk.co.withingtonhopecf.localstub.model.enums.PitchType;

public class PitchTypeConverter implements AttributeConverter<PitchType> {

	@Override
	public AttributeValue transformFrom(PitchType pitchType) {
		return AttributeValue.fromS(pitchType.toString());
	}

	@Override
	public PitchType transformTo(AttributeValue attributeValue) {
		return PitchType.valueOf(attributeValue.s());
	}

	@Override
	public EnhancedType<PitchType> type() {
		return EnhancedType.of(PitchType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}
