package com.radixdlt.client.application.translate.tokens;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.radix.utils.UInt256;
import org.radix.utils.UInt256s;

/**
 * Utility class for converting token units between UInt256 and BigDecimal
 */
public final class TokenUnitConversions {

	private TokenUnitConversions() {
		throw new IllegalStateException("Not initializable");
	}

	/**
	 * Number of subunits in a unit as a power of 10, currently {@value #SUB_UNITS_POW_10}.
	 * In other words, the total number of subunits per unit is 10<sup>{@code SUB_UNITS_POW_10}</sup>.
	 */
	private static final int SUB_UNITS_POW_10 = 18;

	/**
	 * Number of subunits per unit.
	 * @see #SUB_UNITS_POW_10
	 */
	private static final UInt256 SUB_UNITS = UInt256.TEN.pow(SUB_UNITS_POW_10);

	private static final BigDecimal SUB_UNITS_BIG_DECIMAL = UInt256s.toBigDecimal(SUB_UNITS);

	private static final BigDecimal MINIMUM_GRANULARITY_BIG_DECIMAL = BigDecimal.ONE.scaleByPowerOfTen(-1 * SUB_UNITS_POW_10);

	public static int getTokenScale() {
		return SUB_UNITS_POW_10;
	}

	public static BigDecimal getSubunits() {
		return SUB_UNITS_BIG_DECIMAL;
	}

	public static BigDecimal getMinimumGranularity() {
		return MINIMUM_GRANULARITY_BIG_DECIMAL;
	}

	/**
	 * Returns the specified number of subunits as a fractional number
	 * of units.  This method effectively calculates:
	 * <blockquote>
	 *    <var>subunits</var> &times; 10<sup>-SUB_UNITS_POW_10</sup>
	 * </blockquote>
	 *
	 * @param subunits The number of subunits to convert to fractional units
	 * @return The number of fractional units represented by {@code subunits}
	 * @see #SUB_UNITS_POW_10
	 */
	public static BigDecimal subunitsToUnits(UInt256 subunits) {
		return subunitsToUnits(UInt256s.toBigInteger(subunits));
	}

	/**
	 * Returns the specified number of subunits as a fractional number
	 * of units.  This method effectively calculates:
	 * <blockquote>
	 *    <var>subunits</var> &times; 10<sup>-SUB_UNITS_POW_10</sup>
	 * </blockquote>
	 *
	 * @param subunits The number of subunits to convert to fractional units
	 * @return The number of fractional units represented by {@code subunits}
	 * @see #SUB_UNITS_POW_10
	 */
	public static BigDecimal subunitsToUnits(BigInteger subunits) {
		return new BigDecimal(subunits, SUB_UNITS_POW_10);
	}

	/**
	 * Returns the specified number of subunits as a fractional number
	 * of units.  This method effectively calculates:
	 * <blockquote>
	 *    <var>subunits</var> &times; 10<sup>-SUB_UNITS_POW_10</sup>
	 * </blockquote>
	 *
	 * @param subunits The number of subunits to convert to fractional units
	 * @return The number of fractional units represented by {@code subunits}
	 * @see #SUB_UNITS_POW_10
	 */
	public static BigDecimal subunitsToUnits(long subunits) {
		return BigDecimal.valueOf(subunits, SUB_UNITS_POW_10);
	}

	/**
	 * Returns the specified number of units as a {@link UInt256} number of
	 * of subunits.  This method effectively calculates:
	 * <blockquote>
	 *    <var>units</var> &times; 10<sup>SUB_UNITS_POW_10</sup>
	 * </blockquote>
	 *
	 * @param units The number of units to convert to subunits
	 * @return The integer number of subunits represented by {@code units}
	 * @throws IllegalArgumentException if {@code units} is less than zero
	 * @see #SUB_UNITS_POW_10
	 */
	public static UInt256 unitsToSubunits(long units) {
		if (units < 0) {
			throw new IllegalArgumentException("units must be >= 0: " + units);
		}
		// 10^18 is approximately 60 bits, so a positive long (63 bits) cannot overflow here
		return UInt256.from(units).multiply(SUB_UNITS);
	}

	/**
	 * Returns the specified number of units as a {@link UInt256} number of
	 * of subunits.  This method effectively calculates:
	 * <blockquote>
	 *    <var>units</var> &times; 10<sup>SUB_UNITS_POW_10</sup>
	 * </blockquote>
	 *
	 * @param units The number of units to convert to subunits
	 * @return The integer number of subunits represented by {@code units}
	 * @throws IllegalArgumentException if {@code units} is less than zero
	 *         or greater than {@link UInt256#MAX_VALUE}
	 * @throws ArithmeticException if {@code units} &times;
	 *         10<sup>SUB_UNITS_POW_10</sup> has a nonzero fractional part.
	 * @see #SUB_UNITS_POW_10
	 */
	public static UInt256 unitsToSubunits(BigDecimal units) {
		return UInt256s.fromBigDecimal(units.multiply(SUB_UNITS_BIG_DECIMAL));
	}
}
