package uk.co.withingtonhopecf.localstub.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum PitchType {

	ASTRO("Astro Turf"), GRASS("Grass");

	@Getter(onMethod_ = @JsonValue)
	private final String name;

	PitchType(String name) {
		this.name = name;
	}
}
