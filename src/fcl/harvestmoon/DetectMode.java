package fcl.harvestmoon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;



public class DetectMode {
	static final int 			DETECT_MODE 		= 10 ;
	static final int 			ENERGY_ARRAY_SIZE 	= 10 ;
	static final int 			UPPER_THRESHOLD_VALUE = 12 ;
	
	private AlarmManager 		am ;
	private Intent	 			myAlarm ;
	private Context 			mCtx = SmartSensorActivity.getSensContext() ;
	private PendingIntent 		pSender ;
	public static DetectMode 	mDetect ;
	private Scanner 			s = null;
	private EnergyArray[] 		mEnergyArray ; 
	private double  			dEnergyAvg ;
	private int 				iEnergyIndex ;
	private Predictor 			mPredictor ;
	private int 				iThreshCross = 0 ; 
	
	class EnergyArray 
	{
		public EnergyArray()
		{
			dEnergyElement = 0 ;
			bRead = false ;
		}
		
		public double 		dEnergyElement ;
		public boolean 		bRead ;			
	};
	
	public double getAvgEnergy ()
	{
		return dEnergyAvg ;
	}
	
	public int getThresholdCrossings()
	{
		return iThreshCross ;
	}
	
	public void resetThresholdCross ()
	{
		iThreshCross = 0 ; 
	}
	
	public EnergyArray getEnergyArrayElement (int iIndex)
	{
		return mEnergyArray[iIndex] ;
	}
	
	public void setEnergyRead (int iIndex)
	{
		mEnergyArray[iIndex].bRead = true ;
	}
	
	public void deleteEnergyElement (int iIndex)
	{
		mEnergyArray[iIndex].dEnergyElement = 0 ;
		mEnergyArray[iIndex].bRead = false ;
	}
	
	
	public DetectMode() throws FileNotFoundException 
	{
		mDetect = this ;

		myAlarm = new Intent(mCtx, SenReceiver.class) ;
		myAlarm.setAction("fcl.harvestmoon.detect") ;
				
		pSender = PendingIntent.getBroadcast(mCtx.getApplicationContext(), 1235, myAlarm, 1) ;
		long fTime = System.currentTimeMillis() + 1000 * DETECT_MODE ;
		am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, fTime, 1000*DETECT_MODE, pSender);
        
        mEnergyArray = new EnergyArray [ENERGY_ARRAY_SIZE] ;
        
        for (int i = 0; i < ENERGY_ARRAY_SIZE; i++)
        {
        	mEnergyArray[i] = new EnergyArray () ;
        }	
        iEnergyIndex = 0 ;
        
        try 
        {
			s = new Scanner(new BufferedReader(new FileReader("/mnt/sdcard/sensor/sensor.txt")));
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
       
        mPredictor = new Predictor() ;
        
	}
	
	static public DetectMode getDetectInstance()
	{
		return mDetect;
	}
	
	
	public void ProcessInfo() throws FileNotFoundException
	{
		System.out.println("Inside Process Info\n");
	    int iIndex = 0 ;
	    double[] a = new double[500]  ;
	    double mean = 0 ;
	    double energy = 0 ;
	    boolean bProcess = false ;
	    
	    while ((500 > iIndex) && ((null != s) && (s.hasNext()))) 
	    {
	    	bProcess = true ;
        	String temp = new String (s.next());
            int i = temp.lastIndexOf("@") + 1;
            String t = temp.substring(i) ;
            a[iIndex] = Double.parseDouble(t) ;
            if (a[iIndex] > UPPER_THRESHOLD_VALUE)
            {
            	iThreshCross++ ;
            }
            iIndex++ ;
        }
	    
	    if (true == bProcess)
	    {
	    	mPredictor.setbProcess (true) ;
	    	
		    mean = calMean (a) ;
		    
		    energy = calEnergy (a, mean) ;
		    
		    if ((ENERGY_ARRAY_SIZE-1) == iEnergyIndex)
		    {
		    	int iLoop = 0 ;
		    	while (iLoop < ENERGY_ARRAY_SIZE)
		    	{
			    	if (mEnergyArray[iLoop].bRead)
			    	{
			    		deleteEnergyElement(iLoop) ;
			    	}
			    	iLoop++ ;
		    	}
		    	iEnergyIndex = 0 ;
		    }
		    
		    if (false == mEnergyArray[iEnergyIndex].bRead)
		    {
		    	System.out.println("iEnergyindex is " + iEnergyIndex);
		    	mEnergyArray[iEnergyIndex].dEnergyElement = energy ;
		    	iEnergyIndex++ ;
		    }
	    }
    
	}
	
	public double calEnergy (double[] a, double mean)
	{
		double energy = 0 ; 
		int iIndex = 0 ;
		double dTemp = 0 ; 
		
		while (500 > iIndex)
		{
			dTemp = a[iIndex] - mean ;
			energy += (dTemp * dTemp) ;
			iIndex++ ;
		}
		
		System.out.println("Energy is " + energy);
		
		return energy ;
	}
	
	public double calMean (double[] a)
	{
		int iIndex = 0 ;
		double mean = 0 ;
		
		while (500 > iIndex)
		{
			mean += a[iIndex] ;
			iIndex++ ;
		}
		
		mean = mean / 500 ;
		
		System.out.println("Mean is " + mean);
		
		return mean ; 
	}
	
	
	public void StopProcessing()
	{
		am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pSender) ;
		
		mPredictor.StopProcessing () ;
	}
}
