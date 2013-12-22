package com.dada;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class ScanMainDrawer implements SurfaceHolder.Callback {
    private static final String TAG = "ScanMainDrawer";
    
    /**
     * The (absolute) pitch angle beyond which the compass will display a message telling the user
     * that his or her head is at too steep an angle to be reliable.
     */
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;

    /** The refresh rate, in frames per second, of the compass. */
    private static final int REFRESH_RATE_FPS = 45;

    /** The duration, in milliseconds, of one frame. */
    private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;
    
    private SurfaceHolder mHolder;
    private ScanCaptureView mCaptureView;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private RenderThread mRenderThread;
    
    public ScanMainDrawer(Context context) {
    	mCaptureView = new ScanCaptureView(context);
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mSurfaceWidth = width;
		mSurfaceHeight = height;
        doLayout();		
        Log.d(TAG, "Relayout done.");
	}

	private void doLayout() {
		// Measure and layout the view with the canvas dimensions.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight, View.MeasureSpec.EXACTLY);

        mCaptureView.measure(measuredWidth, measuredHeight);
        mCaptureView.layout(
                0, 0, mCaptureView.getMeasuredWidth(), mCaptureView.getMeasuredHeight());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        mHolder = holder;

        mRenderThread = new RenderThread();
        mRenderThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        mHolder = null;		
        mRenderThread.quit();
	}

    /**
     * Repaints the compass.
     */
    private synchronized void repaint() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
        	mCaptureView.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
    
    /**
     * Redraws the compass in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                repaint();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }
}
