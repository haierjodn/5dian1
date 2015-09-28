package net.dian1.player.gestures;

import android.util.Log;

import net.dian1.player.Dian1Application;
import net.dian1.player.media.PlayerEngine;

public class PlayerGestureNextCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGestureNextCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}
	
	@Override
	public void execute() {
		Log.v(Dian1Application.TAG, "PlayerGestureNextCommand");
		mPlayerEngine.next();
	}
	
}

