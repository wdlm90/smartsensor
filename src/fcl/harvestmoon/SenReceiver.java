package fcl.harvestmoon;

import java.io.FileNotFoundException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SenReceiver extends BroadcastReceiver {

	private SenAccelerometer 	mSenAcc ;
	private DetectMode 			mDet = null ;
	private Predictor 			mPred = null ;
	private Context 			mCtx = SmartSensorActivity.getSensContext() ;
	
	public SenReceiver() 
	{
		mSenAcc = SenAccelerometer.getSenAccInstance() ;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if(intent.getAction().equals("fcl.harvestmoon.wakeup"))
		{
			mSenAcc.setbClose(false) ;
			mSenAcc.getmWake().acquire() ;
		}
		else if (intent.getAction().equals("fcl.harvestmoon.detect"))
		{
			System.out.println("Coming in Onreceive of senreceiver\n");
			try 
			{
				mDet = DetectMode.getDetectInstance () ;
				if (null != mDet)
				{
					mDet.ProcessInfo () ;
				}
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		else if (intent.getAction().equals("fcl.harvestmoon.predict"))
		{
			mPred = Predictor.getPredictInstance() ;
			if (null != mPred)
			{
				mPred.Predict() ;
			}
		}
		else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		{
			Bundle bundle = intent.getExtras(); 
			SmsMessage[] msgs = null;
			String msg_from = null; 
			
			if (bundle != null)
			{
				try
				{
					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];
					for(int i=0; i<msgs.length; i++)
					{
					    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					    msg_from = msgs[i].getOriginatingAddress();
					}
					
					String msgBody = msgs[0].getMessageBody();
					System.out.println("SMS is \n");
					System.out.println(msgBody);
					System.out.println("From " + msg_from);
					
					if (msgBody.contains("Transportation"))
					{
						((SmartSensorActivity) mCtx).setmFrom (msg_from) ;
						/*System.out.println("Inside if condition \n");
						Message msg = Message.obtain(null, SensorService.START_ACCEL_LOGGING, 0, 0);
					    try {
					    	Bundle b = new Bundle() ;
					    	b.putFloat("samplingrate", 50) ;
					    	b.putInt("wakeup", 0) ;
					    	//b.putInt ("From", Integer.parseInt(msg_from)) ;
					    	msg.setData(b) ;
					    	((SmartSensorActivity) mCtx).getmService().send(msg);
					    } catch (RemoteException e) {
					        e.printStackTrace();
					    }*/
					}
				}
				catch(Exception e)
				{
				}
			}	
		}
	}
}
