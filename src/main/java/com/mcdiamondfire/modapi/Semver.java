package com.mcdiamondfire.modapi;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Semantic Versioning representation.
 *
 * @param major the major version number
 * @param minor the minor version number
 * @param patch the patch version number
 */
@NullMarked
public record Semver(int major, int minor, int patch) {
	
	private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
	
	/**
	 * Constructs a new semantic version object.
	 *
	 * @param major the major version number
	 * @param minor the minor version number
	 * @param patch the patch version number
	 * @throws IllegalArgumentException if any version number is negative
	 */
	public Semver {
		if (major < 0 || minor < 0 || patch < 0) {
			throw new IllegalArgumentException("Version numbers cannot be negative");
		}
	}
	
	/**
	 * Parses a semantic version string (e.g., "1.0.0") into a Semver object.
	 *
	 * @param version the semantic version string to parse
	 * @return the parsed Semver object
	 * @throws IllegalArgumentException if the version string is invalid
	 */
	public static Semver parse(String version) {
		Objects.requireNonNull(version, "Version cannot be null");
		
		if (version.isEmpty()) {
			throw new IllegalArgumentException("Version cannot be empty");
		}
		
		if (!SEMVER_PATTERN.matcher(version).matches()) {
			throw new IllegalArgumentException("Invalid semantic version: " + version);
		}
		
		String[] parts = version.split("\\.");
		
		return new Semver(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2])
		);
	}
	
	/**
	 * Tries to parse a semantic version string into a Semver object.
	 *
	 * @param version the semantic version string to parse
	 * @return an Optional containing the parsed Semver object, or empty if parsing failed
	 */
	public static Optional<Semver> tryParse(String version) {
		Objects.requireNonNull(version, "Version cannot be null");
		
		try {
			return Optional.of(parse(version));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * Checks if this Semver is incompatible with another Semver based on the major version.
	 *
	 * @param other the other Semver to compare with
	 * @return true if the major versions are incompatible, false otherwise
	 */
	public boolean isIncompatibleWith(Semver other) {
		Objects.requireNonNull(other, "Other Semver cannot be null");
		
		return this.major != other.major;
	}
	
	/**
	 * Returns the string representation of the Semver in "major.minor.patch" format.
	 *
	 * @return the string representation of the Semver
	 */
	@Override
	public String toString() {
		return "%d.%d.%d".formatted(this.major, this.minor, this.patch);
	}
	
}
