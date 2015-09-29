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
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.DownloadActivity;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.download.DownloadHelper;
import net.dian1.player.download.DownloadJob;
import net.dian1.player.download.DownloadJobListener;
import net.dian1.player.download.DownloadProvider;
import net.dian1.player.download.MediaScannerNotifier;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO sd card listener
/**
 * Background download manager
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadService extends Service {

	public static final String ACTION_ADD_TO_DOWNLOAD = "add_to_download";

	public static final String EXTRA_PLAYLIST_ENTRY = "playlist_entry";

	private static final int DOWNLOAD_NOTIFY_ID = 667668;

	private NotificationManager mNotificationManager = null;

	private DownloadProvider mDownloadProvider;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		Log.i(Dian1Application.TAG, "DownloadService.onCreate");
		mNotificationManager = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
		mDownloadProvider = Dian1Application.getInstance().getDownloadManager().getProvider();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if(intent == null){
			return START_NOT_STICKY;
		}

		String action = intent.getAction();
		Log.i(Dian1Application.TAG, "DownloadService.onStart - " + action);

		if(action.equals(ACTION_ADD_TO_DOWNLOAD)){
			PlaylistEntry entry = (PlaylistEntry) intent.getSerializableExtra(EXTRA_PLAYLIST_ENTRY);
			addToDownloadQueue(entry, startId);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(Dian1Application.TAG, "DownloadService.onDestroy");
	}

	private DownloadJobListener mDownloadJobListener = new DownloadJobListener(){

		@Override
		public void downloadEnded(DownloadJob job) {
			mDownloadProvider.downloadCompleted(job);
			displayNotifcation(job);
			new MediaScannerNotifier(DownloadService.this, job);
		}

		@Override
		public void downloadStarted() {
		}

	};
	
	private void displayNotifcation(DownloadJob job)
	{

		String notificationMessage = job.getPlaylistEntry().getMusic().getName() + " - " + job.getPlaylistEntry().getAlbum().getArtistName();

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, notificationMessage, System.currentTimeMillis() );

		PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
				new Intent( this, DownloadActivity.class ), 0);

		notification.setLatestEventInfo( this, getString(R.string.downloaded),
				notificationMessage, contentIntent );
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify( DOWNLOAD_NOTIFY_ID, notification );
	}
	

	
	public void addToDownloadQueue(PlaylistEntry entry, int startId) {
		
		// check database if record already exists, if so abandon starting
		// another download process
		String downloadPath = DownloadHelper.getDownloadPath();
		String downloadFormat = Dian1Application.getInstance().getDownloadFormat();
		DownloadJob downloadJob = new DownloadJob(executorService, entry, downloadPath, startId, downloadFormat);
		
		if(mDownloadProvider.queueDownload(downloadJob)){
			downloadJob.setListener(mDownloadJobListener);
			downloadJob.start();
		}
	}

	public void notifyScanCompleted() {
		if(mDownloadProvider.getQueuedDownloads().size() == 0){
			stopSelf();
		}
	}
}
