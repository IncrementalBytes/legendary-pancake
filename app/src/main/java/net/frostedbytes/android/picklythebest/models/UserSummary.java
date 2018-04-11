package net.frostedbytes.android.picklythebest.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.frostedbytes.android.picklythebest.BaseActivity;

public class UserSummary implements Serializable {

  public final static String ROOT = "UserSummaries";

  public long LevelsPlayed;
  public long LevelsSolved;
  public long TotalGuesses;
  public long TotalTime;
  public String UserId;

  public UserSummary() {

    this.LevelsPlayed = 0;
    this.LevelsSolved = 0;
    this.TotalGuesses = 0;
    this.TotalTime = 0;
    this.UserId = BaseActivity.DEFAULT_ID;
  }

  @Override
  public String toString() {

    Date temp = new Date(this.TotalTime);
    DateFormat dateFormat = new SimpleDateFormat("mm:ss.SSS", Locale.ENGLISH);
    return String.format(
      Locale.ENGLISH,
      "Solved %d out of %d level(s); guessing %d time(s) in %s",
      this.LevelsSolved,
      this.LevelsPlayed,
      this.TotalGuesses,
      dateFormat.format(temp));
  }

  /**
   * Creates a mapped object based on values of this level result object
   * @return A mapped object of level result
   */
  public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("LevelsPlayed", this.LevelsPlayed);
    result.put("LevelsSolved", this.LevelsSolved);
    result.put("TotalGuesses", this.TotalGuesses);
    result.put("TotalTime", this.TotalTime);
    return result;
  }
}
