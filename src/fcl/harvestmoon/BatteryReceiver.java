package fcl.harvestmoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver{
	
	private FileOutputStream 	batStream ;
	private File				BatFile ;
	
	public BatteryReceiver()
	{
		File sensor = new File ("/mnt/sdcard/sensor/") ;
		if (false == sensor.exists())
		{
			sensor.mkdir() ;
		}
		BatFile = new File ("/mnt/sdcard/sensor/" + "battery.txt") ;
		if (false == BatFile.exists())
		{
			try 
			{
				BatFile.createNewFile();
			} 
			catch (IOException e) 
			{
				e.printStackTrace() ;
			}
		}
		
		try
		{
			batStream = new FileOutputStream(BatFile, true);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onReceive(Context arg0, Intent arg1) 
	{
		if (arg1.getAction().equals("android.intent.action.BATTERY_CHANGED"))
		{
			SmartSensorActivity.getSensContext().unregisterReceiver(this) ;
			
			int rawlevel = arg1.getIntExtra("level", -1);
			int scale = arg1.getIntExtra("scale", -1);
			int level = -1;
			
			if (rawlevel >= 0 && scale > 0) 
			{
				level = (rawlevel * 100) / scale;
			}
			
			long time = System.currentTimeMillis() ;
			SimpleDateFormat simple1 = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss-SSS");
    		String time1 = simple1.format(new Date(time));
    		
			try 
			{
				batStream.write(String.format("%s Battery level is %d \n", time1, level).getBytes()) ;
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			try 
			{
				batStream.close() ;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}	
	}
}
