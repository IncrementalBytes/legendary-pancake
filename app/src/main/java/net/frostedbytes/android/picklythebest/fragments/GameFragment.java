package net.frostedbytes.android.picklythebest.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.Guideline;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.models.LevelResult;
import net.frostedbytes.android.picklythebest.utils.LogUtils;
import net.frostedbytes.android.picklythebest.utils.StringUtils;
import net.frostedbytes.android.picklythebest.views.LevelChronometer;

public class GameFragment extends Fragment {

  private final static String TAG = GameFragment.class.getSimpleName();

  public interface OnGameListener {

    void onGameCreated();

    void onGameContinue(LevelResult levelResult);

    void onGameQuit(LevelResult levelResult);
  }

  private OnGameListener mCallback;

  private LevelChronometer mChronometer;

  private TextView mAttemptsTextView;
  private TextView mGuessesTextView;
  private GridView mGridView;

  private int mAllowedAttempts;
  private int mAnswer;
  private List<Integer> mChoices;
  private int mGuesses;
  private int mLevel;
  private LevelResult mLevelResult;
  private boolean mLevelComplete;
  private AsyncTask mTask;

  public static GameFragment newInstance(int level) {

    LogUtils.debug(TAG, "++newInstance(%d)", level);
    GameFragment fragment = new GameFragment();
    Bundle args = new Bundle();
    args.putInt(BaseActivity.ARG_LEVEL, level);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_game, container, false);
    mGridView = view.findViewById(R.id.game_grid_choices);
    mAttemptsTextView = view.findViewById(R.id.game_text_attempts);
    mGuessesTextView = view.findViewById(R.id.game_text_guesses);
    Guideline guideline = view.findViewById(R.id.game_guideline);
    mChronometer = view.findViewById(R.id.game_chronometer);

    // we need to set the guideline to 2/3 the current view width
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int width = (int) Math.round(displayMetrics.widthPixels * .66);
    guideline.setGuidelineBegin(width);

    mAttemptsTextView.setText(
      String.format(
        Locale.ENGLISH,
        "%s -",
        getString(R.string.header_attempts)));
    mGuessesTextView.setText(
      String.format(
        Locale.ENGLISH,
        "%s %s",
        getString(R.string.header_guesses),
        mGuesses));

