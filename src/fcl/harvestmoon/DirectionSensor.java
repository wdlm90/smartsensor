package fcl.harvestmoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class DirectionSensor implements SensorEventListener{
	
	private Context 			mCtx = SmartSensorActivity.getSensContext() ;
	private SensorManager 		mSensorManager;
	private Sensor 				mOrient;
	private FileOutputStream 	DirectStream ;
	private File 				DirectFile ;
	
	
	public DirectionSensor() 
	{
		mSensorManager = (SensorManager)mCtx.getSystemService(Context.SENSOR_SERVICE) ;
		mOrient = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		DirectFile = new File ("/mnt/sdcard/sensor/" + "Direction.txt") ;
		if (false == DirectFile.exists())
		{
			try 
			{
				DirectFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace() ;
			}
		}
		
		try 
		{
			DirectStream = new FileOutputStream(DirectFile, true);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void Start()
	{
		mSensorManager.registerListener(this, mOrient, SensorManager.SENSOR_DELAY_NORMAL) ; //Suitable for orientation changes.
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		long time = System.currentTimeMillis() ;
		SimpleDateFormat simple1 = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss-SSS");
		String time1 = simple1.format(new Date(time));
		String temp = new String(time1) ;
		
		temp += "@" + event.values[0] + '$';
		
		if (((315 <= event.values[0]) && (360 >= event.values[0])) || 
				((0 <= event.values[0]) && (45 > event.values[0])))
		{
			temp += " North \n" ;
		}
		else if ((45 <= event.values[0]) && (135 > event.values[0]))
		{
			temp += " East \n" ;
		}
		else if ((135 <= event.values[0]) && (225 > event.values[0]))
		{
			temp += " South \n" ;
		}
		else if ((225 <= event.values[0]) && (315 > event.values[0]))
		{
			temp += " West \n" ;
		}
		
		try 
		{
			DirectStream.write(temp.getBytes()) ;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void Stop ()
	{
		mSensorManager.unregisterListener(this, mOrient) ;
		try 
		{
			DirectStream.close() ;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
