package net.dian1.player.util;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import net.dian1.player.activity.PlayerActivity;
import net.dian1.player.media.PlayerEngine;

public class OnSeekToListenerImp implements OnTouchListener {

	private static final int HOLD_BUTTON_THRESHOLD = 500;
	private static final int INIT_SEEK_TO_STEP = 500;
	private static final int MILIS_IN_FUTURE = 50000;
	private static final int COUNT_DOWN_INTERVAL = 200;

	PlayerEngine mPlayerEngine;
	CountDownTimer mSeekTimer;
	SeekToMode mSeekToMode;
	double mSeekAccelaration;
	int stepOfSeekTo;

	long startTime = 0;
	long endTime = 0;

	public OnSeekToListenerImp(PlayerEngine playerEngine, SeekToMode seekToMode) {

		mPlayerEngine = playerEngine;
		mSeekToMode = seekToMode;
		

		mSeekTimer = new CountDownTimer(MILIS_IN_FUTURE, COUNT_DOWN_INTERVAL) {
			@Override
			public void onTick(long millisUntilFinished) {
				long time = MILIS_IN_FUTURE - millisUntilFinished;				
				if (time > HOLD_BUTTON_THRESHOLD) {					
					switch (mSeekToMode) {
					case ERewind: {
						mPlayerEngine.rewind(stepOfSeekTo);
						break;
					}

					case EForward: {
						mPlayerEngine.forward(stepOfSeekTo);
						break;
					}
					default:
						Log.e("Timer", "This shouldn't happen");
					}
					
					if( stepOfSeekTo < 5000 )
					{
						stepOfSeekTo += 100;
					}
				}
				

			}
			


			@Override
			public void onFinish() {
			}
		};

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startTime = System.currentTimeMillis();
			mSeekTimer.start();
			mPlayerEngine.pause();
			stepOfSeekTo = INIT_SEEK_TO_STEP;

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mSeekTimer.cancel();
			mPlayerEngine.pause();
			endTime = System.currentTimeMillis();
			if (endTime - startTime < HOLD_BUTTON_THRESHOLD) {
				switch (mSeekToMode) {
				case ERewind: {
					mPlayerEngine.prev();
					break;
				}
				case EForward: {
					mPlayerEngine.next();
					break;
				}
				default:
					Log.e("Timer", "This shouldn't happen");

				}

			} else {
				mPlayerEngine.play();
			}

		}
		return true;
	}

}
