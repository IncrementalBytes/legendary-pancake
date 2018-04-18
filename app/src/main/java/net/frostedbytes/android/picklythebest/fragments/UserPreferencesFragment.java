package net.frostedbytes.android.picklythebest.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class UserPreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = UserPreferencesFragment.class.getSimpleName();

  public static final String KEY_GET_LAST_LEVEL_SETTING = "preference_last_level";

  public interface OnPreferencesListener {

    void onPreferenceChanged();
  }

  private OnPreferencesListener mCallback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach()");
    try {
      mCallback = (OnPreferencesListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement onPreferenceChanged().");
    }
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    LogUtils.debug(TAG, "++onCreatePreferences(Bundle, String)");
    addPreferencesFromResource(R.xml.app_preferences);
  }

  @Override
  public void onPause() {
    super.onPause();

    LogUtils.debug(TAG, "++onPause()");
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();

    LogUtils.debug(TAG, "++onResume()");
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String keyName) {

    LogUtils.debug(TAG, "++onSharedPreferenceChanged(SharedPreferences, String)");
    getPreferenceScreen().getSharedPreferences().edit().apply();
    if (keyName.equals(KEY_GET_LAST_LEVEL_SETTING)) {
      mCallback.onPreferenceChanged();
    } else {
      LogUtils.error(TAG, "Unknown key: ", keyName);
    }
  }
}
