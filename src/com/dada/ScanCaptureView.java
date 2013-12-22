package com.dada;

import com.dada.universalscanner.R;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class ScanCaptureView extends FrameLayout {
	private static final String TAG = "ScanCaptureView";

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;

	public ScanCaptureView(Context context) {
		this(context, null, 0);
	}

	public ScanCaptureView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScanCaptureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		/*
		 * mPreview = new ScanCameraPreview(context); Log.d(TAG,
		 * "ScanCameraPreview created!");
		 */

		LayoutInflater.from(context).inflate(R.layout.scan_capture, this);
		mPreview = (SurfaceView) findViewById(R.id.camera_preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);
		Log.d(TAG, "preview set up");
	}

	private void initPreview(int width, int height) {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			} catch (Throwable t) {
				Log.e(TAG, "Filed to open camera!", t);
			}
		}
		else if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
				Log.d(TAG, "preview display done");
			} catch (Throwable t) {
				Log.e(TAG, "Exception in initPreview()", t);
			}
			/*
			 * if ( !mCameraConfigured ) { Camera.Parameters parameters =
			 * mCamera.getParameters(); parameters.setPreviewSize(1920, 1080);
			 * // hard coded the largest size for now
			 * mCamera.setParameters(parameters);
			 * mCamera.setZoomChangeListener(this);
			 * 
			 * mCameraConfigured = true; }
			 */
		}
	}

	private void startPreview() {
		if ( /* mCameraConfigured && */mCamera != null) {
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// nothing
			Log.d(TAG, "surfaceCreated");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.d(TAG, "surfaceChanged");
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// nothing
			Log.d(TAG, "surfaceDestroyed");
		}
	};
	/*
	 * public static Camera getCameraInstance() { Camera c = null; for (int i =
	 * 0; i < 10; i++) { try { c = Camera.open(); // attempt to get a Camera
	 * instance Log.d(TAG, "Opened the camera"); } catch (Exception e) { //
	 * Camera is not available (in use or does not exist) Log.e(TAG,
	 * "Failed to open the camera [" + (i + 1) + " times]: " + e.getMessage());
	 * } if (c == null) { try { Thread.sleep(100); } catch (InterruptedException
	 * e1) { Log.e(TAG, "Failed to sleep: " + e1.getMessage()); } continue; }
	 * else { break; } } return c; // returns null if camera is unavailable }
	 */
}
