package net.frostedbytes.android.picklythebest.models;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.frostedbytes.android.picklythebest.BaseActivity;

public class LevelSummary {

  public final static String ROOT = "LevelSummaries";

  @Exclude
  public double AverageGuesses;

  @Exclude
  public double AverageTime;

  public int Level;

  public long Played;

  public long Solved;

  public long TotalGuesses;

  public long TotalTime;

  public String UserId;

  public LevelSummary() {

    this.AverageGuesses = 0.0;
    this.AverageTime = 0.0;
    this.Level = 0;
    this.Played = 0;
    this.Solved = 0;
    this.TotalGuesses = 0;
    this.TotalTime = 0;
    this.UserId = BaseActivity.DEFAULT_ID;
  }

  @Override
  public String toString() {

    return String.format(
      Locale.ENGLISH,
      "Level %d: Played: %d, Solved: %d, Guesses: %d, Time: %d",
      this.Level,
      this.Played,
      this.Solved,
      this.TotalGuesses,
      this.TotalTime);
  }

  /**
   * Creates a mapped object based on values of this level summary object
   * @return A mapped object of level summary
   */
  public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("Played", this.Played);
    result.put("Solved", this.Solved);
    result.put("TotalGuesses", this.TotalGuesses);
    result.put("TotalTime", this.TotalTime);
    return result;
  }
}
