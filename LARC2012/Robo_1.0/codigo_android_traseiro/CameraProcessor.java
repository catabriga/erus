package erus.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraProcessor extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	private static final String TAG = "CameraPreview";
	private static final int blackColor = 0;
	private static final int redColor = 1;
	private static final int blueColor = 2;
	
	private static final Scalar YELLOW = new Scalar(255, 255, 0, 0);
	private static final Scalar BLUE = new Scalar(0, 0, 255, 0);
	
	private OpenCVCallback opencv;
	
	private SurfaceHolder holder;
	private Camera camera;
	private ColorBlobDetector CBD;
	private Mat matRGB;
	private Mat matYUV;
	private byte[] bytesRGB;
	private int frameCount;
	private int frameWidth;
	private int frameHeight;
	
	private Scalar[] colors;
	private Scalar[] colorsRadius;
	private double[] minContourAreas;
	
	private boolean finishSurfaceCreate;
	
	private List<Can> listOfCans;
	private int minBlue;
//	private Point trashPosition;
//	private int trashSize;
	private Trash trash;
	
	public CameraProcessor(CodigoAndroidActivity activity) 
	{
		super(activity);		
		
		opencv = new OpenCVCallback(activity);
		opencv.loadOpenCV(activity);
		
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);	
		
		frameCount = 0;
		frameWidth = 0;
		frameHeight = 0;
		
		listOfCans = new ArrayList<Can>();
		
//		trashPosition = new Point();
		trash = new Trash();
		trash.position = new Point();
		
		finishSurfaceCreate = false;
		// Nothing related to opencv can be initialized on this method, because opencv is not initialized yet
		// Some initialization is left for the surfaceCreated and surfaceChanged methods, because when they
		// are called opencv will have been sucessfully initialized
				
	}
		
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	{			
		Camera.Parameters params = camera.getParameters();
		
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        int minWidth = Integer.MAX_VALUE;
        int minHeight = Integer.MAX_VALUE;
        
		for (Camera.Size size : sizes)
		{
			if (size.height < minHeight)
			{
				minWidth = size.width;
				minHeight = size.height;				
			}
		}
		
		params.setPreviewSize(minWidth, minHeight);	
		camera.setParameters(params);
		
		params = camera.getParameters();
		this.frameWidth = params.getPreviewSize().width;
		this.frameHeight = params.getPreviewSize().height;
        int size = frameWidth * frameHeight;
        
        matYUV = new Mat(frameHeight + frameHeight / 2, frameWidth, CvType.CV_8UC1);
        matRGB = new Mat();
        bytesRGB = new byte[3*size];
        
		camera.startPreview();
    }

	
	public void surfaceCreated(SurfaceHolder holder) 
	{
		camera = Camera.open();
                     
        try
        {
			camera.setPreviewDisplay(holder);
		} 
        catch (IOException e) 
        {
        	Log.i(TAG, "setPreviewDisplay failed");
			e.printStackTrace();
		}
      
       camera.setPreviewCallback(this);	// This will make the function onPreviewFrame being called on every new frame
	
       // Initializing the color blob detector now because it needs opencv
       ColorCalibrator cc = new ColorCalibrator();
//       cc.readCalibrationFile();
   	   colors=cc.getColors();
   	   colorsRadius=cc.getColorsRadius();
   	   minContourAreas=cc.getMinContourAreas();
       CBD = new ColorBlobDetector(cc.getNumColors());
       CBD.setRGBColor(colors, colorsRadius, minContourAreas);
       
       finishSurfaceCreate = true;
	}
	
	public boolean getStatusSurfaceCreate()
	{
		return finishSurfaceCreate;
	}

	
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		camera.stopPreview();
		camera = null;
	}
	
	public synchronized int getFrameCount()
	{
		return frameCount;
	}
	
	public synchronized byte[] getFrameData()
	{	
		//byte[] data = new byte[bytesRGB.length];
		//System.arraycopy(bytesRGB, 0, data, 0, data.length);
		
		//return data;
		
		return bytesRGB;
	}
	
	public int getFrameWidth()
	{
		return frameWidth;
	}
	
	public int getFrameHeight()
	{
		return frameHeight;
	}
	
	private Scalar invertColor(Scalar color)
	{
		Scalar invertedColor = new Scalar(255-color.val[0], 255-color.val[1], 255-color.val[2], color.val[3]);
	
		return invertedColor;
	}
	
	private void drawCansLocations(Mat mat)
	{
		Iterator<Can> itr = listOfCans.iterator();
		
		while(itr.hasNext())
		{
			Can can = itr.next();
			
			Point p = can.position;
			
			p.y = can.minY;
			
			Core.circle(mat, p, 3, YELLOW);			
		}
	}
	
	private void drawTrashLocations(Mat mat)
	{		
		Core.circle(mat, trash.position, 3, BLUE);
	}
	