    mTask = new GenerateGridTask(this).execute(mLevel);

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null) {
      mLevel = arguments.getInt(BaseActivity.ARG_LEVEL);
    } else {
      mLevel = -1;
    }

    try {
      mCallback = (OnGameListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "%s must implement onGameContinue(int) and onGameQuit(String).", context.toString()));
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mTask != null) {
      LogUtils.debug(TAG, "Cancelling game generation task.");
      mTask.cancel(true);
    }
  }

  void updateUI() {

    LogUtils.debug(TAG, "++updateUI()");
    mAttemptsTextView.setText(
      String.format(
        Locale.ENGLISH,
        "%s %d",
        getString(R.string.header_attempts),
        mAllowedAttempts));
    mGridView.setAdapter(new GuessAdapter(mChoices));
    mGridView.setOnItemClickListener(this::onGridItemClick);
  }

  private void onGridItemClick(AdapterView<?> parent, View v, int position, long id) {

    LogUtils.debug(TAG, "++onItemClick(AdapterView, View, %d, %d", position, id); // TODO: remove
    TextView guessedText = v.findViewById(R.id.guess_text);
    ImageView updatedImage = v.findViewById(R.id.guess_image);

    int converted; // this value should never used as an answer
    try {
      converted = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(guessedText.getText().toString()).intValue();
    } catch (ParseException pe) {
      // TODO: handle this exception smoothly
      LogUtils.error(TAG, pe.getMessage());
      return;
    }

    // handle case where results dialog was dismissed by a non-actionable event
    if (!mLevelComplete) {
      mGuesses++;
      mGuessesTextView.setText(
        String.format(
          Locale.ENGLISH,
          "%s %d",
          getString(R.string.header_guesses),
          mGuesses));
    }

    if (mLevelResult == null) {
      mLevelResult = new LevelResult();
      mLevelResult.Level = mLevel;
    }

    mLevelResult.Guesses = mGuesses;

    // check the users answer to our known answer
    if (converted == mAnswer) { // user is correct!
      mLevelComplete = true;
      if (mChronometer.isRunning()) {
        mChronometer.stop();
        mLevelResult.Milliseconds = SystemClock.elapsedRealtime() - mChronometer.getBase();
      }

      updatedImage.setImageResource(R.drawable.ic_correct_dark);

      // create a level result object to send back to activity
      mLevelResult.IsSuccessful = true;
      String message = getResources().getQuantityString(
        R.plurals.game_summary,
        mGuesses,
        mGuesses,
        StringUtils.toTimeString(mLevelResult.Milliseconds));

      // build dialog summarizing level result
      Builder builder = new Builder(getContext())
        .setTitle(String.format("LEVEL %s - SUCCESS!", mLevel > 0 ? String.valueOf(mLevel) : "N/A"))
        .setMessage(message)
        .setPositiveButton(R.string.next_level, (continueDialog, which) -> {
          LogUtils.debug(TAG, "++setPositiveButton()");
          mCallback.onGameContinue(mLevelResult);
        })
        .setNegativeButton(R.string.quit, (continueDialog, which) -> {
          LogUtils.debug(TAG, "++setNegativeButton()");
          mCallback.onGameQuit(mLevelResult);
        })
        .setOnDismissListener(dialogInterface -> {
          LogUtils.debug(TAG, "++onDismiss(DialogInterface)");
          mLevelComplete = true;
          LogUtils.warn(TAG, "Dialog dismissed but we aren't leaving the level!");
        });

      AlertDialog dialog = builder.create();
      dialog.show();
    } else if (!mLevelComplete) {
      if (mGuesses >= mAllowedAttempts) { // game over
        updatedImage.setImageResource(R.drawable.ic_incorrect_dark);
        mLevelComplete = true;
        if (mChronometer.isRunning()) {
          mChronometer.stop();
          mLevelResult.Milliseconds = SystemClock.elapsedRealtime() - mChronometer.getBase();
        }

        // create a level result object to send back to activity
        mLevelResult.IsSuccessful = false;
        String message = String.format(Locale.ENGLISH, getString(R.string.ran_out_of_guesses), mAnswer);

        // build dialog summarizing level result
        Builder builder = new Builder(getContext())
          .setTitle(R.string.game_over)
          .setMessage(message)
          .setPositiveButton(R.string.back, (continueDialog, which) -> LogUtils.debug(TAG, "++setPositiveButton()"))
          .setOnDismissListener(dialogInterface -> {
            LogUtils.debug(TAG, "++onDismiss(DialogInterface)");
            mCallback.onGameQuit(mLevelResult);
          });

        AlertDialog dialog = builder.create();
        dialog.show();
      } else { // FEATURE: color code this item based on proximity to correct answer
        if (converted > mAnswer) {
          updatedImage.setImageResource(R.drawable.ic_greater_dark);
        } else {
          updatedImage.setImageResource(R.drawable.ic_less_dark);
        }
      }
    }
  }

  private static class GenerateGridTask extends AsyncTask<Object, Void, List<Integer>> {

    private WeakReference<GameFragment> mFragmentWeakReference;
    private int mAllowedAttempts;
    private int mAnswer;
    private List<Integer> mChoices;
    private int mGuesses;

    GenerateGridTask(GameFragment context) {
      mFragmentWeakReference = new WeakReference<>(context);
    }

    @Override
    protected List<Integer> doInBackground(Object... params) {

      LogUtils.debug(TAG, "++doInBackground(Object...)");
      int level = (int) params[0];
      mAnswer = ThreadLocalRandom.current().nextInt(1, (level + 1));
      LogUtils.debug(TAG, "Answer: %,d", mAnswer);
      mChoices = new ArrayList<>();
      mGuesses = 0;

      try {
        for (int index = 0; index <= level; index++) {
          mChoices.add(index + 1);
        }
      } catch (Exception ex) {
        LogUtils.error(TAG, "Failed in task: %s", ex.getMessage());
      }

      // user only gets guesses equal to 50% of the choices; might adjust in the future
      mAllowedAttempts = (int) Math.round(mChoices.size() * .5);
      LogUtils.debug(TAG, "Allowed Attempts: %d", mAllowedAttempts);

      return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<Integer> gridValues) {

      LogUtils.debug(TAG, "++onPostExecute(List<Integer>)");
      GameFragment fragment = mFragmentWeakReference.get();
      if (fragment == null || fragment.isDetached()) {
        return;
      }

      if (fragment.mCallback != null) {
        fragment.mAnswer = mAnswer;
        fragment.mAllowedAttempts = mAllowedAttempts;
        fragment.mChoices = mChoices;
        fragment.mGuesses = mGuesses;
        fragment.mCallback.onGameCreated();
        fragment.updateUI();
        fragment.mChronometer.setBase(SystemClock.elapsedRealtime());
        fragment.mChronometer.start();
      } else {
        LogUtils.error(TAG, "Callback not successful; app in unexpected state.");
      }
    }
  }

  public class GuessAdapter extends BaseAdapter {

    private List<Integer> mChoices;

    GuessAdapter(List<Integer> choices) {

      mChoices = choices != null ? choices : new ArrayList<>();
    }

    public int getCount() {

      return mChoices.size();
    }

    public Object getItem(int position) {

      return null;
    }

    public long getItemId(int position) {

      return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

      View view = convertView;
      final GuessViewHolder holder;
      if (convertView == null) {
        view = getLayoutInflater().inflate(R.layout.guess_item, parent, false);
        holder = new GuessViewHolder();
        holder.Text = view.findViewById(R.id.guess_text);
        holder.Image = view.findViewById(R.id.guess_image);
        view.setTag(holder);
      } else {
        holder = (GuessViewHolder)view.getTag();
      }

      holder.Text.setText(
        String.format(
          Locale.ENGLISH,
          "%,d",
          mChoices.get(position)));
      holder.Image.setImageResource(R.drawable.ic_guess_dark);

      return view;    }
  }

  private class GuessViewHolder {

    public ImageView Image;
    public TextView Text;
  }
}
