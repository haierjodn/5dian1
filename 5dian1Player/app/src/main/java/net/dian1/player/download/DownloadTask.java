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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.Music;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.util.AudioUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.lidroid.xutils.http.client.HttpRequest;


/**
 * File download thread
 * 
 * @author Lukasz Wisniewski
 */
public class DownloadTask extends AsyncTask<Void, Integer, Integer>{

	DownloadJob mJob;
	
	public DownloadTask(DownloadJob job){
		mJob = job;
	}
	
	@Override
	public void onPreExecute() {
		mJob.notifyDownloadStarted();
		super.onPreExecute();
	}

	@Override
	public Integer doInBackground(Void... params) {
		try {
			return downloadFile(mJob) ? 0 : 2;
		} catch (IOException e) {
			Log.e(Dian1Application.TAG, "Download file faild reason-> " + e.getMessage());
			return 1;
		}
	}

	@Override
	public void onPostExecute(Integer result) {
		mJob.notifyDownloadEnded(result);
		super.onPostExecute(result);
	}

	public static Boolean downloadFile(DownloadJob job) throws IOException{
		
		// TODO rewrite to apache client
		
		PlaylistEntry mPlaylistEntry = job.getPlaylistEntry();
		String mDestination = job.getDestination();

		Music engineMusic = mPlaylistEntry.getMusic();
		String url = engineMusic.getFirstMusicNetUrl();

		if(TextUtils.isEmpty(url)) {
			engineMusic = requestMusicDetail(engineMusic.getId());
			url = engineMusic.getFirstMusicNetUrl();
			job.getPlaylistEntry().setMusic(engineMusic);
		}

		//url = "http://room2.5dian1.net/低音环绕·极品女声1/15.我和草原有个约定.mp3";
		if(TextUtils.isEmpty(url)) {
			return false;
		}
		URL u = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		connection.setRequestMethod("GET");
		//c.setDoOutput(true);
		//c.setDoInput(true);
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Content-Type", "audio/mpeg");
		connection.connect();
		job.setTotalSize(connection.getContentLength());

		Log.i(Dian1Application.TAG, "creating file");
		
		String path = DownloadHelper.getAbsolutePath(mPlaylistEntry, mDestination);
		String fileName = DownloadHelper.getFileName(mPlaylistEntry, job.getFormat());


		try{
			// Create multiple directory
			boolean success = (new File(path)).mkdirs();
			if (success) {
				Log.i(Dian1Application.TAG, "Directory: " + path + " created");
			}    

		}catch (Exception e){//Catch exception if any
			Log.e(Dian1Application.TAG, "Error creating folder", e);
			return false;
		}

		File outFile = new File(path, fileName);

		FileOutputStream fos = new FileOutputStream(outFile);

		InputStream in = connection.getInputStream();

		if(in == null){
			// When InputStream is a NULL
			fos.close();
			return false;
		}

		byte[] buffer = new byte[1024];
		int lenght = 0;
		while ( (lenght = in.read(buffer)) > 0 ) {
			fos.write(buffer, 0, lenght);
			job.setDownloadedSize(job.getDownloadedSize()+lenght);
		}
		fos.close();

		mPlaylistEntry.getMusic().getFirstMusicUrlInfo().setLocalUrl(outFile.getAbsolutePath());
		//downloadCover(job);
		return true;
	}

	private static Music requestMusicDetail(int musicId) {
		ApiRequest musicDetailRequest = new ApiRequest(Dian1Application.getInstance(), ApiData.MusicDetailApi.URL, net.dian1.player.model.Music.class,
				ApiData.MusicDetailApi.getParams(musicId), null).setHttpMethod(HttpRequest.HttpMethod.GET);

		net.dian1.player.model.Music musicDetail = ApiManager.getInstance().executeSync(musicDetailRequest, net.dian1.player.model.Music.class);
		if(musicDetail != null) {
			return AudioUtils.convertMusic(musicDetail);
		}
		return null;
	}
	
	
	private static void downloadCover(DownloadJob job) {

		PlaylistEntry mPlaylistEntry = job.getPlaylistEntry();
		String mDestination = job.getDestination();
		String path = DownloadHelper.getAbsolutePath(mPlaylistEntry,
				mDestination);
		File file = new File(path + "/" + "cover.jpg");
		// check if cover already exists
		if (file.exists()) {
			Log.v(Dian1Application.TAG, "File exists - nothing to do");
			return;
		}

		String albumUrl = mPlaylistEntry.getAlbum().getImage();
		if (albumUrl == null) {
			Log.w(Dian1Application.TAG,
					"album Url = null. This should not happened");
			return;
		}
		albumUrl = albumUrl.replace("1.100", "1.500");

		InputStream stream = null;
		URL imageUrl;
		Bitmap bmp = null;

		// download cover
		try {
			imageUrl = new URL(albumUrl);

			try {
				stream = imageUrl.openStream();
				bmp = BitmapFactory.decodeStream(stream);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.v(Dian1Application.TAG, "download Cover IOException");
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.v(Dian1Application.TAG, "download CoverMalformedURLException");
			e.printStackTrace();
		}

		// save cover to album directory
		if (bmp != null) {

			try {
				file.createNewFile();
				OutputStream outStream = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				outStream.close();

				Log.v(Dian1Application.TAG, "Album cover saved to sd");

			} catch (FileNotFoundException e) {
				Log.w(Dian1Application.TAG, "FileNotFoundException");

			} catch (IOException e) {
				Log.w(Dian1Application.TAG, "IOException");
			}

		}
	}

}
