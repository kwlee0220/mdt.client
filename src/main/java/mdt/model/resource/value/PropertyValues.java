package mdt.model.resource.value;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import lombok.experimental.UtilityClass;
import mdt.model.DataTypes;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PropertyValues {
	public static class StringValue extends PropertyValue<String> {
		public StringValue(String value) {
			super(DataTypes.STRING, value);
		}
	}
	public static class IntegerValue extends PropertyValue<Integer> {
		public IntegerValue(int value) {
			super(DataTypes.INTEGER, value);
		}
	}
	public static class FloatValue extends PropertyValue<Float> {
		public FloatValue(float value) {
			super(DataTypes.FLOAT, value);
		}
	}
	public static class DoubleValue extends PropertyValue<Double> {
		public DoubleValue(double value) {
			super(DataTypes.DOUBLE, value);
		}
	}
	public static class DateTimeValue extends PropertyValue<Instant> {
		public DateTimeValue(Instant value) {
			super(DataTypes.DATE_TIME, value);
		}
	}
	public static class DateValue extends PropertyValue<Date> {
		public DateValue(Date value) {
			super(DataTypes.DATE, value);
		}
	}
	public static class DurationValue extends PropertyValue<Duration> {
		public DurationValue(Duration value) {
			super(DataTypes.DURATION, value);
		}
	}
	public static class BooleanValue extends PropertyValue<Boolean> {
		public BooleanValue(boolean value) {
			super(DataTypes.BOOLEAN, value);
		}
	}
	public static class LongValue extends PropertyValue<Long> {
		public LongValue(long value) {
			super(DataTypes.LONG, value);
		}
	}
	public static class ShortValue extends PropertyValue<Short> {
		public ShortValue(short value) {
			super(DataTypes.SHORT, value);
		}
	}
}
