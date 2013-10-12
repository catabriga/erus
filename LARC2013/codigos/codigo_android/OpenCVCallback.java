package erus.android.erusbot;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.util.Log;

public class OpenCVCallback extends BaseLoaderCallback
{
	private static final String TAG = "OpenCVCallback";
	
	private CodigoAndroidActivity activity;
	
	public OpenCVCallback(CodigoAndroidActivity activity)
	{
		super(activity);
		
		this.activity = activity;
	}

	public void loadOpenCV(Activity AppContext)
	{
		Log.i(TAG, "Trying to load OpenCV library");
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, AppContext, this))
		{
			Log.e(TAG, "Cannot connect to OpenCV Manager");
		}
	}
	
	@Override
	public void onManagerConnected(int status) 
	{
		switch (status) 
		{
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");	
				
				activity.finishCreation();

			} break;
			
			default:
			{
				super.onManagerConnected(status);
			} break;
		}
	}
	
}