/*	private void drawCatchableRegion(Mat mat)
	{		
		int width = getFrameWidth();		
		int robotCenter = width/2 + RobotBrain.ROBOT_CENTER_OFFSET;		
				
		Core.rectangle(mat, new Point(robotCenter - RobotBrain.CATCHABLE_CAN_LIMIT_X/2, RobotBrain.CATCHABLE_CAN_LIMIT_Y_MIN), new Point(robotCenter + RobotBrain.CATCHABLE_CAN_LIMIT_X/2, RobotBrain.CATCHABLE_CAN_LIMIT_Y_MAX), new Scalar(0, 255, 0, 0));
	}*/
	
	private void calculateBlueLimits(List<MatOfPoint> blueContour)
	{	
		Iterator<MatOfPoint> itr = blueContour.iterator();
				
		this.minBlue = Integer.MAX_VALUE;
		
		while(itr.hasNext())
		{
			MatOfPoint mp = (MatOfPoint) itr.next();
						
			org.opencv.core.Point[] pt = mp.toArray();
			
			for(int i=0; i < pt.length; i++)
			{
				if(minBlue > pt[i].y)
				{
					minBlue = (int) pt[i].y;
				}				
			}
						
		}	
	}
	
	private void drawBlueLimits(Mat mat)
	{
		Core.line(mat, new Point(0, minBlue), new Point(mat.cols(), minBlue), new Scalar(255, 128, 50, 0));	
	}
	
	public void onPreviewFrame(byte[] data, Camera camera) 
	{
		//Log.i("PreviewFrame", "data length: "+data.length);
		
		matYUV.put(0, 0, data);
		Imgproc.cvtColor(matYUV, matRGB, Imgproc.COLOR_YUV420sp2RGB, 3);
				
		int imgBoundary = matRGB.rows()/3;
		
		Mat subMatRGB = matRGB.submat( 0, imgBoundary, 0, matRGB.cols());
		CBD.process(subMatRGB);
		
		synchronized(this)
		{		
			calculateBlueLimits(CBD.getContours()[blueColor]);
			listOfCans = getCenterObjBlack(CBD.getContours()[blackColor]);
			getTrashPosition(CBD.getContours()[redColor]);
			
			for (int i = 0; i < CBD.getNumColors(); i++)
			{
				Imgproc.drawContours(matRGB, CBD.getContours()[i], -1, invertColor(CBD.getRGBColors()[i]));
			}		
			Core.line(matRGB, new Point(0, imgBoundary), new Point(matRGB.cols(), imgBoundary), new Scalar(255, 0, 0, 0));
			drawBlueLimits(matRGB);
//			drawCatchableRegion(matRGB);
			drawCansLocations(matRGB);
			drawTrashLocations(matRGB);
			
			matRGB.get(0, 0, bytesRGB);	
			frameCount++;			
		}	
		
	}


//	List<PointF> listCenterObjBlack = getCenterObjBlack(CBD.getContours()[0]);
	private List<Can> getCenterObjBlack(List<MatOfPoint> blackContour)
	{
//		List<MatOfPoint> blackCoutour = CBD.getContours()[0];//get list of black contour
		Iterator<MatOfPoint> itr = blackContour.iterator();
		List<Can> listCenterObjBlack = new ArrayList<Can>(blackContour.size());
				
		while(itr.hasNext())
		{
			MatOfPoint mp = (MatOfPoint) itr.next();
						
			org.opencv.core.Point[] pt = mp.toArray();
			
			// Only goes after blobs bigger than a certain amount
			if(pt.length > 10)
			{
				int minY = Integer.MAX_VALUE;
				Point center = new Point(0,0);
				for(int i=0; i < pt.length; i++)
				{
					center.x += pt[i].x;
					center.y += pt[i].y;
					
					if(pt[i].y < minY)
					{
						minY = (int) pt[i].y;
					}
				}
				center.x /= pt.length;
				center.y /= pt.length;
				
				Can can = new Can();
				can.position = center;
				can.size = pt.length;
				can.minY = minY;
								
				listCenterObjBlack.add(can);
			}						
		}
		
		return listCenterObjBlack;		
	}
	
	private void getTrashPosition(List<MatOfPoint> redContour)
	{
		this.trash.size = 0;
		int maxCountor = 0;
		
		Iterator<MatOfPoint> itr = redContour.iterator();
		
		while(itr.hasNext())
		{
			MatOfPoint mp = (MatOfPoint) itr.next();
			
			Point[] pt = mp.toArray();
			
			if(pt.length > maxCountor && pt.length > 20)
			{
				Point center = new Point(0,0);
				int minY = Integer.MAX_VALUE;
				for(int i=0; i < pt.length; i++)
				{
					center.x += pt[i].x;
					center.y += pt[i].y;
					
					if(pt[i].y < minY)
					{
						minY = (int) pt[i].y;
					}
				}
				maxCountor = pt.length;
				trash.position.x = center.x / pt.length;
				trash.position.y = center.y / pt.length;
				trash.size = pt.length;
				trash.minY = minY;
			}
		}
	}
	
	public synchronized Trash getTrashPosition()
	{
		return trash;
	}
	
/*
	//TEM QUE VERIFICAR ESSA FUNÇÃO
	public Point[] getLimitIsland(List<MatOfPoint> blueContour)
	{
		Point[] listLimitIsland = new Point[frameWidth];
		
		for(int i = 0; i < listLimitIsland.length; i++)
		{
			listLimitIsland[i].x = i;
			listLimitIsland[i].y = 0;
		}
		
		Iterator<MatOfPoint> itr = blueContour.iterator();
		while(itr.hasNext())
		{
			MatOfPoint mp = (MatOfPoint) itr.next();
			Point[] pt = mp.toArray();
			for(int i = 0; i < pt.length; i++)
			{
				if(pt[i].y > listLimitIsland[(int)pt[i].x].y)
				{
					listLimitIsland[(int)pt[i].x].y = pt[i].y;
				}
			}
		}
		
		return listLimitIsland;		
	}
	//*/
	
	public void setRGBColor(Scalar[] rgbaColor, Scalar[] colorRadius, double[] minContourArea)
	{
		CBD.setRGBColor(rgbaColor, colorRadius, minContourArea);
	}
	
	public Scalar[] getColors()
	{
		return this.colors;
	}
	
	public Scalar[] getColorsRadius()
	{
		return this.colorsRadius;
	}
	
	public double[] getMinContourAreas()
	{
		return this.minContourAreas;
	}
	
	public synchronized List<Can> getListOfCans()
	{
		return listOfCans;
	}

	public synchronized int getBlueLimits()
	{
		return minBlue;
	}
}