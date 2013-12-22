package com.dada;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ScanService extends Service {

    private static final String TAG = "ScanService";
    private static final String LIVE_CARD_ID = "scan";
    
    //private ScanMainDrawer mCallback;

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Intent intentForNewActivity = new Intent(ScanService.this, ScanCaptureActivity.class);
        intentForNewActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentForNewActivity);
    	/*
        if (mLiveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            mLiveCard = mTimelineManager.getLiveCard(LIVE_CARD_ID);

            // Keep track of the callback to remove it before unpublishing.
            mCallback = new ScanMainDrawer(this);
            mLiveCard.enableDirectRendering(true).getSurfaceHolder().addCallback(mCallback);
            mLiveCard.setNonSilent(true);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish();
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }
        */

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            /*
            if (mCallback != null) {
                mLiveCard.getSurfaceHolder().removeCallback(mCallback);
            }
            */
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }
}
