package fcl.harvestmoon;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;


public class SenAccelerometer /*extends BroadcastReceiver*/ implements SensorEventListener {

	static final int 				ACCELEROMETER_SENSOR = 1 ;
	private int 					iType = -1 ;
	public float 					fSamRate = 1 ;
	private Context					mCtx = SmartSensorActivity.getSensContext() ;
	private SensorManager 			mSensorManager;
	private Sensor 					mAccelerometer;
	private FileHandler 			mFile = null ;
	public long 					lastTime = 0 ; 
	private PowerManager.WakeLock 	mWake ;
	private int 					mSamCount = 0 ;
	private int 					mCurCount = 0 ;
	private boolean 				bClose = false ;
	public static SenAccelerometer 	mSen ;
	private AlarmManager 			am ;
	private PendingIntent 			pSender ;
	private Intent 					myAlarm ;
	private int 					mWakeUp = 0 ;
	private DirectionSensor 		mDirection ;
	//private int 					mFrom ;
	
	static public SenAccelerometer getSenAccInstance()
	{
		return mSen;
	}

	public SenAccelerometer(int type, float fSamplingRate, int wakeUp/*, int iFrom*/) 
	{
		mSen = this ;
		iType = type ;
		mWakeUp = wakeUp ;
		fSamRate = fSamplingRate ;
		mSensorManager = (SensorManager)mCtx.getSystemService(Context.SENSOR_SERVICE) ;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mFile = new FileHandler() ;	
		Message msg = Message.obtain(null, FileHandler.FILE_OPEN, 0, 0) ;
		mFile.sendMessage(msg) ;
		mSamCount = (int) (fSamRate * 10) ;
		mDirection = new DirectionSensor() ;
		
		PowerManager pm = (PowerManager)mCtx.getSystemService(Context.POWER_SERVICE);
		mWake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerManager");
	}

	public void StartAccelerometer() 
	{
		switch (iType)
		{
			case SenAccelerometer.ACCELEROMETER_SENSOR :
			{
				System.out.println("StartAccelerometer");
				lastTime = System.currentTimeMillis() ;
				mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST) ;
				mDirection.Start() ;
			}	
			break ;
			
			default :
				System.out.println("Oops this shouldnt have come here");
			break ;
		}
		
		if (0 != mWakeUp)
		{
			Calendar cal = Calendar.getInstance() ;
			cal.add(Calendar.SECOND, 30) ;
			
			myAlarm = new Intent(mCtx, SenReceiver.class) ;
			myAlarm.setAction("fcl.harvestmoon.wakeup") ;
						
			pSender = PendingIntent.getBroadcast(mCtx.getApplicationContext(), 
					1234, myAlarm, 0) ;
			long fTime = System.currentTimeMillis() + 1000;
			
	        am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
	        am.setRepeating(AlarmManager.RTC_WAKEUP,
	                        fTime, 60*1000*mWakeUp, pSender);
		}
	}
	
	public void StopAccelerometer ()
	{
		mSensorManager.unregisterListener(this, mAccelerometer) ;
		mAccelerometer = null ;
		Message msg = Message.obtain(null, FileHandler.FILE_CLOSE, 0, 0) ;
		mFile.sendMessage(msg) ;
		
		if (0 != mWakeUp)
		{
			myAlarm = new Intent("fcl.harvestmoon.wakeup") ;
			
			pSender = PendingIntent.getBroadcast(mCtx.getApplicationContext(), 
					1234, myAlarm, 0) ;
			am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
			am.cancel(pSender) ;
		}		
		mDirection.Stop() ;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if ((false == bClose) && (mCurCount < mSamCount)) 
		{
			long now = System.currentTimeMillis() ;
			if (50 != fSamRate)
			{
				if ((1/fSamRate) <= (now - lastTime)/1000.0)
				{
					mCurCount = (mWakeUp == 0) ? 0 : ++mCurCount ;
					lastTime = now ;
					Message msg = Message.obtain(null, FileHandler.FILE_WRITE, 0, 0) ;
					Bundle data = new Bundle() ;
					data.putFloat("x", event.values[0]) ;
					data.putFloat("y", event.values[1]) ;
					data.putFloat("z", event.values[2]) ;
					data.putLong("time", System.currentTimeMillis()) ;
					msg.setData(data) ;
					
					mFile.sendMessage(msg) ;
				}
			}
			else
			{
				Message msg = Message.obtain(null, FileHandler.FILE_WRITE, 0, 0) ;
				Bundle data = new Bundle() ;
				data.putFloat("x", event.values[0]) ;
				data.putFloat("y", event.values[1]) ;
				data.putFloat("z", event.values[2]) ;
				data.putLong("time", System.currentTimeMillis()) ;
				msg.setData(data) ;
				
				mFile.sendMessage(msg) ;
			}
		}
		else
		{
			bClose = true ;
			mCurCount = 0 ;
			if (mWake.isHeld())
			{
				mWake.release() ;
			}
		}
	}

	public PowerManager.WakeLock getmWake() 
	{
		return mWake;
	}

	public boolean isbClose() 
	{
		return bClose;
	}


	public void setbClose(boolean bClose) 
	{
		this.bClose = bClose;
	}	
}
