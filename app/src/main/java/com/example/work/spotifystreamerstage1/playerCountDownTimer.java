package com.example.work.spotifystreamerstage1;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.SeekBar;

/**
 * Created by work on 7/17/15.
 */
public class playerCountDownTimer extends CountDownTimer {
    private SeekBar sb;
    public long timeRemaining = 0;

    public playerCountDownTimer(long start, long interval, SeekBar sb)
    {
        super(start, interval);
        this.sb = sb;
        Log.d("playerCountDownTimer", "start " + Long.toString(start) + " interval " + Long.toString(interval));
    }

    @Override
    public void onFinish()
    {
    }

    @Override
    public void onTick(long millisRemaining)
    {
        timeRemaining = millisRemaining;
        // using units of 10 ms to make the bar work better
        int secsRemaining = (int) (millisRemaining);  // make sure the bar completes
        int max = sb.getMax();
        int progress = max - secsRemaining;
        if ( progress > max) progress = max;
        sb.setProgress(progress);
        if (secsRemaining != millisRemaining)
          Log.d("playerCountDownTimer:onTick", "can't handle integer conversion s" + Long.toString(millisRemaining));
    }

}
