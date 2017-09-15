package kr.co.hs.mediascanner;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.io.File;

/**
 * Created by privacydev on 2017. 9. 15..
 */

public class HsMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient{
    private MediaScannerConnection mMediaScannerConnection;
    final Object mSyncObject;
    private Uri mLastUri = null;
    private Context mContext;

    public HsMediaScanner(Context context) {
        this.mContext = context;
        this.mSyncObject = new Object();
        mMediaScannerConnection = new MediaScannerConnection(getContext(), this);
        synchronized (mSyncObject){
            mMediaScannerConnection.connect();
            try {
                mSyncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMediaScannerConnected() {
        synchronized (mSyncObject){
            mSyncObject.notify();
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        synchronized (mSyncObject){
            mLastUri = uri;
            mSyncObject.notify();
        }
    }


    public Context getContext() {
        return mContext;
    }

    public String getExtexsion(File file) {
        try {
            int e = file.getName().lastIndexOf(".");
            String ext = file.getName().substring(e + 1);
            return ext;
        } catch (Exception var3) {
            return null;
        }
    }

    public Uri getUri(File file){
        String extension = getExtexsion(file);
        String mimeType = null;
        try{
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }catch (Exception e){
            mimeType = null;
        }
        return getUri(file, mimeType);
    }

    public Uri getUri(File file, String mimeType){
        synchronized (mSyncObject){
            mMediaScannerConnection.scanFile(file.getAbsolutePath(), mimeType);
            try {
                mSyncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mLastUri;
    }

    public void close(){
        synchronized (mSyncObject){
            mMediaScannerConnection.disconnect();
        }
    }
}
