package net.frostedbytes.android.picklythebest.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.frostedbytes.android.picklythebest.BaseActivity;

public class LevelResult {

  public final static String ROOT = "LevelResults";

  public int Guesses;
  public int Level;
  public boolean IsSuccessful;
  public long Time;
  public String UserId;

  public LevelResult() {

    this.Guesses = 0;
    this.Level = 0;
    this.IsSuccessful = false;
    this.Time = 0;
    this.UserId = BaseActivity.DEFAULT_ID;
  }

  @Override
  public String toString() {

    Date temp = new Date(this.Time);
    DateFormat dateFormat = new SimpleDateFormat("mm:ss.SSS", Locale.ENGLISH);
    return String.format(Locale.ENGLISH, "Level %d - Guesses: %d, Time: %s", this.Level, this.Guesses, dateFormat.format(temp));
  }

  /**
   * Creates a mapped object based on values of this level result object
   * @return A mapped object of level result
   */
  public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("Guesses", this.Guesses);
    result.put("Level", this.Level);
    result.put("Time", this.Time);
    return result;
  }
}
