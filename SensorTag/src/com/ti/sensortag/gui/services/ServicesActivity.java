/**************************************************************************************************
  Filename:       ServicesActivity.java
  Revised:        $Date: 2013-08-30 11:44:31 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27454 $

  Copyright 2013 Texas Instruments Incorporated. All rights reserved.
 
  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. 
  The License limits your use, and you acknowledge, that the Software may not be 
  modified, copied or distributed unless used solely and exclusively in conjunction 
  with a Texas Instruments Bluetooth� device. Other than for the foregoing purpose, 
  you may not use, reproduce, copy, prepare derivative works of, modify, distribute, 
  perform, display or sell this Software and/or its documentation for any purpose.
 
  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED �AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 
  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com

 **************************************************************************************************/
package com.ti.sensortag.gui.services;

import static com.ti.sensortag.R.drawable.buttonsoffoff;
import static com.ti.sensortag.R.drawable.buttonsoffon;
import static com.ti.sensortag.R.drawable.buttonsonoff;
import static com.ti.sensortag.R.drawable.buttonsonon;
import static com.ti.sensortag.models.Devices.LOST_DEVICE_;
import static com.ti.sensortag.models.Devices.NEW_DEVICE_;
import static com.ti.sensortag.models.Devices.State.CONNECTED;
import static com.ti.sensortag.models.Measurements.PROPERTY_ACCELEROMETER;
import static com.ti.sensortag.models.Measurements.PROPERTY_AMBIENT_TEMPERATURE;
import static com.ti.sensortag.models.Measurements.PROPERTY_GYROSCOPE;
import static com.ti.sensortag.models.Measurements.PROPERTY_HUMIDITY;
import static com.ti.sensortag.models.Measurements.PROPERTY_IR_TEMPERATURE;
import static com.ti.sensortag.models.Measurements.PROPERTY_MAGNETOMETER;
import static com.ti.sensortag.models.Measurements.PROPERTY_SIMPLE_KEYS;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;

import com.ti.sensortag.R;
import com.ti.sensortag.models.Devices;
import com.ti.sensortag.models.Measurements;
import com.ti.sensortag.models.Point3D;
import com.ti.sensortag.models.SimpleKeysStatus;

public class ServicesActivity extends Activity implements PropertyChangeListener {

  private static final Measurements model = Measurements.INSTANCE;
  private static final char DEGREE_SYM = '\u2103';
  
  //for signal strength..
  TelephonyManager        Tel;
  MyPhoneStateListener    MyListener;
  //ends
  
  // Added by me for testing
  long interval=48, i=0; // Specifies how many readings are grouped per sms.
  int count =1, choice = 0, rss;
  float max = 0 , min = 42, average = 0 ,adder = 0;
  FileWriter tempfwriter;
  String patient = "Rajesh";
  //ARV
  public PowerManager pm;
  public PowerManager.WakeLock wakelockk;
  String test_1;
  //Button sendbutton;

  DecimalFormat decimal = new DecimalFormat("+0.00;-0.00");

  volatile boolean b = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    
    pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    wakelockk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
    setContentView(R.layout.services_browser);
    
    
    //for checking signal strength..
    MyListener   = new MyPhoneStateListener();
    Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
    Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    //ends..
    
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    test_1 = getIntent().getStringExtra("TEST");
    Toast.makeText(getBaseContext(), "ARV in services SA :"+test_1, Toast.LENGTH_SHORT).show();
  
