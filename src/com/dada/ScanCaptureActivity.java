package com.dada;

import com.dada.universalscanner.R;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class ScanCaptureActivity extends Activity {
	private static final String TAG = "ScanCaptureActivity";
	
	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
    private boolean mCameraConfigured = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.scan_capture);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPreview = (SurfaceView) findViewById(R.id.camera_preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);
	}

	@Override
	public void onResume() {
		super.onResume();

		mCamera = Camera.open();
		startPreview();
	}

	@Override
	public void onPause() {
		if (mInPreview)
			mCamera.stopPreview();

		mCamera.release();
		mCamera = null;
		mInPreview = false;

		super.onPause();
	}
	
    private void initPreview(int width, int height) 
    {
        if ( mCamera != null && mPreviewHolder.getSurface() != null) {
            try 
            {
                mCamera.setPreviewDisplay(mPreviewHolder);
            }
            catch (Throwable t) 
            {
                Log.e(TAG, "Exception in initPreview()", t);
                Toast.makeText(ScanCaptureActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
            
            if ( !mCameraConfigured ) 
            {
                Camera.Parameters parameters = mCamera.getParameters();
                
                /*int widthMeasured = mPreview.getMeasuredWidth();
                int heightMeasured = mPreview.getMeasuredHeight();*/
                parameters.setPreviewSize(640, 480); // hard coded the largest size for now
                mCamera.setParameters(parameters);
                //mCamera.setZoomChangeListener(this);
                
                mCameraConfigured = true;
            }
            /*
            */
        }
    }

    private void startPreview() 
    {
        if ( mCameraConfigured &&/**/ mCamera != null ) 
        {
            mCamera.startPreview();
            mInPreview = true;
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated( SurfaceHolder holder ) 
        {
        	// nothing
        }

        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) 
        {
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed( SurfaceHolder holder ) 
        {
            // nothing
        }
    };
    
}
