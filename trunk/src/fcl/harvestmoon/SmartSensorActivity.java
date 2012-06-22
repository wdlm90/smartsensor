package fcl.harvestmoon;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class SmartSensorActivity extends Activity implements OnClickListener, ServiceConnection{
    
	/** Called when the activity is first created. */
    private static final String 	SMS_DELIVERED = "SMS_DELIVERED";
    private static final String 	SMS_SENT = "SMS_SENT";
    public static final String 		SEN_PREFS = "SenPrefs";
	
    private EditText			 	editSampling = null ;
	private EditText 				editWakeUp = null ;
	private ToggleButton 			toggleStart = null ;
	private Messenger 				mService = null;
	private ServiceConnection 		mConnect  = null ;
	static public Context 			mContext = null ;
	private boolean 				bSensorOn = false ;
	private int 					mWakeUp = 0 ;
	private float 					mSamRate = 0 ;
	public static ImageView 		mImage = null ;
	private TextView 				t1, t2 ;
	private String 					mFrom = null ;
	private CheckBox 				c1 ;
	private boolean 				mSMS = false ;
	
	
	public String getmFrom() 
	{
		return mFrom;
	}

	public void setmFrom(String mFrom) 
	{
		this.mFrom = new String (mFrom) ;
	}
	     
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        mContext = this ;
        editSampling = (EditText) findViewById(R.id.widget34) ;
        editWakeUp = (EditText) findViewById(R.id.editText1) ;
        
        toggleStart = (ToggleButton) findViewById(R.id.toggleButton1) ;
        toggleStart.setOnClickListener(this) ;
    
        t1 = (TextView) findViewById(R.id.textView1) ;
        t1.setTextColor(Color.BLACK) ;
        t2 = (TextView) findViewById(R.id.widget32) ;
        t2.setTextColor(Color.BLACK) ;
        
        mImage = (ImageView) findViewById(R.id.imageView1) ;
        mImage.setVisibility(View.GONE) ;
        mImage.setScaleType(ImageView.ScaleType.FIT_CENTER) ;
        
		c1 = (CheckBox) findViewById(R.id.checkBox1) ;
		c1.setText("SMS") ;
			
		registerReceiver(sendreceiver, new IntentFilter(SMS_SENT));
		registerReceiver(deliveredreceiver, new IntentFilter(SMS_DELIVERED));
		registerReceiver(smsreceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }
    
    public Messenger getmService() 
    {
		return mService;
	}

    public static ImageView getImageView () 
    {
    	return mImage ;
    }
    
    public static Context getSensContext ()
    {
    	return mContext ;
    }
    
	@Override
	public void onClick(View v) 
	{
		if (v.equals(toggleStart))
		{
			if (false == bSensorOn)
			{ 
				bSensorOn = true ;
				mSMS = c1.isChecked() ;
				Editable text = null ; 
				text = editSampling.getText() ;
				float temp = 0 ;
				if (null != text)
				{ 
					temp =	Float.parseFloat(text.toString()) ;
				}
				temp = (temp > 0) ? temp : 1 ;
				
				mSamRate = temp ;
				text = editWakeUp.getText() ;
				 
				mWakeUp = 0 ;
				registerReceiver(new BatteryReceiver(), 
						new IntentFilter("android.intent.action.BATTERY_CHANGED")) ;
				
				Message msg = Message.obtain(null, SensorService.START_ACCEL_LOGGING, 0, 0);
		        try 
		        {
		        	Bundle b = new Bundle() ;
		        	b.putFloat("samplingrate", temp) ;
		        	b.putInt("wakeup", mWakeUp) ;
		        	msg.setData(b) ;
		            mService.send(msg);
		        } 
		        catch (RemoteException e) 
		        {
		            e.printStackTrace();
		        }
			}
			else 
			{
				bSensorOn = false ;
				Message msg = Message.obtain(null, SensorService.STOP_ACCEL_LOGGING, 0, 0);
		        try 
		        {
		            mService.send(msg);
		        } 
		        catch (RemoteException e) 
		        {
		            e.printStackTrace();
		        }
		        this.registerReceiver(new BatteryReceiver(), 
						new IntentFilter("android.intent.action.BATTERY_CHANGED")) ;
			}
		}
	}
	
    public void onServiceConnected(ComponentName className, IBinder service) 
    {
    	mService = new Messenger(service);
    }

    public void onServiceDisconnected(ComponentName className) 
    {
        mService = null;
    }
	 
	@Override
	protected void onStart() 
	{
		super.onStart();
		mConnect = SmartSensorActivity.this ;
		Intent SerIntent = new Intent(this, SensorService.class) ;
		startService(SerIntent) ;
		bindService(SerIntent, mConnect, Context.BIND_AUTO_CREATE) ;
	}
	 
	@Override
	protected void onStop() 
	{
		super.onStop();
		if ((null != mService) && (null != mConnect))
		{
			unbindService(mConnect) ;
			mConnect = null ;
		}
		mSMS = c1.isChecked() ;
		
		SharedPreferences settings = getSharedPreferences(SEN_PREFS, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean("sensOn", bSensorOn);
	    editor.putFloat("samplingrate", mSamRate) ;
	    editor.putBoolean("SMS", mSMS) ;
	    editor.putInt("wakeup", mWakeUp) ;
	    editor.commit();
	}

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(sendreceiver);
	    unregisterReceiver(deliveredreceiver);
	    unregisterReceiver(smsreceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() 
	{
		SharedPreferences settings = getSharedPreferences(SEN_PREFS, 0);
	    bSensorOn = settings.getBoolean("sensOn", false) ;
	    if (true == bSensorOn)
	    {
	    	toggleStart.setChecked(true) ;
	    }
	    mSMS = settings.getBoolean("SMS", false) ;
	    if (mSMS)
	    {
	    	c1.setChecked(true) ;
	    }
	    mSamRate = settings.getFloat("samplingrate", 50) ;
	    editSampling.setText(String.format("%f", mSamRate)) ;
	    mWakeUp = settings.getInt("wakeup", 0) ;
	    editWakeUp.setText(String.format("%d", mWakeUp)) ;
	    
		super.onResume();
	}
	
		
	public void sendSms(String message, boolean isBinary)
	{
		if ( mSMS  && (null != mFrom))
		{
			SmsManager manager = SmsManager.getDefault();
	
		    PendingIntent piSend = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
		    PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);
	
		    if(isBinary)
		    {
		    	byte[] data = new byte[message.length()];
	
				for(int index=0; index<message.length() && index < 160 ; ++index)
				{
				        data[index] = (byte)message.charAt(index);
				}
	
				manager.sendTextMessage(mFrom, null, message, piSend, piDelivered);
		    }
		}
	}
	
	private BroadcastReceiver sendreceiver = new BroadcastReceiver()
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
	        String info = "Send information: ";
	        switch(getResultCode())
	        {
	                case Activity.RESULT_OK: info += "send successful"; break;
	                case SmsManager.RESULT_ERROR_GENERIC_FAILURE: info += "send failed, generic failure"; break;
	                case SmsManager.RESULT_ERROR_NO_SERVICE: info += "send failed, no service"; break;
	                case SmsManager.RESULT_ERROR_NULL_PDU: info += "send failed, null pdu"; break;
	                case SmsManager.RESULT_ERROR_RADIO_OFF: info += "send failed, radio is off"; break;
	        }   
	     }
	};
	
	private BroadcastReceiver deliveredreceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
		    String info = "Delivery information: ";
		    
		    switch(getResultCode())
		    {
		            case Activity.RESULT_OK: info += "delivered"; break;
		            case Activity.RESULT_CANCELED: info += "not delivered"; break;
		    }
		}
	};
    
	private BroadcastReceiver smsreceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();        
			SmsMessage[] msgs = null;
			
			if(null != bundle)
			{
		        String info = "Text SMS from ";
			    Object[] pdus = (Object[]) bundle.get("pdus");
			    msgs = new SmsMessage[pdus.length];
			    
			    for (int i=0; i<msgs.length; i++)
			    {
			        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
			        info += msgs[i].getOriginatingAddress();                     
			        info += "\n*****TEXT MESSAGE*****\n";
			        info += msgs[i].getMessageBody().toString();
			    }
			}                         
		}
	};
}