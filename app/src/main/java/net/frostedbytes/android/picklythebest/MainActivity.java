package net.frostedbytes.android.picklythebest;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import java.util.Locale;
import java.util.Objects;
import net.frostedbytes.android.picklythebest.fragments.GameFragment;
import net.frostedbytes.android.picklythebest.fragments.MainFragment;
import net.frostedbytes.android.picklythebest.utils.LogUtils;

public class MainActivity extends BaseActivity implements
  NavigationView.OnNavigationItemSelectedListener,
  MainFragment.OnMainListener,
  GameFragment.OnGameListener {

  private final static String TAG = MainActivity.class.getSimpleName();

  private NavigationView mNavigationView;
  private ProgressDialog mProgressDialog;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    mNavigationView = findViewById(R.id.nav_view);
    mNavigationView.setNavigationItemSelectedListener(this);

    replaceFragment(MainFragment.newInstance());
  }

  @Override
  public void onBackPressed() {

    LogUtils.debug(TAG, "++onBackPressed()");
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onGameContinue(int level) {

    LogUtils.debug(TAG, "++onGameContinue(%d)", level);
    showProgressDialog("I am thinking of a number...");
    int nextLevel = level + 1;
    Fragment gameFragment = GameFragment.newInstance(nextLevel);
    replaceFragment(gameFragment, String.format(Locale.ENGLISH, "%s-%d", gameFragment.getClass().getName(), nextLevel));
  }

  @Override
  public void onGameCreated() {

    LogUtils.debug(TAG, "++onGameCreated()");
    hideProgressDialog();
  }

  @Override
  public void onLeaderboard() {

    LogUtils.debug(TAG, "++onLeaderboard()");
    LogUtils.warn(TAG, "Not yet implemented");
  }

  @Override
  public void onNewGame() {

    LogUtils.debug(TAG, "++onNewGame()");
    mNavigationView.getMenu().findItem(R.id.nav_new_game).setEnabled(false);
    showProgressDialog("I am thinking of a number...");
    Fragment gameFragment = GameFragment.newInstance(1);
    replaceFragment(gameFragment, String.format(Locale.ENGLISH, "%s-%d", gameFragment.getClass().getName(), 1));
  }

  @Override
  public void onGameQuit(int level) {

    LogUtils.debug(TAG, "++onGameQuit(%d)", level);
    mNavigationView.getMenu().findItem(R.id.nav_new_game).setEnabled(true);
    replaceFragment(MainFragment.newInstance());
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {

    LogUtils.debug(TAG, "++onNavigationItemSelected(MenuItem)");
    switch (Objects.requireNonNull(item).getItemId()) {
      case R.id.nav_home:
        mNavigationView.getMenu().findItem(R.id.nav_new_game).setEnabled(true);
        replaceFragment(MainFragment.newInstance());
        break;
      case R.id.nav_new_game:
        mNavigationView.getMenu().findItem(R.id.nav_new_game).setEnabled(false);
        Fragment gameFragment = GameFragment.newInstance(1);
        replaceFragment(gameFragment, String.format(Locale.ENGLISH, "%s-%d", gameFragment.getClass().getName(), 1));
        break;
      case R.id.nav_leaderboard:
        break;
      case R.id.nav_stats:
        break;
      case R.id.nav_preferences:
        break;
      case R.id.nav_log_out:
        break;
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onStatistics() {

    LogUtils.debug(TAG, "++onStatistics()");
    LogUtils.warn(TAG, "Not yet implemented");
  }

  void hideProgressDialog() {

    LogUtils.debug(TAG, "++hideProgressDialog()");
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
  }

  private void replaceFragment(Fragment fragment) {

    LogUtils.debug(TAG, "++replaceFragment(Fragment)");
    replaceFragment(fragment, fragment.getClass().getName());
  }

  private void replaceFragment(Fragment fragment, String backStackName) {

    LogUtils.debug(TAG, "++replaceFragment(Fragment, %s)", backStackName);
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (!fragmentManager.popBackStackImmediate(backStackName, 0)){ //fragment not in back stack, create it.
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.addToBackStack(backStackName);
      fragmentTransaction.commit();
    }
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
