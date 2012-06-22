package fcl.harvestmoon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class FileHandler extends Handler /*implements Runnable*/ {
	static final int 			FILE_OPEN = 1 ;
	static final int 			FILE_CLOSE = 2 ;
	static final int 			FILE_WRITE = 3 ;
	
	private Bundle 				mData ;
	private float 				mLinearVal ;
	public FileOutputStream 	fStream ;
	
	public FileHandler(Bundle data)
	{
		mData = data ;
	}
	
	public FileHandler()
	{
		
	}
	
	public void handleMessage(Message msg) 
	{
        switch (msg.what) 
        {
	        case FILE_OPEN :
	        {
	        	//long now = System.currentTimeMillis();
	    		//SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss-SSS");
	    		//String time = simple.format(new Date(now));
	    		File sensor = new File ("/mnt/sdcard/sensor/") ;
	    		if (false == sensor.exists())
	    		{
	    			sensor.mkdir() ;
	    		}
	    		try
	    		{
	    			File SenFile = new File ("/mnt/sdcard/sensor/sensor.txt") ;
	    			try 
	    			{
	    				SenFile.createNewFile();
	    			} 
	    			catch (IOException e) 
	    			{
	    				e.printStackTrace() ;
	    			} 
	    			fStream = new FileOutputStream(SenFile) ;
	    			
	    		} 
	    		catch (Exception e)
	    		{
	    			e.printStackTrace() ;
	    		}
	        }		  
	        break ;
	        
	        case FILE_CLOSE :
	        {
	        	try 
	        	{
	        		fStream.close() ;
				} 
	        	catch (IOException e) 
	        	{
					e.printStackTrace();
				}
	        }
	        break ;
	        
	        case FILE_WRITE :
	        {	
	          	mData = msg.getData() ;
	        	float x = mData.getFloat("x") ;
	        	float y = mData.getFloat("y") ;
	        	float z = mData.getFloat("z") ;
	        	long t = mData.getLong("time") ; 
	        	
	        	SimpleDateFormat simple1 = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss-SSS");
	    		String time1 = simple1.format(new Date(t));
	    		
	        	mLinearVal = (float) Math.sqrt((x * x) + (y * y) + (z * z)) ;
	        	
	        	String temp = time1 +  String.format("@%f\n", mLinearVal) ;
	        	try 
	        	{
	        		fStream.write(temp.getBytes()) ;
				} 
	        	catch (IOException e) 
	        	{
					e.printStackTrace();
				}
	        }	
	        break ;
        }
	}
}

