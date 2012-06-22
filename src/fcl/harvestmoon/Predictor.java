package fcl.harvestmoon;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class Predictor {
	
	static final int 		BUS_LOWER_THRESHOLD = 140 ;
	static final int 		BUS_UPPER_THRESHOLD = 800 ;
	static final int 		MRT_LOWER_THRESHOLD = 30 ;
	static final int 		MRT_UPPER_THRESHOLD = 80 ;
	static final int 		WALK_LOWER_THRESHOLD = 2000 ;
	static final int 		BUS_ADJUSTEMENT_VALUE = 20 ;
	static final int 		MRT_ADJUSTEMENT_VALUE = 10 ;
	
	static final int 		PREDICT_MODE = 10 ;
	
	static final int 		STATE_INITIALIZED = 1 ;
	static final int 		STATE_PROB_BUS_1 = 2 ;
	static final int 		STATE_PROB_BUS_2 = 3 ; 
	static final int 		STATE_PROB_MRT_1 = 4 ;
	static final int 		STATE_PROB_MRT_2 = 5 ;
	static final int 		STATE_WALK = 6 ; 
	static final int 		STATE_STATIONARY = 7 ; 
	static final int 		STATE_BUS_CONFIRMED = 8 ;
	static final int 		STATE_MRT_CONFIRMED = 9 ; 
	
	private AlarmManager 	am ;
	private Intent	 		myAlarm ;
	
	private Context 		mCtx = SmartSensorActivity.getSensContext() ;
	private PendingIntent 	pSender ;
	
	public static Predictor mPredict ;
	private DetectMode 		mDet ;
	private int 			iState ;
	private double  		dEnergyAvg ;
	private int 			iSampleNo ;
	private boolean 		bProcess ;
	
	
	static public Predictor getPredictInstance()
	{
		return mPredict;
	}
	
	public Predictor() 
	{
		mPredict = this ;
		mDet = DetectMode.getDetectInstance() ;
		iSampleNo = 0 ;
		dEnergyAvg = 0.0 ;
		iState = STATE_INITIALIZED ;
		
		myAlarm = new Intent(mCtx, SenReceiver.class) ;
		myAlarm.setAction("fcl.harvestmoon.predict") ;
				
		pSender = PendingIntent.getBroadcast(mCtx.getApplicationContext(), 1236, myAlarm, 1) ;
		long fTime = System.currentTimeMillis() + 1000 * PREDICT_MODE ;
		am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, fTime, 1000*PREDICT_MODE, pSender);
	}

	public boolean isbProcess() 
	{
		return bProcess;
	}

	public void setbProcess(boolean bProcess) 
	{
		this.bProcess = bProcess;
	}
	
	public void Predict ()
	{
		if (true == bProcess)
		{
			DetectMode.EnergyArray energy = mDet.getEnergyArrayElement(iSampleNo % 10) ;
			System.out.println("Inside Predict function avg energy is \n" + dEnergyAvg + "and energy is " + energy.dEnergyElement);
			
			dEnergyAvg *= iSampleNo ;
			dEnergyAvg += energy.dEnergyElement ;
			mDet.setEnergyRead(iSampleNo % 10) ;
			iSampleNo++ ;
			dEnergyAvg /= iSampleNo ;
				
			if ((((BUS_LOWER_THRESHOLD - BUS_ADJUSTEMENT_VALUE) < dEnergyAvg) || (BUS_LOWER_THRESHOLD < dEnergyAvg)) && (BUS_UPPER_THRESHOLD > dEnergyAvg))
			{
				System.out.println("In BUS section iState is \n" + iState);
				if (iState == STATE_INITIALIZED) 
				{
					iState = STATE_PROB_BUS_1 ;
				}
				else if (iState == STATE_PROB_BUS_1)
				{
					iState = STATE_PROB_BUS_2 ;
				}
				else if (iState == STATE_PROB_BUS_2)
				{
					System.out.println("$$$$$$$$$$$$$$$$$$$$$   BUS CONFIRMED   $$$$$$$$$$$$$$$$$$$$$\n");
					iState = STATE_BUS_CONFIRMED ;
					SmartSensorActivity.getImageView().setVisibility(View.VISIBLE) ;
					SmartSensorActivity.getImageView().setImageResource(R.drawable.bus) ;
					((SmartSensorActivity) mCtx).sendSms("BUS", true) ;
				}
				else if (STATE_BUS_CONFIRMED != iState)
				{
					iState = STATE_INITIALIZED ;
				}
			}
			else if ((((MRT_LOWER_THRESHOLD - MRT_ADJUSTEMENT_VALUE) < dEnergyAvg) || (MRT_LOWER_THRESHOLD < dEnergyAvg)) && (MRT_UPPER_THRESHOLD > dEnergyAvg)) 
			{
				System.out.println("In MRT section iState is \n" + iState);
				if (iState == STATE_INITIALIZED) 
				{
					iState = STATE_PROB_MRT_1 ;
				}
				else if (iState == STATE_PROB_MRT_1)
				{
					iState = STATE_PROB_MRT_2 ;
				}
				else if (iState == STATE_PROB_MRT_2)
				{
					System.out.println("$$$$$$$$$$$$$$$$$$$$$   MRT CONFIRMED   $$$$$$$$$$$$$$$$$$$$$\n");
					iState = STATE_MRT_CONFIRMED ;
					SmartSensorActivity.getImageView().setVisibility(View.VISIBLE) ;
					SmartSensorActivity.getImageView().setImageResource(R.drawable.mrt) ;
					((SmartSensorActivity) mCtx).sendSms("MRT", true) ;
				}
				else if (STATE_MRT_CONFIRMED != iState)
				{
					iState = STATE_INITIALIZED ;
				}				
			}
			else if (WALK_LOWER_THRESHOLD < dEnergyAvg)
			{
				System.out.println("In WALK section  \n");
				if (iState == STATE_WALK)
				{
					iState = STATE_INITIALIZED ;
				}
				else
				{
					iState = STATE_WALK ;
					Toast.makeText(mCtx, "WALKING", Toast.LENGTH_SHORT).show() ;
					SmartSensorActivity.getImageView().setVisibility(View.VISIBLE) ;
					SmartSensorActivity.getImageView().setImageResource(R.drawable.walk) ;
					((SmartSensorActivity) mCtx).sendSms("WALK", true) ;
				}	
			}
			bProcess = false ;
		}
	}
	
	public void StopProcessing()
	{
		am = (AlarmManager)mCtx.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pSender) ;
	}
}
