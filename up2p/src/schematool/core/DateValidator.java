package schematool.core;

import java.util.Calendar;

/**
 * Helper class for validating date strings.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DateValidator {

	/**
	 * Returns a calendar set to the date of the given
	 * parameters.
	 * 
	 * @param year year in the calendar
	 * @param month month of the year (1=January, 2=February, ...)
	 * @param day day of the month
	 * @return a calendar for the date or <code>null</code> if the
	 * date cannot be parsed or is invalid
	 */
	public static Calendar validate(String year, String month, String day) {
		if (year == null || month == null || day == null)
			return null;

		// validate each part
		int yearInt, monthInt, dayInt;

		try {
			yearInt = Integer.parseInt(year);
		} catch (NumberFormatException e) {
			return null;
		}
		// preliminary validation
		if (yearInt == 0)
			return null;

		try {
			monthInt = Integer.parseInt(month);
		} catch (NumberFormatException e) {
			return null;
		}
		// preliminary validation
		if (monthInt < 1 || monthInt > 12)
			return null;

		try {
			dayInt = Integer.parseInt(day);
		} catch (NumberFormatException e) {
			return null;
		}
		// preliminary validation
		if (dayInt < 1 || dayInt > 31)
			return null;

		// create the date to validate it
		Calendar c = Calendar.getInstance();
		c.set(yearInt, monthInt - 1, dayInt);
		// validate day of month
		if (dayInt > c.getActualMaximum(Calendar.DAY_OF_MONTH)
			|| dayInt < c.getActualMinimum(Calendar.DAY_OF_MONTH))
			return null;
		return c;
	}

	/**
	 * Returns true if the date represented by the first object
	 * is after the date indicated in the second object.
	 * 
	 * @param first first date to compare
	 * @param second second date to compare
	 * @return true if the first is after the second
	 */
	public static boolean isAfter(Calendar first, Calendar second) {
		return first.after(second);
	}
}