    /* sendbutton = (Button) findViewById(R.id.button1);
	sendbutton.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String sensorid = test_1;
			String sensortype = "temperature";
			BufferedReader br = null;
			 
			try {
	 
				String sCurrentLine;
				
				File ext = Environment.getExternalStorageDirectory();
				File myFile = new File(ext, "mysdfile_25.txt");
	 
				br = new BufferedReader(new FileReader(myFile));
	 
				while ((sCurrentLine = br.readLine()) != null) {
					String[] numberSplit = sCurrentLine.split(":") ; 
					String time = numberSplit[ (numberSplit.length-2) ] ;
					String val = numberSplit[ (numberSplit.length-1) ] ;
					//System.out.println(sCurrentLine);
					HttpResponse httpresponse;
					//int responsecode;
					StatusLine responsecode;
					String strresp;
					
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost("https://rnicu-cloud.appspot.com/sensor/update");
					
					try{
					 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		             nameValuePairs.add(new BasicNameValuePair("sensorid", sensorid));
		             nameValuePairs.add(new BasicNameValuePair("sensortype", sensortype));
		             nameValuePairs.add(new BasicNameValuePair("time", time));
		             nameValuePairs.add(new BasicNameValuePair("val", val));
		           //  try {
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				//		try {
							httpresponse = httpclient.execute(httppost);
							//responsecode = httpresponse.getStatusLine().getStatusCode();
							responsecode = httpresponse.getStatusLine();
							strresp = responsecode.toString();
							Log.d("ARV Response msg from post", httpresponse.toString());
							Log.d("ARV status of post", strresp);
							HttpEntity httpentity = httpresponse.getEntity();
							Log.d("ARV entity string", EntityUtils.toString(httpentity));
						//	Log.d("ARV oly entity", httpentity.toString());
						//	Log.d("ARV Entity response", httpentity.getContent().toString());
							//httpclient.execute(httppost);
							Toast.makeText(getApplicationContext(), "Posted data and returned value:"+ strresp, Toast.LENGTH_LONG).show();
				//		} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
				//			e.printStackTrace();
				//		} catch (IOException e) {
							// TODO Auto-generated catch block
				//			e.printStackTrace();
				//		}
				//	} 
					}catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	 
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			
			
		//	Toast.makeText(getApplicationContext(), "Entered on click", Toast.LENGTH_SHORT).show();
			
			
			/*catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	});*/
    
    getActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onResume() {
    super.onResume();

    // Setup this view to listen to the model
    // in the traditional MVC pattern.
    model.addPropertyChangeListener(this);

    // Also listen to changes in connection state so
    // we can notify the user with toasts that
    // the device has been disconnected.
    Devices.INSTANCE.addPropertyChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();

    // Stop listening to changes.
    model.removePropertyChangeListener(this);
    Devices.INSTANCE.removePropertyChangeListener(this);
  }

