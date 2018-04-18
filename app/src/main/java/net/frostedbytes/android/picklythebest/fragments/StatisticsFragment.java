package net.frostedbytes.android.picklythebest.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.models.LevelSummary;
import net.frostedbytes.android.picklythebest.utils.LogUtils;
import net.frostedbytes.android.picklythebest.utils.StringUtils;

public class StatisticsFragment extends Fragment {

  private final static String TAG = StatisticsFragment.class.getSimpleName();

  private List<LevelSummary> mLevelSummaries;
  private String mUserId;

  private RecyclerView mHorizontalRecyclerView;

  private Query mLevelQuery;
  private ValueEventListener mLevelListener;

  public static StatisticsFragment newInstance(String userId) {

    LogUtils.debug(TAG, "++newInstance(%s)", userId);
    StatisticsFragment fragment = new StatisticsFragment();
    Bundle args = new Bundle();
    args.putString(BaseActivity.ARG_USER_ID, userId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_statistics, container, false);

    mHorizontalRecyclerView = view.findViewById(R.id.statistics_recycler_view);
    final LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    mHorizontalRecyclerView.setLayoutManager(manager);

    // look for user data in database
    mLevelQuery = FirebaseDatabase.getInstance().getReference().child(LevelSummary.ROOT).child(mUserId);
    mLevelListener = new ValueEventListener() {

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {

        LogUtils.debug(TAG, "onDataChange() for LevelSummary query.");
        mLevelSummaries = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          LevelSummary levelSummary = snapshot.getValue(LevelSummary.class);
          if (levelSummary != null) {
            levelSummary.Level = Integer.parseInt(snapshot.getKey().substring(3, snapshot.getKey().length()));
            mLevelSummaries.add(levelSummary);
          } else {
            LogUtils.debug(TAG, "LevelSummary was null.");
          }
        }

        updateUI();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

        LogUtils.debug(TAG, "onCancelled() for LevelSummary query.");
        LogUtils.error(TAG, databaseError.getMessage());
      }
    };
    mLevelQuery.addValueEventListener(mLevelListener);

    updateUI();
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null) {
      mUserId = arguments.getString(BaseActivity.ARG_USER_ID);
    } else {
      mUserId = BaseActivity.DEFAULT_ID;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mLevelQuery != null) {
      mLevelQuery.removeEventListener(mLevelListener);
    }
  }

  private void updateUI() {

    if (mLevelSummaries != null && mLevelSummaries.size() > 0) {
      LogUtils.debug(TAG, "++updateUI()");
      HorizontalRecyclerViewAdapter horizontalAdapter = new HorizontalRecyclerViewAdapter(mLevelSummaries);
      mHorizontalRecyclerView.setAdapter(horizontalAdapter);
    } else {
      LogUtils.warn(TAG, "Nothing to see.");
    }
  }

  private class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<LevelSummaryHolder> {

    private final List<LevelSummary> mLevelSummaries;

    HorizontalRecyclerViewAdapter(List<LevelSummary> levelSummaries) {

      mLevelSummaries = levelSummaries;
    }

    @NonNull
    @Override
    public LevelSummaryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
      return new LevelSummaryHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelSummaryHolder holder, int position) {

      LevelSummary matchSummary = mLevelSummaries.get(position);
      holder.bind(matchSummary);
    }

    @Override
    public int getItemCount() { return mLevelSummaries.size(); }
  }

  private class LevelSummaryHolder extends RecyclerView.ViewHolder {

    private final TextView mLevelTextView;
    private final TextView mPlayedTextView;
    private final TextView mSolvedTextView;
    private final TextView mGuessesTextView;
    private final TextView mAverageGuessesTextView;
    private final TextView mTimeTextView;
    private final TextView mAverageTimeTextView;

    private LevelSummary mLevelSummary;

    LevelSummaryHolder(LayoutInflater inflater, ViewGroup parent) {
      super(inflater.inflate(R.layout.level_statistics_item, parent, false));

      mLevelTextView = itemView.findViewById(R.id.statistics_text_level);
      mPlayedTextView = itemView.findViewById(R.id.statistics_text_played_value);
      mSolvedTextView = itemView.findViewById(R.id.statistics_text_solved_value);
      mGuessesTextView = itemView.findViewById(R.id.statistics_text_guesses_value);
      mAverageGuessesTextView = itemView.findViewById(R.id.statistics_text_average_guesses_value);
      mTimeTextView = itemView.findViewById(R.id.statistics_text_total_time_value);
      mAverageTimeTextView = itemView.findViewById(R.id.statistics_text_average_time_value);
    }

    void bind(LevelSummary levelSummary) {

      mLevelSummary = levelSummary;
      mLevelTextView.setText(String.format(Locale.ENGLISH, "%s %,d", getString(R.string.header_level), mLevelSummary.Level));
      mPlayedTextView.setText(String.format(Locale.ENGLISH, "%,d", mLevelSummary.Played));
      mSolvedTextView.setText(String.format(Locale.ENGLISH, "%,d", mLevelSummary.Solved));
      mGuessesTextView.setText(String.format(Locale.ENGLISH, "%,d", mLevelSummary.TotalGuesses));
      mAverageGuessesTextView.setText(
        String.format(
          Locale.ENGLISH,
          "%02f",
          (double) mLevelSummary.TotalGuesses / mLevelSummary.Played));
      mTimeTextView.setText(StringUtils.toTimeString(mLevelSummary.Milliseconds));
      mAverageTimeTextView.setText(
        String.format(
          Locale.ENGLISH,
          "%s",
          StringUtils.toTimeString(mLevelSummary.Milliseconds / mLevelSummary.Played)));
    }
  }
}
