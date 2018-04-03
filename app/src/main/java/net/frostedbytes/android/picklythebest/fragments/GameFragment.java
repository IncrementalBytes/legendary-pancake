package net.frostedbytes.android.picklythebest.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.frostedbytes.android.picklythebest.BaseActivity;
import net.frostedbytes.android.picklythebest.R;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class GameFragment extends Fragment {

  private final static String TAG = GameFragment.class.getSimpleName();

  public interface OnGameListener {

    void onGameCreated();
    void onGameContinue(int level);
    void onGameQuit(int level);
  }

  private OnGameListener mCallback;

  private Chronometer mChronometer;

  private TextView mAttemptsTextView;
  private GridView mGridView;

  private int mAnswer;
  private List<Integer> mChoices;
  private int mGuesses;
  private int mLevel;
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
    mAttemptsTextView = view.findViewById(R.id.game_text_attempts);
    mChronometer = view.findViewById(R.id.game_chronometer);
    TextView levelTextView = view.findViewById(R.id.game_text_level);
    mGridView = view.findViewById(R.id.game_grid_choices);

    mAttemptsTextView.setText(
      String.format(
        Locale.ENGLISH,
        "%s %s",
        getString(R.string.attempts_header),
        mGuesses));
    levelTextView.setText(
      String.format(
        "LEVEL %s",
        mLevel > 0 ? String.valueOf(mLevel) : "N/A"));

    mTask = new GenerateGridTask().execute();

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

    mGridView.setAdapter(new ImageAdapter(mChoices));
    mGridView.setOnItemClickListener((parent, v, position, id) -> {

      LogUtils.debug(TAG, "++onItemClick(AdapterView, View, %d, %d", position, id); // TODO: remove
      TextView guessedText = v.findViewById(R.id.guess_text);
      ImageView updatedImage = v.findViewById(R.id.guess_image);
      int converted;
      try {
        converted = NumberFormat.getNumberInstance(Locale.ENGLISH).parse(guessedText.getText().toString()).intValue();
        mGuesses++;
        mAttemptsTextView.setText(
          String.format(
            Locale.ENGLISH,
            "%s %d",
            getString(R.string.attempts_header),
            mGuesses));
        if (converted == mAnswer) {
          mChronometer.stop();
          updatedImage.setImageResource(R.drawable.ic_correct_dark);
          AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setMessage(R.string.correct_answer)
            .setPositiveButton(android.R.string.yes, (continueDialog, which) -> mCallback.onGameContinue(mLevel))
            .setNegativeButton(android.R.string.no, (continueDialog, which) -> mCallback.onGameQuit(mLevel))
            .create();
          dialog.show();
        } else {
          updatedImage.setImageResource(R.drawable.ic_incorrect_dark);
        }
      } catch (ParseException pe) {
        LogUtils.error(TAG, pe.getMessage());
      }
    });
  }

  private class GenerateGridTask extends AsyncTask<Void, Void, List<Integer>> {

    @Override
    protected List<Integer> doInBackground(Void... params) {

      Random valueGenerator = new Random(Calendar.getInstance().getTimeInMillis());
      mAnswer = valueGenerator.nextInt();
      LogUtils.debug(TAG, "Answer: %,d", mAnswer);
      mChoices = new ArrayList<>();
      int targetIndex = ThreadLocalRandom.current().nextInt(0, mLevel);
      LogUtils.debug(TAG, "Answer Indexed: %d", targetIndex);
      mGuesses = 0;
      try {
        for (int index = 0; index <= mLevel; index++) {
          if (index == targetIndex) {
            mChoices.add(mAnswer);
          } else {
            mChoices.add(valueGenerator.nextInt());
          }
        }
      } catch (Exception ex) {
        LogUtils.error(TAG, "Failed in task: %s", ex.getMessage());
      }

      return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<Integer> gridValues) {

      if (mCallback != null) {
        mCallback.onGameCreated();
        updateUI();
        mChronometer.start();
      } else {
        LogUtils.error(TAG, "Callback not successful; app in unexpected state.");
      }
    }
  }

  public class ImageAdapter extends BaseAdapter {

    private List<Integer> mChoices;

    ImageAdapter(List<Integer> choices) {

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
