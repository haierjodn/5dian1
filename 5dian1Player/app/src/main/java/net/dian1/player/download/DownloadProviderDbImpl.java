/*
 * Copyright (C) 2011 Teleca Poland Sp. z o.o. <android@teleca.com>
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

package net.dian1.player.download;

import java.util.ArrayList;

import net.dian1.player.api.Music;

/**
 * DownloadProvider implementations. Uses SqlLite database to store
 * DownloadJobs.
 * 
 * @author Bartosz Cichosz
 * 
 */
public class DownloadProviderDbImpl implements DownloadProvider {

	private ArrayList<DownloadJob> mQueuedJobs;
	private ArrayList<DownloadJob> mCompletedJobs;
	private DownloadManager mDownloadManager;

	private DownloadDatabase mDb;

	private static final String DB_PATH = "/dian1.db";

	public DownloadProviderDbImpl(DownloadManager downloadManager) {
		mDownloadManager = downloadManager;

		mQueuedJobs = new ArrayList<DownloadJob>();
		mCompletedJobs = new ArrayList<DownloadJob>();

		mDb = new DownloadDatabaseImpl(DownloadHelper.getDownloadPath()
				+ DB_PATH);
		loadOldDownloads();
	}

	private void loadOldDownloads() {
		ArrayList<DownloadJob> oldDownloads = mDb.getAllDownloadJobs();
		for (DownloadJob dJob : oldDownloads) {
			if (dJob.getProgress() == 100) {
				mCompletedJobs.add(dJob);
			} else {
				mDownloadManager.download(dJob.getPlaylistEntry());
			}
		}
		//mDownloadManager.notifyObservers();
	}

	public ArrayList<DownloadJob> getAllDownloads() {
		ArrayList<DownloadJob> allDownloads = new ArrayList<DownloadJob>();
		allDownloads.addAll(mCompletedJobs);
		allDownloads.addAll(mQueuedJobs);
		return allDownloads;
	}

	public ArrayList<DownloadJob> getCompletedDownloads() {
		return mCompletedJobs;
	}

	public ArrayList<DownloadJob> getQueuedDownloads() {
		return mQueuedJobs;
	}

	public void downloadCompleted(final DownloadJob job) {
		mQueuedJobs.remove(job);
		mCompletedJobs.add(job);
		if(job.getErrorCode() == 0) {
			mDb.setStatus(job.getPlaylistEntry(), true);
		}
		mDownloadManager.notifyObservers(job);
	}

	public boolean queueDownload(DownloadJob downloadJob) {
		for (DownloadJob dJob : mCompletedJobs) {
			if (dJob.getPlaylistEntry().getMusic().getId() == downloadJob
					.getPlaylistEntry().getMusic().getId())
				return false;
		}

		for (DownloadJob dJob : mQueuedJobs) {
			if (dJob.getPlaylistEntry().getMusic().getId() == downloadJob
					.getPlaylistEntry().getMusic().getId())
				return false;
		}

		if (mDb.addToLibrary(downloadJob.getPlaylistEntry())) {
			mQueuedJobs.add(downloadJob);
			mDownloadManager.notifyObservers(downloadJob);
			return true;
		} else {
			return false;
		}
	}

	public void removeDownload(DownloadJob job) {
		if (job.getProgress() < 100) {
			job.cancel();
		}
		if(!mQueuedJobs.remove(job)) {
			mCompletedJobs.remove(job);
		}
		mDb.remove(job);
		mDownloadManager.notifyObservers(job);
	}

	@Override
	public boolean trackAvailable(Music music) {
		if(music != null) {
			for (DownloadJob dJob : mCompletedJobs) {
				if (music.getId() == dJob.getPlaylistEntry().getMusic().getId()) {
					return true;
				}
			}
		}
		return false;
	}
}
