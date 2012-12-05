package erus.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.opencv.core.Scalar;

import android.os.Environment;
import android.util.Log;


public class ColorCalibrator
{
	private static final String TAG = "ColorCalibrator";
	
	public static final int NUM_COLORS = 4;
	
	private static final String COLOR_FILE = "colorFile.txt";
	
	private Scalar[] colors;
	private Scalar[] colorsRadius;
	private double[] minContourAreas;
	
	public ColorCalibrator()
	{
		colors = new Scalar[NUM_COLORS];
		colorsRadius = new Scalar[NUM_COLORS];
		minContourAreas = new double[NUM_COLORS];
		
		for(int i=0; i<NUM_COLORS; i++)
		{
			colors[i] = new Scalar(225,0,0,0);
			colorsRadius[i] = new Scalar(25,50,50,0);
			minContourAreas[i] = 0.1;
		}
	}
	
	public void readCalibrationFile()
	{
		try
		{
			File sdcard = Environment.getExternalStorageDirectory();
			File colorFile = new File(sdcard,COLOR_FILE);
			FileInputStream fstream = new FileInputStream(colorFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			for(int i=0; i<NUM_COLORS; i++)
			{
						
				for(int j=0; j<4; j++)
				{
					colors[i].val[j] = Double.parseDouble(br.readLine());
				}
				for(int j=0; j<4; j++)
				{					
					colorsRadius[i].val[j] = Double.parseDouble(br.readLine());
				}				
				minContourAreas[i] = Double.parseDouble(br.readLine()); 
			}				
						
			br.close();			
		}
		catch (Exception e)
		{			
			Log.i(TAG, "Error reading File. " + e.getMessage());		    	
		}
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
	
	public int getNumColors()
	{
		return NUM_COLORS;
	}
	
	public void writeCalibrationFile(Scalar[] colors, Scalar[] colorsRadius, double[] minContourAreas)
	{
		try
		{	
			File sdcard = Environment.getExternalStorageDirectory();
			File colorFile = new File(sdcard,COLOR_FILE);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(colorFile)));
			
			for(int i=0; i<NUM_COLORS; i++)
			{						
				for(int j=0; j<4; j++)
				{
					out.println(colors[i].val[j]);
				}
				for(int j=0; j<4; j++)
				{					
					out.println(colorsRadius[i].val[j]);
				}				
				out.println(minContourAreas[i]); 
			}				
			
			out.close();
			
		}
		catch(Exception e)
		{
			Log.i(TAG, "Error writing File. " + e.getMessage());
		}
	}
}