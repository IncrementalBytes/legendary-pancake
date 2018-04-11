package net.frostedbytes.android.picklythebest.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.models.UserSummary;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class MainFragment extends Fragment {

  private static final String TAG = MainFragment.class.getSimpleName();

  public interface OnMainListener {

    void onNewGame();
    void onLeaderboard();
  }

  private OnMainListener mCallback;

  private TextView mPlayedTextView;
  private TextView mSolvedTextView;
  private TextView mGuessesTextView;
  private TextView mTimeTextView;
  private TextView mAverageGuessesTextView;
  private TextView mAverageTimeTextView;

  private String mUserId;
  private UserSummary mUserSummary;

  private ValueEventListener mSummaryListener;
  private Query mSummaryQuery;

  public static MainFragment newInstance(String userId) {

    LogUtils.debug(TAG, "++newInstance()");
    MainFragment fragment = new MainFragment();
    Bundle args = new Bundle();
    args.putString(BaseActivity.ARG_USER_ID, userId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_main, container, false);

    Button newGameButton = view.findViewById(R.id.main_button_new);
    Button leaderboardButton = view.findViewById(R.id.main_button_leaderboard);
    mPlayedTextView = view.findViewById(R.id.summary_text_levels_value);
    mSolvedTextView = view.findViewById(R.id.summary_text_solved_value);
    mGuessesTextView = view.findViewById(R.id.summary_text_guesses_value);
    mTimeTextView = view.findViewById(R.id.summary_text_time_value);
    mAverageGuessesTextView = view.findViewById(R.id.summary_text_average_guesses_value);
    mAverageTimeTextView = view.findViewById(R.id.summary_text_average_time_value);

    newGameButton.setOnClickListener(buttonView -> mCallback.onNewGame());
    leaderboardButton.setOnClickListener(buttonView -> mCallback.onLeaderboard());

    // look for user data in database
    mSummaryQuery = FirebaseDatabase.getInstance().getReference().child(UserSummary.ROOT).child(mUserId);
    mSummaryListener = new ValueEventListener() {

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {

        LogUtils.debug(TAG, "++onDataChange()");
        UserSummary summary = dataSnapshot.getValue(UserSummary.class);
        if (summary != null) {
          mUserSummary = summary;
          updateUI();
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

        LogUtils.debug(TAG, "++onCancelled(DatabaseError)");
        LogUtils.error(TAG, databaseError.getMessage());
      }
    };
    mSummaryQuery.addValueEventListener(mSummaryListener);

    updateUI();

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    if (getArguments() != null) {
      mUserId = getArguments().getString(BaseActivity.ARG_USER_ID);
    }

    try {
      mCallback = (OnMainListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "%s must implement onNewGame().", context.toString()));
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mSummaryQuery != null) {
      mSummaryQuery.removeEventListener(mSummaryListener);
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
    if (mUserSummary != null) {
      mPlayedTextView.setText(String.format(Locale.ENGLISH, "%,d", mUserSummary.LevelsPlayed));
      mSolvedTextView.setText(String.format(Locale.ENGLISH, "%,d", mUserSummary.LevelsSolved));
      mGuessesTextView.setText(String.format(Locale.ENGLISH, "%,d", mUserSummary.TotalGuesses));
      Date temp = new Date(mUserSummary.TotalTime);
      DateFormat dateFormat = new SimpleDateFormat("mm:ss.SSS", Locale.ENGLISH);
      mTimeTextView.setText(String.format(Locale.ENGLISH, "%s", dateFormat.format(temp)));
      double average = mUserSummary.TotalGuesses / mUserSummary.LevelsPlayed;
      mAverageGuessesTextView.setText(String.format(Locale.ENGLISH, "%,f", average));
      average = mUserSummary.TotalTime / mUserSummary.LevelsPlayed;
      temp = new Date(Math.round(average));
      mAverageTimeTextView.setText(String.format(Locale.ENGLISH, "%s", dateFormat.format(temp)));
    } else {
      mPlayedTextView.setText("-");
      mSolvedTextView.setText("-");
      mGuessesTextView.setText("-");
      mTimeTextView.setText("-");
      mAverageGuessesTextView.setText("-");
      mAverageTimeTextView.setText("-");
    }
  }
}
