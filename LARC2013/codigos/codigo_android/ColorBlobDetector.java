package erus.android.erusbot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorBlobDetector
{
	private Scalar[] colorsRGB;
	// Lower and Upper bounds for range checking in HSV color space
	private Scalar[] mLowerBound;
	private Scalar[] mUpperBound;
	// Minimum contour area in percent for contours filtering
	private static double[] mMinContourArea;
	// Color radius for range checking in HSV color space
	private Scalar[] mColorRadius;	
	private List<MatOfPoint>[] mContours;
	
	private int numColors;
	
	@SuppressWarnings("unchecked")
	public ColorBlobDetector(int numColors)
	{
		this.numColors = numColors;
		
		colorsRGB = new Scalar[numColors];
		mLowerBound = new Scalar[numColors];
		mUpperBound = new Scalar[numColors];
		mMinContourArea = new double[numColors];
		mColorRadius = new Scalar[numColors];
		mContours = (List<MatOfPoint>[]) new List[numColors];
		
		for (int i = 0; i < numColors; i++)
		{
			colorsRGB[i] = new Scalar(0);
			mLowerBound[i] = new Scalar(0);
			mUpperBound[i] = new Scalar(0);
			mColorRadius[i] = new Scalar(25,50,50,0);
			mContours[i] = new ArrayList<MatOfPoint>();
			mMinContourArea[i] = 0.1;
		}		
	}
	
	private Scalar convertScalarRgbaToHsv(Scalar rgbaColor)
	{	
        Mat pointMatHsv = new Mat();
        Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC4, rgbaColor);
        Imgproc.cvtColor(pointMatRgba, pointMatHsv, Imgproc.COLOR_RGB2HSV_FULL, 4);
        
        return new Scalar(pointMatHsv.get(0, 0));
	}
	
	public void setRGBColor(Scalar[] rgbaColor, Scalar[] colorRadius, double[] minContourArea)
	{
		for (int i = 0; i < numColors; i++)
		{			
			this.colorsRGB[i] = rgbaColor[i];
			Scalar hsvColor = convertScalarRgbaToHsv(rgbaColor[i]); 
			mColorRadius[i] = colorRadius[i];			
			mMinContourArea[i] = minContourArea[i];
			
		    double minH = (hsvColor.val[0] >= mColorRadius[i].val[0]) ? hsvColor.val[0]-mColorRadius[i].val[0] : 0; 
		    double maxH = (hsvColor.val[0]+mColorRadius[i].val[0] <= 255) ? hsvColor.val[0]+mColorRadius[i].val[0] : 255;
	
	  		mLowerBound[i].val[0] = minH;
	   		mUpperBound[i].val[0] = maxH;
	
	  		mLowerBound[i].val[1] = hsvColor.val[1] - mColorRadius[i].val[1];
	   		mUpperBound[i].val[1] = hsvColor.val[1] + mColorRadius[i].val[1];
	
	  		mLowerBound[i].val[2] = hsvColor.val[2] - mColorRadius[i].val[2];
	   		mUpperBound[i].val[2] = hsvColor.val[2] + mColorRadius[i].val[2];
	
	   		mLowerBound[i].val[3] = 0;
	   		mUpperBound[i].val[3] = 255;
	   		
		}
	}
	
	public void process(Mat rgbaImage)
	{
		Mat hsvMat = new Mat();
  		Imgproc.cvtColor(rgbaImage, hsvMat, Imgproc.COLOR_RGB2HSV_FULL);
         	
      	for (int i = 0; i < numColors; i++)
      	{
      
    		Mat Mask = new Mat();
    		Core.inRange(hsvMat, mLowerBound[i], mUpperBound[i], Mask);
    		
    		Mat dilatedMask = new Mat();
    		Imgproc.dilate(Mask, dilatedMask, new Mat());
    		
    		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    		
    		Mat hierarchy = new Mat();
    		
    		Imgproc.findContours(dilatedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
    		
    		  // Find max contour area
            double maxArea = 0;
            Iterator<MatOfPoint> each = contours.iterator();
            while (each.hasNext())
            {
            	MatOfPoint wrapper = each.next();
            	double area = Imgproc.contourArea(wrapper);
            	if (area > maxArea)
            		maxArea = area;
            }
            
            // Filter contours by area
            mContours[i].clear();
            each = contours.iterator();
            while (each.hasNext())
            {
            	MatOfPoint contour = each.next();
            	if (Imgproc.contourArea(contour) > mMinContourArea[i]*maxArea)
            	{
            		mContours[i].add(contour);
            	}
            }
    	}    
	}

	public Scalar[] getRGBColors()
	{
		return this.colorsRGB;
	}
	
	public int getNumColors()
	{
		return this.numColors;
	}
	
	public List<MatOfPoint>[] getContours()
	{
		return mContours;
	}

}