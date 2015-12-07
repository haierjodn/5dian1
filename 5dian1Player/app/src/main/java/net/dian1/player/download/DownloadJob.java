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

package net.dian1.player.download;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.PlaylistEntry;

import java.util.concurrent.ExecutorService;

/**
 * Single remote file download task
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadJob {

	private ExecutorService executorService;

	private PlaylistEntry mPlaylistEntry;
	private String mDestination;

	private DownloadTask mDownloadTask;
	private DownloadJobListener mListener;

	private int mProgress;
	private int mTotalSize;
	private int mDownloadedSize;

	//0: no error, 1:network, ....
	private int errorCode;

	private int mStartId;

	private String mFormat;

	private DownloadManager mDownloadManager;

	public PlaylistEntry getPlaylistEntry() {
		return mPlaylistEntry;
	}

	public void setPlaylistEntry(PlaylistEntry playlistEntry) {
		mPlaylistEntry = playlistEntry;
	}

	public String getDestination() {
		return mDestination;
	}

	public void setDestination(String destination) {
		mDestination = destination;
	}

	public DownloadJob(ExecutorService service, PlaylistEntry playlistEntry,
					   String destination, int startId, String downloadFormat){
		executorService = service;
		mPlaylistEntry = playlistEntry;
		mDestination = destination;
		mProgress = 0;
		mStartId = startId;
		mFormat = downloadFormat;
		mDownloadManager = Dian1Application.getInstance().getDownloadManager();
	}

	public void start(){
		mDownloadTask = new DownloadTask(this);
		if(executorService != null) {
			mDownloadTask.executeOnExecutor(executorService);
		} else {
			mDownloadTask.execute();
		}
	}

	public void pause(){
		// TODO DownloadTask.pause()
	}

	public void resume(){
		// TODO DownloadTask.resume()
	}

	public void cancel(){
		mDownloadTask.cancel(true);
	}
	
	public void setListener(DownloadJobListener listener){
		mListener = listener;
	}
	
	public int getProgress(){
		return mProgress;
	}
	
	public void setProgress(int progress){
		mProgress = progress;
	}
	
	public void setTotalSize(int totalSize) {
		this.mTotalSize = totalSize;
	}

	public int getTotalSize() {
		return mTotalSize;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setDownloadedSize(int downloadedSize) {
		this.mDownloadedSize = downloadedSize;
		int oldProgress = mProgress;
		mProgress = (mDownloadedSize*100)/mTotalSize;
		if(mProgress != oldProgress) {
			mDownloadManager.notifyObservers(this);
		}
	}

	public int getDownloadedSize() {
		return mDownloadedSize;
	}

	public void notifyDownloadStarted(){
		if(mListener != null) {
			mListener.downloadStarted();
		}
		mProgress = 0;
	}
	
	public void notifyDownloadEnded(int errorCode){
		if(errorCode <= 0 && !mDownloadTask.isCancelled()){
			mProgress = 100;
			if(mListener != null) {
				mListener.downloadEnded(this);
			}
		} else {
			this.errorCode = errorCode;
			if(mListener != null) {
				mListener.downloadEnded(this);
			}
		}
	}

	public void setStartId(int mStartId) {
		this.mStartId = mStartId;
	}

	public int getStartId() {
		return mStartId;
	}

	public void setFormat(String mFormat) {
		this.mFormat = mFormat;
	}

	public String getFormat() {
		return mFormat;
	}

}
