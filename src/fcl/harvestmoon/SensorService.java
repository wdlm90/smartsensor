package fcl.harvestmoon;

import java.io.FileNotFoundException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;


public class SensorService extends Service 
{
	static final int 				START_ACCEL_LOGGING = 1 ;
	static final int 				STOP_ACCEL_LOGGING = 2 ;
	static final int 				STOP_SERVICE = 3 ;
	
	private PowerManager.WakeLock 	mWake ;
	private NotificationManager 	mNotify = null ;
	private SenAccelerometer 		mAccel = null ;
	private DetectMode 				mDet = null ;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();	
		
		mNotify = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerManager");
		
		showNotification();		
	}

	@Override
	public void onDestroy()
	{	
		super.onDestroy();
		
		if (mWake.isHeld())
		{
			mWake.release() ;
		}
		if (null != mAccel)
		{
			mAccel.StopAccelerometer() ;
			mAccel = null ;
		}
		mNotify.cancel(1) ;
	}
	
	class SensorServiceHandler extends Handler 
	{
        @Override
        public void handleMessage(Message IncMsg) 
        {
            switch (IncMsg.what) 
            {
                case START_ACCEL_LOGGING:
                {
                	Bundle b = IncMsg.getData() ;
					try 
					{
						mDet = new DetectMode () ;
					} 
					catch (FileNotFoundException e) 
					{
						e.printStackTrace();
					} 
                	
                	int wakeUp = b.getInt("wakeup") ;
                	if (0 == wakeUp)
                	{
                		if (!mWake.isHeld())
                		{
                			mWake.acquire() ;
                		}
                	}
                	mAccel = new SenAccelerometer(SenAccelerometer.ACCELEROMETER_SENSOR, 
                				b.getFloat("samplingrate"), wakeUp);
                	mAccel.StartAccelerometer() ;
                }	      	
                break;
 
                case STOP_ACCEL_LOGGING:
                {
                   	if (null != mAccel)
                	{
                		mAccel.StopAccelerometer() ;
                		               		
                		mAccel = null ;
                		if (mWake.isHeld())
                		{
                			mWake.release() ;
                		}
                	}
                	if (null != mDet)
                	{
                		mDet.StopProcessing() ;
                	}
                }
                break ;
 
                case STOP_SERVICE :
                {
                	stopSelf() ;
                }
                break ;
                
                default:
                {
                	super.handleMessage(IncMsg);
                }
            }
        }
    }
	
	final Messenger mServiceMessenger = new Messenger(new SensorServiceHandler());
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return mServiceMessenger.getBinder() ;
	}
	
	private void showNotification() 
	{
        CharSequence text = getText(R.string.sensor_service);

        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
        		new Intent(this, SmartSensorActivity.class), 0);


        notification.setLatestEventInfo(this, "Local service", text, contentIntent);

        mNotify.notify(1, notification) ;
	}
}
