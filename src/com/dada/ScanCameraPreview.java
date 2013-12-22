package com.dada;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScanCameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "ScanCameraPreview";

	private SurfaceHolder mHolder;
	private boolean isPreviewRunning;
	private Camera mCamera;

	public ScanCameraPreview(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		Log.e(TAG, "ScanCameraPreview created");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (isPreviewRunning) {
			try {
				mCamera.stopPreview();
				Log.d(TAG, "Restarting camera preview...");
			} catch (Exception e) {
				Log.e(TAG, "Error stopping camera preview", e);
			}
		}

		try {
			mCamera.startPreview();
			isPreviewRunning = true;
			Log.d(TAG, "Camera preview restarted!");
		} catch (Exception e) {
			Log.e(TAG, "Error starting camera preview", e);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		for (int i = 0; i < 100; i++) {
			try {
				mCamera = Camera.open();
				Log.d(TAG, "Camera opened!");
			} catch (Exception e) {
				Log.e(TAG, "Fail to open Camera for " + (i + 1) + " times.", e);
				return;
			}
			if (mCamera != null)
				break;
		}
		
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Throwable ignored) {
			Log.e(TAG, "Error setting camera preview.", ignored);
		}

		isPreviewRunning = false;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (isPreviewRunning && mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			Log.d(TAG, "Camera released!");
			isPreviewRunning = false;
		}
	}

}
