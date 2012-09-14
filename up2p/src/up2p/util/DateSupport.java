package up2p.util;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Provides support for processing ISO date and time strings.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DateSupport {
    private final static byte[][] MONTHS = new byte[][] { "january".getBytes(),
            "february".getBytes(), "march".getBytes(), "april".getBytes(),
            "may".getBytes(), "june".getBytes(), "july".getBytes(),
            "august".getBytes(), "september".getBytes(), "october".getBytes(),
            "november".getBytes(), "december".getBytes() };

    /**
     * Returns the current date formatted to ISO8601.
     * 
     * @return current date string as yyyy-mm-dd
     */
    public static String getCurrentDate() {
        return getISO8601Date(Calendar.getInstance());
    }

    /**
     * Returns the current date and time formatted to ISO8601.
     * 
     * @return current date and time string as yyyy-mm-ddThh:mm:ss+ZZZZZ with
     * time zone offset (Z) in (+|-)hh:mm format or 'Z' if UTC (GMT) time zone
     */
    public static String getCurrentDateTime() {
        return getISO8601DateTime(Calendar.getInstance());
    }

    /**
     * Returns the current time formatted to ISO8601.
     * 
     * @return time string as hh:mm:ss+ZZZZZ with time zone offset (Z) in
     * (+|-)hh:mm format or 'Z' if UTC (GMT) time zone
     */
    public static String getCurrentTime() {
        return getISO8601Time(Calendar.getInstance());
    }

    /**
     * Returns an ISO8601 formatted date string.
     * 
     * @param calendar calendar set to a date
     * @return date string as yyyy-mm-dd
     */
    public static String getISO8601Date(Calendar calendar) {
        StringBuffer resultDate = new StringBuffer();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(4);

        // set era
        if (calendar.get(Calendar.ERA) == GregorianCalendar.BC)
            resultDate.append("-");
        // set year
        if (calendar.isSet(Calendar.YEAR))
            resultDate.append(nf.format(calendar.get(Calendar.YEAR)));

        // set to two digits
        nf.setMinimumIntegerDigits(2);

        // set month
        if (calendar.isSet(Calendar.MONTH))
            resultDate
                    .append("-" + nf.format(calendar.get(Calendar.MONTH) + 1));
        // set day
        if (calendar.isSet(Calendar.DAY_OF_MONTH))
            resultDate.append("-"
                    + nf.format(calendar.get(Calendar.DAY_OF_MONTH)));
        return resultDate.toString();
    }

    /**
     * Returns an ISO8601 formatted date and time string.
     * 
     * @param calendar calendar set to the date and time required
     * @return date and time string as yyyy-mm-ddThh:mm:ss+ZZZZZ with time zone
     * offset (Z) in (+|-)hh:mm format or 'Z' if UTC (GMT) time zone
     */
    public static String getISO8601DateTime(Calendar calendar) {
        return getISO8601Date(calendar) + "T" + getISO8601Time(calendar);
    }

    /**
     * Returns an ISO8601 formatted time string.
     * 
     * @param calendar calendar set to a time
     * @return time string as hh:mm:ss+ZZZZZ with time zone offset (Z) in
     * (+|-)hh:mm format or 'Z' if UTC (GMT) time zone
     */
    public static String getISO8601Time(Calendar calendar) {
        StringBuffer resultDate = new StringBuffer();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(2);

        // set hour
        if (calendar.isSet(Calendar.HOUR_OF_DAY)) {
            resultDate.append(nf.format(calendar.get(Calendar.HOUR_OF_DAY)));
            resultDate.append(":");
            // set minutes
            resultDate.append(nf.format(calendar.get(Calendar.MINUTE)));
            resultDate.append(":");
            // set seconds
            resultDate.append(nf.format(calendar.get(Calendar.SECOND)));
            // get time zone offset in milliseconds
            int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
            int hourOffset = zoneOffset / 3600000;
            int minuteOffset = (Math.abs(zoneOffset) % 3600000) / 60000;
            if (zoneOffset == 0)
                resultDate.append("Z");
            else {
                if (zoneOffset > 0)
                    resultDate.append("+");
                resultDate.append(nf.format(hourOffset) + ":"
                        + nf.format(minuteOffset));
            }
        }
        return resultDate.toString();
    }

    /**
     * Processes a date and returns a date string containing either YYYY-MM-DD,
     * YYYY-MM or YYYY.
     * 
     * @param day day of the month or <code>null</code> if not used
     * @param month month of the year or <code>null</code> if not used
     * @param year the integer year
     * @return processed ISO date string or partial ISO date string
     */
    public static String parseDate(String day, String month, String year) {
        StringBuffer result = new StringBuffer();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(4);

        // trim whitespace to be lenient
        if (day != null)
            day = day.trim();
        if (month != null)
            month = month.trim();
        if (year != null)
            year = year.trim();

        // year is required
        if (year != null && year.length() > 0) {
            try {
                result.append(nf.format(Integer.parseInt(year)));
            } catch (NumberFormatException e) {
                // could not parse year so date in invalid
                return "";
            }
            if (month != null && month.length() > 0) {
                byte[] monthBytes = month.toLowerCase().getBytes();
                int monthInt = 0;
                nf.setMinimumIntegerDigits(2);
                switch (month.length()) {
                case 1:
                case 2:
                    // one or two digit month
                    try {
                        monthInt = Integer.parseInt(month);
                    } catch (NumberFormatException e) {
                        // bad month
                        monthInt = 0;
                    }
                    break;
                case 3:
                    // three letter month
                    for (int i = 0; i < MONTHS.length; i++) {
                        if (monthBytes[0] == MONTHS[i][0]
                                && monthBytes[1] == MONTHS[i][1]
                                && monthBytes[2] == MONTHS[i][2]) {
                            // found the month
                            monthInt = i + 1;
                            break;
                        }
                    }
                default:
                    // try full month name
                    for (int i = 0; i < MONTHS.length; i++) {
                        if (Arrays.equals(monthBytes, MONTHS[i])) {
                            // found the month
                            monthInt = i + 1;
                            break;
                        }
                    }
                }
                // add the month to the date string if available
                if (monthInt > 0) {
                    result.append("-");
                    result.append(nf.format(monthInt));

                    // process the day only if month is present
                    if (day != null
                            && ((day.length() == 1 || day.length() == 2))) {
                        // one or two digit day of the month
                        int dayInt = 0;
                        try {
                            dayInt = Integer.parseInt(day);
                        } catch (NumberFormatException e) {
                            // bad day
                            dayInt = 0;
                        }
                        if (dayInt > 0) {
                            result.append("-");
                            result.append(nf.format(dayInt));
                        }
                    }
                }
            }
        }
        return result.toString();
    }
}