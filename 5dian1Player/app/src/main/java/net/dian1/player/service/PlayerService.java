/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dian1.player.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.MainActivity;
import net.dian1.player.activity.PlayerActivity;
import net.dian1.player.api.Album;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.media.PlayerEngineImpl;
import net.dian1.player.media.PlayerEngineListener;

import java.util.List;

/**
 * Background player
 *
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class PlayerService extends Service {
    private static final String LASTFM_INTENT = "fm.last.android.metachanged";
    private static final String SIMPLEFM_INTENT = "com.adam.aslfms.notify.playstatechanged";

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_BIND_LISTENER = "bind_listener";

    private WifiManager mWifiManager;
    private WifiLock mWifiLock;
    private PlayerEngine mPlayerEngine;
//    private TelephonyManager mTelephonyManager;
//    private PhoneStateListener mPhoneStateListener;
    private AudioManager mAudioManager;
    private boolean mPausedByTransientLossOfFocus;

    private NotificationManager mNotificationManager = null;
    private static final int PLAYING_NOTIFY_ID = 667667;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener(){
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange){
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mPlayerEngine != null) {
                        if(mPlayerEngine.isPlaying()) {
                            //we do not need get focus back in this situation
                            //会长时间失去，所以告知下面的判断，获得焦点后不要自动播放
                            mPausedByTransientLossOfFocus = false;
                            mPlayerEngine.pause();//因为会长时间失去，所以直接暂停
                            mAudioManager.abandonAudioFocus(this);
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mPlayerEngine != null) {
                        if (mPlayerEngine.isPlaying()) {
                            //短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
                            mPausedByTransientLossOfFocus = true;
                            mPlayerEngine.pause();
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //重新获得焦点，且符合播放条件，开始播放
                    if(!mPlayerEngine.isPlaying() && mPausedByTransientLossOfFocus){
                        mAudioManager.requestAudioFocus(this,
                                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        mPausedByTransientLossOfFocus = false;
                        mPlayerEngine.play();
                    }
                    break;
            }
        }};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(Dian1Application.TAG, "Player Service onCreate");

        mPlayerEngine = new PlayerEngineImpl();
        mPlayerEngine.addListener(mLocalEngineListener);

        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

//        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        mPhoneStateListener = new PhoneStateListener() {
//
//            @Override
//            public void onCallStateChanged(int state, String incomingNumber) {
//                Log.e(Dian1Application.TAG, "onCallStateChanged");
//                if (state == TelephonyManager.CALL_STATE_IDLE) {
//                    // resume playback
//                } else {
//                    if (mPlayerEngine != null) {
//                        mPlayerEngine.pause();
//                    }
//                }
//            }
//
//        };
//        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiLock = mWifiManager.createWifiLock(Dian1Application.TAG);
        mWifiLock.setReferenceCounted(false);

        Dian1Application.getInstance().setConcretePlayerEngine(mPlayerEngine);
        mRemoteEngineListeners = Dian1Application.getInstance().fetchPlayerEngineListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        super.onStartCommand(intent, flag, startId);
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }

        String action = intent.getAction();
        Log.i(Dian1Application.TAG, "Player Service onStart - " + action);

        if (action.equals(ACTION_STOP)) {
            stopSelfResult(startId);
            return Service.START_NOT_STICKY;
        }

        if (action.equals(ACTION_BIND_LISTENER)) {
            mRemoteEngineListeners = Dian1Application.getInstance().fetchPlayerEngineListener();
            return Service.START_NOT_STICKY;
        }

        // we need to have up-to-date playlist if any of play,next,prev buttons is pressed
        updatePlaylist();

        if (action.equals(ACTION_PLAY)) {
            mPlayerEngine.play();
            return Service.START_NOT_STICKY;
        }

        if (action.equals(ACTION_NEXT)) {
            mPlayerEngine.next();
            return Service.START_NOT_STICKY;
        }

        if (action.equals(ACTION_PREV)) {
            mPlayerEngine.prev();
            return Service.START_NOT_STICKY;
        }
        return Service.START_NOT_STICKY;
    }

    /**
     * Fetches a new playlist if its reference address differs from the current one
     */
    private void updatePlaylist() {
        if (mPlayerEngine.getPlaylist() != Dian1Application.getInstance().fetchPlaylist()) {
            mPlayerEngine.openPlaylist(Dian1Application.getInstance().fetchPlaylist());
        }
    }

    @Override
    public void onDestroy() {
        Log.i(Dian1Application.TAG, "Player Service onDestroy");
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mPlayerEngine.removeListener(mLocalEngineListener);
        Dian1Application.getInstance().setConcretePlayerEngine(null);
        mPlayerEngine.stop();
        mPlayerEngine = null;
        // unregister listener
//        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }

    /**
     * Hint: if necessary this can be extended to ArrayList of listeners in the future, though
     * I do not expect that it will be necessary
     */
    private List<PlayerEngineListener> mRemoteEngineListeners;

    /**
     * Sends notification to the status bar + passes other notifications to remote listeners
     */
    private PlayerEngineListener mLocalEngineListener = new PlayerEngineListener() {

        @Override
        public void onTrackBuffering(int percent) {
            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackBuffering(percent);
                }
            }
        }

        @Override
        public void onTrackChanged(PlaylistEntry playlistEntry) {
            displayNotifcation(playlistEntry);
            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackChanged(playlistEntry);
                }
            }

            // Scrobbling
            boolean scrobblingEnabled = PreferenceManager.getDefaultSharedPreferences(PlayerService.this).getBoolean("scrobbling_enabled", false);
            if (scrobblingEnabled) {
                scrobblerMetaChanged();
            }
        }

        @Override
        public void onTrackProgress(int seconds) {
            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackProgress(seconds);
                }
            }
        }

        @Override
        public void onTrackStop() {
            // allow killing this service
            // NO-OP setForeground(false);
            mWifiLock.release();

            mNotificationManager.cancel(PLAYING_NOTIFY_ID);

            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackStop();
                }
            }
        }

        @Override
        public boolean onTrackStart() {
            // prevent killing this service
            // NO-OP setForeground(true);
            mWifiLock.acquire();

            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    if (!mRemoteEngineListeners.get(i).onTrackStart()) {
                        return false;
                    }
                }
            }

            boolean wifiOnlyMode = PreferenceManager.getDefaultSharedPreferences(PlayerService.this).getBoolean("wifi_only", false);

            // wifi only mode
            if (wifiOnlyMode && !mWifiManager.isWifiEnabled()) {
                return false;
            }

            // roaming protection
