package net.frostedbytes.android.picklythebest.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.Locale;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class MainFragment extends Fragment {

  private static final String TAG = MainFragment.class.getSimpleName();

  public interface OnMainListener {

    void onNewGame();
    void onStatistics();
    void onLeaderboard();
  }

  private OnMainListener mCallback;

  public static MainFragment newInstance() {

    LogUtils.debug(TAG, "++newInstance()");
    MainFragment fragment = new MainFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_main, container, false);

    Button newGameButton = view.findViewById(R.id.main_button_new);
    Button statisticsButton = view.findViewById(R.id.main_button_stats);
    Button leaderboardButton = view.findViewById(R.id.main_button_leaderboard);

    newGameButton.setOnClickListener(buttonView -> mCallback.onNewGame());
    statisticsButton.setOnClickListener(buttonView -> mCallback.onStatistics());
    leaderboardButton.setOnClickListener(buttonView -> mCallback.onLeaderboard());

    updateUI();

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnMainListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "%s must implement onNewGame().", context.toString()));
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    LogUtils.debug(TAG, "++onResume()");
    updateUI();
  }

  private void updateUI() {

    LogUtils.debug(TAG, "++updateUI()");
  }
}
