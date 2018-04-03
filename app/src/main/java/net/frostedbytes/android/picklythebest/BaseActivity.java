package net.frostedbytes.android.picklythebest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class BaseActivity extends AppCompatActivity {

  private static final String TAG = BaseActivity.class.getSimpleName();

  public static final String ARG_LEVEL = "level";

  @Override
  public void onCreate(Bundle saved) {
    super.onCreate(saved);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
//    if (BuildConfig.DEBUG) {
//      FirebaseCrash.setCrashCollectionEnabled(false);
//    } else {
//      FirebaseCrash.setCrashCollectionEnabled(true);
//    }
  }
}
