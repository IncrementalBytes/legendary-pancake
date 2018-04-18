package net.frostedbytes.android.picklythebest.utils;

import java.util.Locale;

public class StringUtils {

  public static String toTimeString(long ticks) {

    long milliseconds = ticks % 1000;
    long temp = ticks / 1000;
    long minutes = temp / 60;
    long remainingSeconds = temp % 60;

    return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%03d", 0, minutes, remainingSeconds, milliseconds);
  }
}
