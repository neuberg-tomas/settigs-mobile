/*
Copyright (C) Petr Cada and Tomas Jedrzejek
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.spirit.diagnostic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.helpers.ByteOperation;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.R;
import com.spirit.BaseActivity;

public class DiagnosticActivity extends BaseActivity{
	final private String TAG = "DiagnosticActivity";
	
	final private int DIAGNOSTIC_CALL_BACK_CODE = 21;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 22;
	
	private DstabiProvider stabiProvider;
	
	
	final private Handler delayHandle = new Handler();
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.diagnostic);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.diagnostic_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		getPositionFromUnit();
    }
	
	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			finish();
		}
	}
	
	/**
	 * ziskani informace o poloze kniplu z jednotky
	 */
	protected void getPositionFromUnit(){
		delayHandle.postDelayed(new Runnable() {
		  @Override
		  public void run() {
			  stabiProvider.getDiagnostic(DIAGNOSTIC_CALL_BACK_CODE);
		  }
		}, 250); // 250ms
		
	}
	
	protected void updateGui(byte[] b){
		
		//AILERON
		int aileron = ByteOperation.twoByteToSigInt(b[0], b[1]);
		int aileronPercent = Math.round((100f / 340f) *  aileron);
		((ProgressBar)findViewById(R.id.aileron_progress_diagnostic)).setProgress(Math.round(aileronPercent + 100));
		((TextView)findViewById(R.id.aileron_value_diagnostic)).setText(String.valueOf(aileronPercent));
		
		//ELEVATOR
		int elevator = ByteOperation.twoByteToSigInt(b[2], b[3]);
		int elevatorPercent = Math.round((100f / 340f) *  elevator);
		((ProgressBar)findViewById(R.id.elevator_progress_diagnostic)).setProgress(Math.round(elevatorPercent + 100));
		((TextView)findViewById(R.id.elevator_value_diagnostic)).setText(String.valueOf(elevatorPercent ));
		
		//RUDDER
		int rudder = ByteOperation.twoByteToSigInt(b[6], b[7]);
		int rudderPercent = Math.round((100f / 340f) *  rudder);
		((ProgressBar)findViewById(R.id.rudder_progress_diagnostic)).setProgress(Math.round(rudderPercent + 100));
		((TextView)findViewById(R.id.rudder_value_diagnostic)).setText(String.valueOf(rudderPercent));
		
		//PITCH
		int pitch = ByteOperation.twoByteToSigInt(b[4], b[5]);
		int pitchPercent = Math.round((100f / 340f) *  pitch);
		((ProgressBar)findViewById(R.id.pitch_progress_diagnostic)).setProgress(Math.round(pitchPercent + 100));
		((TextView)findViewById(R.id.pitch_value_diagnostic)).setText(String.valueOf(pitchPercent));
		
		//GYRO
		int gyro = ByteOperation.twoByteToSigInt(b[8], b[9]) + 427;
		int gyroPercent = Math.round((100f / 765f) *  gyro);
		((ProgressBar)findViewById(R.id.gyro_progress_diagnostic)).setProgress(Math.round(gyroPercent));
		((TextView)findViewById(R.id.gyro_value_diagnostic)).setText(String.valueOf(gyroPercent));
		
		//SENZOR X Y Z
		((TextView)findViewById(R.id.diagnostic_x)).setText(String.valueOf(ByteOperation.twoByteToSigInt(b[10], b[11])));
		((TextView)findViewById(R.id.diagnostic_y)).setText(String.valueOf(ByteOperation.twoByteToSigInt(b[12], b[13])));
		((TextView)findViewById(R.id.diagnostic_z)).setText(String.valueOf(ByteOperation.twoByteToSigInt(b[14], b[15])));
		
	}
	
	
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
		        		Log.d(TAG, "Prisla chyba");
		        		getPositionFromUnit();
		        		
		        		//sendInError();
						break;
					case DstabiProvider.MESSAGE_SEND_COMPLETE:
						sendInSuccessInfo();
						break;
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
						}
						break;
	        		case DIAGNOSTIC_CALL_BACK_CODE:
	        			if(msg.getData().containsKey("data")){
	        				
	        				if(msg.getData().getByteArray("data").length > 16){
	        					Log.d(TAG, "Odpoved delsi nez 16");
	        				}
	        				
	        				updateGui(msg.getData().getByteArray("data"));
	        				
	        				getPositionFromUnit();
	        			}
	        			break;
	        		case PROFILE_SAVE_CALL_BACK_CODE:
	        			sendInSuccessDialog();
	        			showProfileSavedDialog();
	        			break;
	        	}
	        }
	    };

	/**
     * vytvoreni kontextoveho menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	    super.onCreateOptionsMenu(menu);
	    
	    menu.add(GROUP_SAVE, SAVE_PROFILE_MENU, Menu.NONE, R.string.save_profile_to_unit);
	    return true;
    }
    
    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	 super.onOptionsItemSelected(item);
    	//ulozit do jednotky
    	if(item.getGroupId() == GROUP_SAVE && item.getItemId() == SAVE_PROFILE_MENU){
    		saveProfileToUnit(stabiProvider, PROFILE_SAVE_CALL_BACK_CODE);
    	}
    	return false;
    }
	
}