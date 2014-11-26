/**************************************************************************************************
  Filename:       PreferencesActivity.java
  Revised:        $Date: 2013-08-30 11:43:54 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27452 $

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
package com.ti.sensortag.gui.preferences;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.ti.sensortag.R;

public class PreferencesActivity extends PreferenceActivity {

  @SuppressWarnings("unused")
  private static final String TAG = "PreferencesActivity";
  private BluetoothDevice device;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    device = getIntent().getParcelableExtra("com.ti.sensortag.device");
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    super.onBuildHeaders(target);
    loadHeadersFromResource(R.xml.preference_headers, target);
  }

  public BluetoothDevice getDevice() {
    return device;
  }
}
