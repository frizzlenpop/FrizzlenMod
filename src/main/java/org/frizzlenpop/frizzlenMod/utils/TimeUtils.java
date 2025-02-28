package org.frizzlenpop.frizzlenMod.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*(?:s[a-z]*)?)?",
            Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Converts a time string like "1d12h30m" to milliseconds
     * 
     * @param timeString The string representing time (e.g., "1y2mo3w4d5h6m7s")
     * @return The time in milliseconds, or -1 if invalid format
     */
    public static long parseTimeString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return -1;
        }
        
        Matcher matcher = TIME_PATTERN.matcher(timeString);
        if (!matcher.matches()) {
            return -1;
        }
        
        long totalMillis = 0;
        
        String years = matcher.group(1);
        String months = matcher.group(2);
        String weeks = matcher.group(3);
        String days = matcher.group(4);
        String hours = matcher.group(5);
        String minutes = matcher.group(6);
        String seconds = matcher.group(7);
        
        if (years != null && !years.isEmpty()) {
            totalMillis += TimeUnit.DAYS.toMillis(Integer.parseInt(years) * 365);
        }
        
        if (months != null && !months.isEmpty()) {
            totalMillis += TimeUnit.DAYS.toMillis(Integer.parseInt(months) * 30);
        }
        
        if (weeks != null && !weeks.isEmpty()) {
            totalMillis += TimeUnit.DAYS.toMillis(Integer.parseInt(weeks) * 7);
        }
        
        if (days != null && !days.isEmpty()) {
            totalMillis += TimeUnit.DAYS.toMillis(Integer.parseInt(days));
        }
        
        if (hours != null && !hours.isEmpty()) {
            totalMillis += TimeUnit.HOURS.toMillis(Integer.parseInt(hours));
        }
        
        if (minutes != null && !minutes.isEmpty()) {
            totalMillis += TimeUnit.MINUTES.toMillis(Integer.parseInt(minutes));
        }
        
        if (seconds != null && !seconds.isEmpty()) {
            totalMillis += TimeUnit.SECONDS.toMillis(Integer.parseInt(seconds));
        }
        
        return totalMillis;
    }
    
    /**
     * Alias for parseTimeString for backward compatibility
     */
    public static long parseDuration(String timeString) {
        return parseTimeString(timeString);
    }
    
    /**
     * Formats milliseconds into a human-readable time string
     * 
     * @param millis The time in milliseconds
     * @return A formatted string like "1d 2h 3m 4s"
     */
    public static String formatTime(long millis) {
        if (millis <= 0) {
            return "0 seconds";
        }
        
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(days == 1 ? " day" : " days");
        }
        
        if (hours > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        
        if (minutes > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }
        
        if (seconds > 0 || result.length() == 0) {
            if (result.length() > 0) result.append(" ");
            result.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }
        
        return result.toString();
    }
    
    /**
     * Formats a timestamp to a date string
     * 
     * @param timestamp The timestamp in milliseconds
     * @return A formatted date string
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Gets the remaining time as a string
     * 
     * @param expiryTimeMillis The expiry time in milliseconds
     * @return The formatted remaining time or "Expired" if expired
     */
    public static String getRemainingTime(long expiryTimeMillis) {
        long now = System.currentTimeMillis();
        
        if (expiryTimeMillis <= now) {
            return "Expired";
        }
        
        return formatTime(expiryTimeMillis - now);
    }
} 