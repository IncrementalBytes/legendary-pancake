package net.frostedbytes.android.picklythebest.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.utils.StringUtils;

public class UserSummary implements Serializable {

  public final static String ROOT = "UserSummaries";

  public long LevelsPlayed;
  public long LevelsSolved;
  public long TotalGuesses;
  public long TotalMilliseconds;
  public String UserId;

  public UserSummary() {

    this.LevelsPlayed = 0;
    this.LevelsSolved = 0;
    this.TotalGuesses = 0;
    this.TotalMilliseconds = 0;
    this.UserId = BaseActivity.DEFAULT_ID;
  }

  @Override
  public String toString() {

    return String.format(
      Locale.ENGLISH,
      "Solved %d out of %d level(s); guessing %d time(s) in %s",
      this.LevelsSolved,
      this.LevelsPlayed,
      this.TotalGuesses,
      StringUtils.toTimeString(this.TotalMilliseconds));
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
    result.put("TotalMilliseconds", this.TotalMilliseconds);
    return result;
  }
}
