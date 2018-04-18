package net.frostedbytes.android.picklythebest;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.google.firebase.crash.FirebaseCrash;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class BaseActivity extends AppCompatActivity {

  private static final String TAG = BaseActivity.class.getSimpleName();

  public static final String ARG_EMAIL = "user_email";
  public static final String ARG_LEVEL = "level";
  public static final String ARG_USER_ID = "user_id";
  public static final String ARG_USER_NAME = "user_name";
  public static final String ARG_USER_PHOTO = "user_photo";
  public static final String DEFAULT_ID = "000000000-0000-0000-0000-000000000000";

  private ProgressDialog mProgressDialog;

  @Override
  public void onCreate(Bundle saved) {
    super.onCreate(saved);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
    if (BuildConfig.DEBUG) {
      FirebaseCrash.setCrashCollectionEnabled(false);
    } else {
      FirebaseCrash.setCrashCollectionEnabled(true);
    }
  }

  void hideProgressDialog() {

    LogUtils.debug(TAG, "++hideProgressDialog()");
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
  }

  void replaceFragment(Fragment fragment) {

    LogUtils.debug(TAG, "++replaceFragment(Fragment)");
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.main_fragment_container, fragment);
    fragmentTransaction.commit();
  }

  void showProgressDialog(String message) {

    LogUtils.debug(TAG, "++showProgressDialog()");
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setCancelable(false);
      mProgressDialog.setMessage(message);
    }

    mProgressDialog.show();
  }
}
