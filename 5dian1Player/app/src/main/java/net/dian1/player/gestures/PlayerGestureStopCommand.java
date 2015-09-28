package net.dian1.player.gestures;

import android.util.Log;

import net.dian1.player.Dian1Application;
import net.dian1.player.media.PlayerEngine;

public class PlayerGestureStopCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGestureStopCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}
	@Override
	public void execute() {
		Log.v(Dian1Application.TAG, "PlayerGestureStopCommand");
		mPlayerEngine.stop();
	}

}
