package uk.co.withingtonhopecf.localstub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import uk.co.withingtonhopecf.localstub.converter.AvailabilityStatusConverter;
import uk.co.withingtonhopecf.localstub.model.enums.AvailabilityStatus;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
@DynamoDbImmutable(builder = Availability.AvailabilityBuilder.class)
public class Availability {

	@Getter(onMethod_ = @DynamoDbConvertedBy(AvailabilityStatusConverter.class))
	AvailabilityStatus status;

	String comment;

}
