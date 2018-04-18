package net.frostedbytes.android.picklythebest.models;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.utils.StringUtils;

public class LevelResult {

  public final static String ROOT = "LevelResults";

  public int Guesses;
  public int Level;
  public boolean IsSuccessful;
  public long Milliseconds;
  public String UserId;

  public LevelResult() {

    this.Guesses = 0;
    this.Level = 0;
    this.IsSuccessful = false;
    this.Milliseconds = 0;
    this.UserId = BaseActivity.DEFAULT_ID;
  }

  @Override
  public String toString() {

    return String.format(
      Locale.ENGLISH,
      "Level %d - Guesses: %d, Time: %s",
      this.Level,
      this.Guesses,
      StringUtils.toTimeString(this.Milliseconds));
  }

  /**
   * Creates a mapped object based on values of this level result object
   * @return A mapped object of level result
   */
  public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("Guesses", this.Guesses);
    result.put("Level", this.Level);
    result.put("Time", this.Milliseconds);
    return result;
  }
}
