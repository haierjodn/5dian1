package net.dian1.player.gestures;

import android.util.Log;

import net.dian1.player.Dian1Application;
import net.dian1.player.media.PlayerEngine;

public class PlayerGesturePrevCommand implements GestureCommand {

	PlayerEngine mPlayerEngine;
	
	public PlayerGesturePrevCommand( PlayerEngine engine ){
		mPlayerEngine = engine;
	}

	@Override
	public void execute() {
		Log.v(Dian1Application.TAG, "PlayerGesturePrevCommand");
		mPlayerEngine.prev();
	}
	
}