//            boolean roamingProtection = PreferenceManager.getDefaultSharedPreferences(PlayerService.this).getBoolean("roaming_protection", true);
//            if (!mWifiManager.isWifiEnabled()) {
//                if (roamingProtection && mTelephonyManager.isNetworkRoaming())
//                    return false;
//            }

            return true;
        }

        @Override
        public void onTrackPause() {
            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackPause();
                }
            }
        }

        @Override
        public void onTrackStreamError() {
            if (mRemoteEngineListeners != null) {
                for (int i = 0; i < mRemoteEngineListeners.size(); i++) {
                    mRemoteEngineListeners.get(i).onTrackStreamError();
                }
            }
        }

    };

    /**
     * Send changes to selected scrobbling application
     */
    private void scrobblerMetaChanged() {
        PlaylistEntry entry = mPlayerEngine.getPlaylist().getSelectedTrack();

        if (entry != null) {
            String scrobblerApp = PreferenceManager.getDefaultSharedPreferences(PlayerService.this).getString("scrobbler_app", "");
            assert (scrobblerApp.length() > 0);

            if (Log.isLoggable(Dian1Application.TAG, Log.INFO)) {
                Log.i(Dian1Application.TAG, "Scrobbling track " + entry.getMusic().getName() + " via " + scrobblerApp);
            }

            if (scrobblerApp.equalsIgnoreCase("lastfm")) {
                Intent i = new Intent(LASTFM_INTENT);
                i.putExtra("artist", entry.getAlbum().getArtistName());
                i.putExtra("album", entry.getAlbum().getName());
                i.putExtra("track", entry.getMusic().getName());
                i.putExtra("duration", entry.getMusic().getDuration() * 1000); // duration in milliseconds
                sendBroadcast(i);
            } else if (scrobblerApp.equalsIgnoreCase("simplefm")) {
                Intent i = new Intent(SIMPLEFM_INTENT);
                i.putExtra("app-name", getResources().getString(R.string.app_name));
                i.putExtra("app-package", "net.dian1.player");
                i.putExtra("state", 0);    // state 0 = START - track has started playing
                i.putExtra("artist", entry.getAlbum().getArtistName());
                i.putExtra("track", entry.getMusic().getName());
                i.putExtra("duration", entry.getMusic().getDuration()); // duration in seconds
                i.putExtra("album", entry.getAlbum().getName());
                i.putExtra("track-no", entry.getMusic().getNumAlbum());
                sendBroadcast(i);
            } else {
                // somehow the scrobbling app is not selected properly
            }
        }
    }

    private void displayNotifcation(PlaylistEntry playlistEntry) {
        Album album = playlistEntry.getAlbum();

        String artist = (album == null ? playlistEntry.getMusic().getArtist() : album.getArtistName());

        String notificationMessage = playlistEntry.getMusic().getName() + " - " + artist;

        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.noti_small)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.noti_small))
                .setTicker(notificationMessage)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationMessage)
                /*.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))*/;
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        mNotificationManager.notify(PLAYING_NOTIFY_ID, notification);

        addTaskStack(this, MainActivity.class);
    }

    void addTaskStack(Context context, Class<?> stack) {
        TaskStackBuilder.create(context).addParentStack(stack);
    }

}
