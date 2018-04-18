package net.frostedbytes.android.picklythebest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class LevelChronometer extends Chronometer {

  private boolean isRunning = false;

  public LevelChronometer(Context context) {
    super(context);
  }

  public LevelChronometer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LevelChronometer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void start() {
    super.start();

    isRunning = true;
  }

  @Override
  public void stop() {
    super.stop();

    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }
}
