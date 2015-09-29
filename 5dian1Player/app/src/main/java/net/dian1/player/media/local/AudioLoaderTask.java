package net.dian1.player.media.local;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import net.dian1.player.api.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioLoaderTask extends AsyncQueryHandler {

    public final static String[] DEF_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA};

    public final static String DEF_SELECTION_ALL = null;

//    public final static String DEF_SELECTION_LOCAL = MediaStore.Audio.Media.DATA + " like '%" + Constants.LOCAL_REGIX + "%'";
//
//    public final static String DEF_SELECTION_SDCARD = MediaStore.Audio.Media.DATA + " like '%" + Constants.SDCARD_REGIX + "%'";
//
//    public final static String DEF_SELECTION_USB = MediaStore.Audio.Media.DATA + " like '%" + Constants.USB_REGIX + "%'";

    //    public final static String[] DEF_SELECTION_ARGS = new String[] { "audio/mpeg", "audio/x-ms-wma", "audio/mp4", "audio/x-aac" };

    protected AudioLoaderManager mAudioManager;
    private Context mContext = null;

    public AudioLoaderTask(Context context, AudioLoaderManager am) {
        super(context.getContentResolver());
        mContext = context;
        mAudioManager = am;
    }

    public void loadData() {
        startQuery(0, (Object) null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AudioLoaderTask.DEF_PROJECTION, AudioLoaderTask.DEF_SELECTION_ALL,
                null, null);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cursor == null) {
            return;
        }
        new LoadSongsTask().execute(cursor);
    }

    class LoadSongsTask extends AsyncTask<Cursor, Void, List<Music>> {
        @Override
        protected List<Music> doInBackground(Cursor... params) {
            List<Music> songs = null;
            try {
                if (params[0].moveToFirst()) {
                    songs = getSongs(params[0]);
                }
            } finally {
                params[0].close();
            }
            return songs;
        }

        @Override
        protected void onPostExecute(List<Music> result) {
            mAudioManager.clear();
            mAudioManager.add(result);
            mAudioManager.notifyDataChange();
            super.onPostExecute(result);
        }
    }

    /**
     * getSongList()
     *
     * @param cursor
     */
    public List<Music> getSongs(Cursor cursor) {
        List<Music> songs = new ArrayList<Music>();
        do {
            Music song = null;
            song = new Music();
            //String fileName = getStringForChinese(cursor.getString(1));
            //song.setName(fileName);// file Name
            String fileTitle = getStringForChinese(cursor.getString(2));
            song.setName(fileTitle);// song name
            song.setDuration(cursor.getInt(3) / 1000);// play time
            String artist = getStringForChinese(cursor.getString(4));
            song.setArtist(artist);// artist
            //            song.setAlbumArt(cursor.getString(5)); // album
            final int albumId = cursor.getInt(6); // album id
            song.setAlbumId(albumId);
            //String album = getAlbumArt(mContext.getContentResolver(), albumId);
            //song.setAlbum(album);


            if (cursor.getString(9) != null) {// fileSize
                //float temp = cursor.getInt(9) / 1024f / 1024f;
                song.setFileSize(cursor.getInt(9));
//                DecimalFormat df = new DecimalFormat("####.00");
//                String sizeStr = df.format(temp);
//                song.setFileSize(sizeStr + "M");
            }

            String path = cursor.getString(10);
            if (path != null && path.contains("system/media/audio")) {
                // if audio in system path, ignore it
                continue;
            }
            song.setUrl(path);
            song.setStream(path);
            if (new File(song.getUrl()).exists()) {
                songs.add(song);
            }

        } while (cursor.moveToNext());
        return songs;
    }

    public static String getAlbumArt(ContentResolver resolver, int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = null;
        String album_art = null;
        try {
            cur = resolver.query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                    projection, null, null, null);

            if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
                cur.moveToNext();
                album_art = cur.getString(0);
            }
        } catch (Exception e) {
            Log.w("AudioLoaderTask", e.toString());
        } finally {
            if (cur != null) {
                cur.close();
                cur = null;
            }
        }
        return album_art;
    }

    public static String getStringForChinese(String value) {
        String newValue = value;
        try {
            if (value.equals(new String(value.getBytes("ISO-8859-1"), "ISO-8859-1"))) {
                newValue = new String(value.getBytes("ISO-8859-1"), "GBK");
            }
        } catch (Exception e) {
        }
        return newValue;
    }

}
