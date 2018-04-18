package net.frostedbytes.android.picklythebest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.frostedbytes.android.picklythebest.fragments.GameFragment;
import net.frostedbytes.android.picklythebest.fragments.MainFragment;
import net.frostedbytes.android.picklythebest.fragments.StatisticsFragment;
import net.frostedbytes.android.picklythebest.fragments.UserPreferencesFragment;
import net.frostedbytes.android.picklythebest.models.LevelResult;
import net.frostedbytes.android.picklythebest.models.LevelSummary;
import net.frostedbytes.android.picklythebest.models.UserSummary;
import net.frostedbytes.android.picklythebest.utils.LogUtils;
import net.frostedbytes.android.picklythebest.utils.PathUtils;

public class MainActivity extends BaseActivity implements
  NavigationView.OnNavigationItemSelectedListener,
  MainFragment.OnMainListener,
  GameFragment.OnGameListener {

  private final static String TAG = MainActivity.class.getSimpleName();

  private DrawerLayout mDrawer;
  private Toolbar mToolbar;

  private int mLevel;
  private List<LevelSummary> mLevelSummaryList;
  private String mUserId;
  private UserSummary mUserSummary;

  private ValueEventListener mLevelListener;
  private Query mLevelQuery;
  private ValueEventListener mSummaryListener;
  private Query mSummaryQuery;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    mToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mToolbar);

    mDrawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView mNavigationView = findViewById(R.id.nav_view);
    View headerView =  mNavigationView.getHeaderView(0);
    TextView userNameText = headerView.findViewById(R.id.nav_header_name);
    TextView emailText = headerView.findViewById(R.id.nav_header_email);

    mNavigationView.setNavigationItemSelectedListener(this);

    // get parameters from previous activity
    mUserId = getIntent().getStringExtra(BaseActivity.ARG_USER_ID);
    String userName = getIntent().getStringExtra(BaseActivity.ARG_USER_NAME);
    userNameText.setText(userName);
    String email = getIntent().getStringExtra(BaseActivity.ARG_EMAIL);
    emailText.setText(email);

    // look for user data in database
    mLevelQuery = FirebaseDatabase.getInstance().getReference().child(LevelSummary.ROOT).child(mUserId);
    mLevelListener = new ValueEventListener() {

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {

        LogUtils.debug(TAG, "onDataChange() for LevelSummary query.");
        mLevelSummaryList = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          LevelSummary levelSummary = snapshot.getValue(LevelSummary.class);
          if (levelSummary != null) {
            String levelString = snapshot.getKey().substring(3, snapshot.getKey().length());
            levelSummary.Level = Integer.parseInt(levelString);
            mLevelSummaryList.add(levelSummary);
          }
        }

        getUserSummaryData();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

        LogUtils.debug(TAG, "onCancelled() for LevelSummary query.");
        LogUtils.error(TAG, databaseError.getMessage());
      }
    };
    mLevelQuery.addValueEventListener(mLevelListener);

    getUserSummaryData();
    replaceFragment(MainFragment.newInstance(mUserId));
  }

  @Override
  public void onBackPressed() {

    LogUtils.debug(TAG, "++onBackPressed()");
    if (mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START)) {
      mDrawer.closeDrawer(GravityCompat.START);
    } else {
      replaceFragment(MainFragment.newInstance(mUserId));
    }
  }

  @Override
  public void onContinueGame(int level) {

    LogUtils.debug(TAG, "++onContinueGame(%d)", level);
    mLevel = level;
    mToolbar.setTitle(String.format(Locale.ENGLISH, "Level %d", mLevel));
    Fragment gameFragment = GameFragment.newInstance(mLevel);
    replaceFragment(gameFragment);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mLevelQuery != null) {
      mLevelQuery.removeEventListener(mLevelListener);
    }

    if (mSummaryQuery != null) {
      mSummaryQuery.removeEventListener(mSummaryListener);
    }
  }

  @Override
  public void onGameCreated() {

    LogUtils.debug(TAG, "++onGameCreated()");
    hideProgressDialog();
    mToolbar.setTitle(String.format(Locale.ENGLISH, "Level %d", mLevel));
  }

  @Override
  public void onGameContinue(LevelResult levelResult) {

    LogUtils.debug(TAG, "++onGameContinue(LevelResult)");
    showProgressDialog(getString(R.string.thinking));
    pushLevelResult(levelResult);

    // update last known game
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    sharedPreferences.edit().putString(UserPreferencesFragment.KEY_GET_LAST_LEVEL_SETTING, String.valueOf(levelResult.Level)).apply();

    // advance to the next level of the game
    mLevel = levelResult.Level + 1;
    mToolbar.setTitle(String.format(Locale.ENGLISH, "Level %d", mLevel));
    Fragment gameFragment = GameFragment.newInstance(mLevel);
    replaceFragment(gameFragment);
  }

  @Override
  public void onGameQuit(LevelResult levelResult) {

    LogUtils.debug(TAG, "++onGameQuit(LevelResult)");
    pushLevelResult(levelResult);

    // update last known game
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    sharedPreferences.edit().putString(UserPreferencesFragment.KEY_GET_LAST_LEVEL_SETTING, String.valueOf(levelResult.Level)).apply();

    // return to the main screen
    mToolbar.setTitle(getString(R.string.app_name));
    replaceFragment(MainFragment.newInstance(mUserId));
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {

    LogUtils.debug(TAG, "++onNavigationItemSelected(MenuItem)");
    switch (Objects.requireNonNull(item).getItemId()) {
      case R.id.nav_home:
        mToolbar.setTitle(getString(R.string.app_name));
        replaceFragment(MainFragment.newInstance(mUserId));
        break;
      case R.id.nav_new_game:
        mLevel = 1;
        replaceFragment(GameFragment.newInstance(mLevel));
        break;
      case R.id.nav_leaderboard:
        break;
      case R.id.nav_stats:
        replaceFragment(StatisticsFragment.newInstance(mUserId));
        break;
      case R.id.nav_preferences:
        break;
      case R.id.nav_log_out:
        @SuppressLint("RestrictedApi")
        AlertDialog dialog = new AlertDialog.Builder(this)
          .setMessage(R.string.logout_message)
          .setPositiveButton(android.R.string.yes, (positiveDialog, which) -> {

            // sign out of firebase
            FirebaseAuth.getInstance().signOut();

            // sign out of google, if necessary
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(getString(R.string.default_web_client_id))
              .requestEmail()
              .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {

              // return to sign-in activity
              startActivity(new Intent(getApplicationContext(), SignInActivity.class));
              finish();
            });
          })
          .setNegativeButton(android.R.string.no, null)
          .create();
        dialog.show();
        break;
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onShowLeaderboard() {

    LogUtils.debug(TAG, "++onLeaderboard()");
    LogUtils.warn(TAG, "Not yet implemented");
  }

  @Override
  public void onShowStatistics() {

    LogUtils.debug(TAG, "++onShowStatistics()");
    replaceFragment(StatisticsFragment.newInstance(mUserId));
  }

  @Override
  public void onStartNewGame() {

    LogUtils.debug(TAG, "++onNewGame()");
//    mNavigationView.getMenu().findItem(R.id.nav_new_game).setEnabled(false);
    mLevel = 1;
    replaceFragment(GameFragment.newInstance(mLevel));
  }

  private void getUserSummaryData() {

    LogUtils.debug(TAG, "++getUserSummaryData()");
    mSummaryQuery = FirebaseDatabase.getInstance().getReference().child(UserSummary.ROOT).child(mUserId);
    mSummaryListener = new ValueEventListener() {

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {

        LogUtils.debug(TAG, "onDataChanged() for UserSummary query.");
        UserSummary summary = dataSnapshot.getValue(UserSummary.class);
        if (summary != null) {
          mUserSummary = summary;
        } else {
          LogUtils.warn(TAG, "There was no user summary to retrieve.");
          mUserSummary = new UserSummary();
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

        LogUtils.debug(TAG, "onCancelled() for UserSummary query.");
        LogUtils.error(TAG, databaseError.getMessage());
      }
    };
    mSummaryQuery.addValueEventListener(mSummaryListener);
  }

  private void pushLevelResult(LevelResult levelResult) {

    LogUtils.debug(TAG, "++pushLevelResult(LevelResult)");
    // TODO: FEATURE: upload the level result and cloud functions will update the level summary and user summary
//    queryPath = PathUtils.combine(LevelResult.ROOT, mUserId, Calendar.getInstance().getTimeInMillis());
//    FirebaseDatabase.getInstance().getReference().child(queryPath).setValue(levelResult.toMap(), new CompletionListener() {
//
//      @Override
//      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//        if (databaseError != null) {
//          LogUtils.debug(TAG, "Data could not be saved: %s", databaseError.getMessage());
//        } else {
//          LogUtils.debug(TAG, "Data saved successfully.");
//        }
//      }
//    });
    //  TODO: FEATURE: remove these individual stat updates
    String levelId = String.format(Locale.ENGLISH, "ID_%06d", levelResult.Level);
    String queryPath = PathUtils.combine(LevelSummary.ROOT, mUserId, levelId);
    boolean found = false;
    if (mLevelSummaryList != null) { // locate existing level summary
      for (LevelSummary levelSummary : mLevelSummaryList) {
        if (levelSummary.Level == levelResult.Level) { // update the existing level data
          LogUtils.debug(TAG, "Before: %s", levelSummary.toString());
          levelSummary.Played++;
          levelSummary.Solved = levelResult.IsSuccessful ? levelSummary.Solved + 1 : levelSummary.Solved;
          levelSummary.Milliseconds += levelResult.Milliseconds;
          levelSummary.TotalGuesses += levelResult.Guesses;
          LogUtils.debug(TAG, "After: %s", levelSummary.toString());
          FirebaseDatabase.getInstance().getReference().child(queryPath).setValue(levelSummary.toMap());
          found = true;
          break;
        }
      }
    } else {
      mLevelSummaryList = new ArrayList<>();
    }

    if (!found) {
      LevelSummary levelSummary = new LevelSummary();
      levelSummary.Level = levelResult.Level;
      levelSummary.Played = 1;
      levelSummary.Solved = levelResult.IsSuccessful ? 1 : 0;
      levelSummary.TotalGuesses = levelResult.Guesses;
      levelSummary.Milliseconds = levelResult.Milliseconds;
      mLevelSummaryList.add(levelSummary);
      FirebaseDatabase.getInstance().getReference().child(queryPath).setValue(levelSummary.toMap());
    }

    // update the user summary
    if (mUserSummary == null) {
      mUserSummary = new UserSummary();
    }

    mUserSummary.LevelsPlayed++;
    mUserSummary.LevelsSolved = levelResult.IsSuccessful ? mUserSummary.LevelsSolved + 1 : mUserSummary.LevelsSolved;
    mUserSummary.TotalMilliseconds += levelResult.Milliseconds;
    mUserSummary.TotalGuesses += levelResult.Guesses;

    queryPath = PathUtils.combine(UserSummary.ROOT, mUserId);
    FirebaseDatabase.getInstance().getReference().child(queryPath).setValue(mUserSummary.toMap());
  }
}