  public void write_data(String sensid,String senstype,String val){
	  long timemilli =  System.currentTimeMillis();
		Log.d("TIMMEE", String.valueOf(timemilli));
		Toast.makeText(getBaseContext(), "IM here", Toast.LENGTH_SHORT).show();
		/*HttpResponse httpresponse;
		//int responsecode;
		StatusLine responsecode;
		String strresp;
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("https://rnicu-cloud.appspot.com/sensor/update");
		
		try{
		 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
       nameValuePairs.add(new BasicNameValuePair("sensorid", sensid));
       nameValuePairs.add(new BasicNameValuePair("sensortype", senstype));
       nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(timemilli)));
       nameValuePairs.add(new BasicNameValuePair("val", val));
     //  try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	//		try {
				httpresponse = httpclient.execute(httppost);
				//responsecode = httpresponse.getStatusLine().getStatusCode();
				responsecode = httpresponse.getStatusLine();
				strresp = responsecode.toString();
				Log.d("ARV Response msg from post", httpresponse.toString());
				Log.d("ARV status of post", strresp);
				HttpEntity httpentity = httpresponse.getEntity();
				Log.d("ARV entity string", EntityUtils.toString(httpentity));
			//	Log.d("ARV oly entity", httpentity.toString());
			//	Log.d("ARV Entity response", httpentity.getContent().toString());
				//httpclient.execute(httppost);
				Toast.makeText(getApplicationContext(), "Posted data and returned value:"+ strresp, Toast.LENGTH_LONG).show();
	//		} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (IOException e) {
				// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	} 
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	  
  }
  /**
   * This class listens to changes in the model of sensor values.
   * */
  
  @Override
  public void propertyChange(final PropertyChangeEvent event) {  
	final String property = event.getPropertyName();
	
    runOnUiThread(new Runnable() {
      public void run() {
        try {        		   
          /*if (property.equals(PROPERTY_ACCELEROMETER)) {
            // A change in accelerometer data has occured.
            Point3D newValue = (Point3D) event.getNewValue();
            String xaxis = decimal.format(newValue.x);
            String yaxis = decimal.format(newValue.y);
            String zaxis = decimal.format(newValue.z);

            String acl = "X: " + decimal.format(newValue.x) + "g" + "\nY: " + decimal.format(newValue.y) + "g" + "\nZ: " + decimal.format(newValue.z) + "g";
            
            ((TextView) findViewById(R.id.accelerometerTxt)).setText(acl);
            
          }
          } else */if (property.equals(PROPERTY_AMBIENT_TEMPERATURE)) {
        	  double newAmbientValue = (Double) event.getNewValue();
              
              final int img;
              TextView textView = (TextView) findViewById(R.id.ambientTemperatureTxt);
              String formattedText = "Touch Not Detected";
              
              if (newAmbientValue == 11.00)
              {
              	formattedText = "Touch Detected";
              	//img = touch;
              
              }	else
              { 
              	
              	//img = notouch;
              }
          

              //((ImageView) findViewById(R.id.touch)).setImageResource(img);
              
             
              
              textView.setText(formattedText);
          } /*ARVelse*/
          
          else if (property.equals(PROPERTY_IR_TEMPERATURE)) {
            double newIRValue = (Double) event.getNewValue();
          //  float newIRValue_1 = (Float) event.getNewValue();
            TextView textView = (TextView) findViewById(R.id.ir_temperature);
            String value = decimal.format(newIRValue);
            String formattedText = value + DEGREE_SYM;
            wakelockk.acquire();
            textView.setText(formattedText);
            tempwriteintoafile(value);
            analyze(Float.valueOf(value), patient);          // Added by Nihesh for analysis of incoming readings.
				                
            //ARV
            /*try {
				java.util.Date date = new java.util.Date();
				Timestamp chk = new Timestamp(date.getTime());
				long timemilli =  System.currentTimeMillis();
				String abc = String.valueOf(timemilli);//chk.toString();
				String separator = System.getProperty("line.separator");
				
				File ext = Environment.getExternalStorageDirectory();
				File myFile = new File(ext, "mysdfile_7.txt");
				if(myFile.exists()){
					try {
						FileOutputStream fOut = new FileOutputStream(myFile,true);
					    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
					    myOutWriter.append(abc);
					    myOutWriter.append("   ");
					    myOutWriter.append(formattedText_1);
					    myOutWriter.append(separator);
					    myOutWriter.flush();
					    myOutWriter.close();
					    fOut.close();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				else{
					myFile.createNewFile();
				}
			
			//	File myFile = new File("/sdcard/mysdfile.txt");
			//	myFile.createNewFile();
			//	FileOutputStream fOut = new FileOutputStream(myFile);
//ARV				OutputStreamWriter myOutWriter = 
//ARV										new OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_APPEND));//fOut
			//	myOutWriter.append(txtData.getText());
//ARV				myOutWriter.write(abc);
//ARV				myOutWriter.append(separator);
//ARV				myOutWriter.flush();
//ARV				myOutWriter.close();
		//		fOut.close();
				Toast.makeText(getBaseContext(),
						"Done writing SD 'mysdfile.txt'",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
		    } //ARV
		   
*/        
			
          }//endif
          
          
          /* else if (property.equals(PROPERTY_HUMIDITY)) {
            double newHumidity = (Double) event.getNewValue();
   // ARV        TextView textView = (TextView) findViewById(R.id.humidityTxt);
            String formattedText = decimal.format(newHumidity) + "%rH";
   //ARV         textView.setText(formattedText);
          } else if (property.equals(PROPERTY_MAGNETOMETER)) {
            Point3D newValue = (Point3D) event.getNewValue();

            String msg = "X: " + decimal.format(newValue.x) + "uT" + "\nY: " + decimal.format(newValue.y) + "uT" + "\nZ: " + decimal.format(newValue.z) + "uT";

   //ARV         ((TextView) findViewById(R.id.magnetometerTxt)).setText(msg);
          } else if (property.equals(PROPERTY_GYROSCOPE)) {
            Point3D newValue = (Point3D) event.getNewValue();

            String msg = "X: " + decimal.format(newValue.x) + "deg/s" + "\nY: " + decimal.format(newValue.y) + "deg/s" + "\nZ: " + decimal.format(newValue.z)
                + "deg/s";

  //ARV          ((TextView) findViewById(R.id.gyroscopeTxt)).setText(msg);
          } else if (property.equals(Measurements.PROPERTY_BAROMETER)) {
            Double newValue = (Double) event.getNewValue();

            String msg = new DecimalFormat("+0.0;-0.0").format(newValue / 100) + " hPa";

 //ARV           ((TextView) findViewById(R.id.barometerTxt)).setText(msg);
          } else if (property.equals(PROPERTY_SIMPLE_KEYS)) {
            SimpleKeysStatus newValue = (SimpleKeysStatus) event.getNewValue();

            final int img;
            switch (newValue) {
            case OFF_OFF:
              img = buttonsoffoff;
              break;
            case OFF_ON:
              img = buttonsoffon;
              break;
            case ON_OFF:
              img = buttonsonoff;
              break;
            case ON_ON:
              img = buttonsonon;
              break;
            default:
              throw new UnsupportedOperationException();
            }

            ((ImageView) findViewById(R.id.buttons)).setImageResource(img);
          }*/ else if (property.equals(LOST_DEVICE_ + CONNECTED)) {
            // A device has been disconnected
            // We notify the user with a toast

            int duration = Toast.LENGTH_SHORT;
            String text = "Lost connection";

            Toast.makeText(ServicesActivity.this, text, duration).show();
            finish();
          } else if (property.equals(NEW_DEVICE_ + CONNECTED)) {
            // A device has been disconnected
            // We notify the user with a toast

            int duration = Toast.LENGTH_SHORT;
            String text = "Established connection";

            Toast.makeText(ServicesActivity.this, text, duration).show();
          }
        } catch (NullPointerException e) {
          e.printStackTrace();
          // Could be that the ServicesFragment is no longer visible
          // But we still receive property change events.
          // referring to the views with findViewById will then return a null.
        }
      }
    });
  }
  
//Added by Nihesh. Code starts.
 protected void sendSMSMessage(String value, String patient){	  
	  /*
  	    String SENT = "sent.";
  	    String DELIVERED = "delivered.";
	    Intent sentIntent = new Intent(SENT);
	    //Create Pending Intents
	      PendingIntent sentPI = PendingIntent.getBroadcast(
	      getApplicationContext(), 0, sentIntent,
	      PendingIntent.FLAG_UPDATE_CURRENT);
	      
	    Intent deliveryIntent = new Intent(DELIVERED);

	      PendingIntent deliverPI = PendingIntent.getBroadcast(
	      getApplicationContext(), 0, deliveryIntent,
	      PendingIntent.FLAG_UPDATE_CURRENT);*/
	      
	  Log.i("Send SMS", "");
	  String phoneNo = "09740370321";
	  long timi22 = System.currentTimeMillis();
	  //timi22 = timi22 - 19800000;
	  String timi222 = String.valueOf(timi22);
	  //String msgc = String.format("%04d", count);
	  //String patient = "rsp";
	
	  try{
		  SmsManager smsManager = SmsManager.getDefault();
		  smsManager.sendTextMessage(phoneNo, null,'&'+patient+'$'+timi22+' '+value+'%' , null, null);
		  Toast.makeText(getApplicationContext(), "Sms sent.", Toast.LENGTH_LONG).show();
	  } catch (Exception e){
		  Toast.makeText(getApplicationContext(), "Sms failed, please try again.", Toast.LENGTH_LONG).show();
		  e.printStackTrace();
	  }
	  choice = 0;
	  return;
 }
 
 protected void analyze(float newvalue, String patient){ 
	    if (i < interval) 
	  {
		  adder = adder + newvalue;
		  if (max < newvalue){
			  max = newvalue;
		  }
		  if (min > newvalue){
			  min = newvalue;
		  }
		  i = i + 1;
	  } else if(i >= interval)
	  {
		  //String sendmessage = "Max: "+ max + "  " + "Min: " + min + "  " + "Avg: " + adder/interval + "\n";
		  String sendmessage1 = String.valueOf(adder/interval);
		  max = 0;
		  min = 42;
		  adder = 0;
		  checkInternetConenction();
		  if(choice == 1){
		      	httpcon(sendmessage1, patient);   
		         }
		         else {
		        	 Toast.makeText(this, "GSM signal strength is: "+ rss, Toast.LENGTH_SHORT).show();
		        	 if (rss!=99){
		        	   sendSMSMessage(sendmessage1, patient);
		        	  }
		        	 else{
		        		 Toast.makeText(this, "No network connection.Can't send message.", Toast.LENGTH_LONG).show();
		        	 }
		 			} 
		  //tempwriteintoafile(sendmessage);
		  i = 0;
	  }
	  return;
 }
 
 protected void tempwriteintoafile(String msg){
	  Calendar c1 = Calendar.getInstance();
	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  String timee = df.format(c1.getTime());
	  String msgc = String.format("%04d", count);
	  try{
	  tempfwriter = new FileWriter("/storage/emulated/0/Android/TemperatureReadingrajesh3110.txt", true);
	  tempfwriter.append(msgc + " ");
	  tempfwriter.append(timee + " ");
	  tempfwriter.append(msg + "\n");
	  tempfwriter.close();
	  count = count + 1;
	  if(count%interval==0){
		  Toast.makeText(getApplicationContext(), "Writting in file successful!!", Toast.LENGTH_LONG).show();  
	  }
 }
	  catch(Exception e){
		  e.printStackTrace();
	  }
	  return;
 }
 
 //check Internet conenction.
 private void checkInternetConenction(){
    ConnectivityManager check = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (check != null) 
    {
       NetworkInfo[] info = check.getAllNetworkInfo();
       if (info != null) 
          for (int i = 0; i <info.length; i++) {
          if (info[i].getState() == NetworkInfo.State.CONNECTED)
          {
        	 choice =1;
             Toast.makeText(this, "Internet is connected",Toast.LENGTH_SHORT).show();
             return;
          }
          }
    }
    Toast.makeText(this, "Not conencted to internet",Toast.LENGTH_SHORT).show();
    choice = 2;
    return;
    
 }
 
 protected void httpcon(String reading, String patient){
	 long timemilli =  System.currentTimeMillis();
	 //timemilli = timemilli - 19800000;
	 
	 HttpResponse httpresponse;
		//int responsecode;
		StatusLine responsecode;
		String strresp;
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("https://rnicu01.appspot.com/upload");
		
		try{
		 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("tag", patient));
      //nameValuePairs.add(new BasicNameValuePair("sensortype", sensortype));
      nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(timemilli)));
      nameValuePairs.add(new BasicNameValuePair("value", reading));
    //  try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	//		try {
				httpresponse = httpclient.execute(httppost);
				//responsecode = httpresponse.getStatusLine().getStatusCode();
				responsecode = httpresponse.getStatusLine();
				strresp = responsecode.toString();
				Log.d("ARV Response msg from post", httpresponse.toString());
				Log.d("ARV status of post", strresp);
				HttpEntity httpentity = httpresponse.getEntity();
				Log.d("ARV entity string", EntityUtils.toString(httpentity));
			//	Log.d("ARV oly entity", httpentity.toString());
			//	Log.d("ARV Entity response", httpentity.getContent().toString());
				//httpclient.execute(httppost);
				//Toast.makeText(getApplicationContext(), "Posted data and returned value:"+ strresp, Toast.LENGTH_LONG).show();
	//		} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} catch (IOException e) {
				// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	} 
		}catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 choice = 0;
 }
 
 private class MyPhoneStateListener extends PhoneStateListener
 {
   /* Get the Signal strength from the provider, each tiome there is an update */
   @Override
   public void onSignalStrengthsChanged(SignalStrength signalStrength)
   {
      super.onSignalStrengthsChanged(signalStrength);
      rss = signalStrength.getGsmSignalStrength();
   }

 }
  //Added by Nihesh. Code ends.
  
}
