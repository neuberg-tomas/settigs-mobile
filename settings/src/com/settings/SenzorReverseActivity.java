package com.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;


public class SenzorReverseActivity extends BaseActivity{

final private String TAG = "SenzorReverseActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	private final String protocolCode[] = {
			"SENSOR_REVX",
			"SENSOR_REVY",
			"SENSOR_REVZ",
	};
	
	private int formItems[] = {
			R.id.x_pitch_reverse,
			R.id.y_roll_reverse,
			R.id.z_yaw_reverse,
	};
	
	private DstabiProvider stabiProvider;
	
	private DstabiProfile profileCreator;
	
	private int lock = 0;
	
	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.senzor_reverse);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.senzor_button_text), " \u2192 " , getString(R.string.reverse)));
        
        stabiProvider =  DstabiProvider.getInstance(connectionHandler);
        
        initConfiguration();
		delegateListener();
    }
	
	 /**
	  * prvotni konfigurace view
	  */
	 private void initConfiguration()
	 {
		 sendInProgressRead();
		 // ziskani konfigurace z jednotky
		 stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	 }
	
	/**
	  * prirazeni udalosti k prvkum
	  */
	 private void delegateListener(){
		//nastaveni posluchacu pro formularove prvky
		 for(int i = 0; i < formItems.length; i++){
			 ((CheckBox) findViewById(formItems[i])).setOnCheckedChangeListener(checkboxListener);
		 }
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
	  * naplneni formulare
	  * 
	  * @param profile
	  */
	 private void initGuiByProfileString(byte[] profile){
		profileCreator = new DstabiProfile(profile);
		 
		if(!profileCreator.isValid()){
			errorInActivity(R.string.damage_profile);
			return;
		}
		
			for(int i = 0; i < formItems.length; i++){
				CheckBox tempCheckbox = (CheckBox) findViewById(formItems[i]);
				
				Boolean checked = profileCreator.getProfileItemByName(protocolCode[i]).getValueForCheckBox();
				if(checked)lock = lock + 1;
				tempCheckbox.setChecked(checked);
			}
	 }
	
	
	private OnCheckedChangeListener checkboxListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if(lock != 0){
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);
			
			// TODO Auto-generated method stub
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for(int i = 0; i < formItems.length; i++){
				if(buttonView.getId() == formItems[i]){
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromCheckBox(isChecked);
					stabiProvider.sendDataNoWaitForResponce(item);
					
					sendInProgress();
				}
			}
			
		}
		
	};
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
						sendInError();
						break;
					case DstabiProvider.MESSAGE_SEND_COMPLETE:
						sendInSuccess();
						break;
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
						}
						break;
	        		case PROFILE_CALL_BACK_CODE:
	        			if(msg.getData().containsKey("data")){
	        				initGuiByProfileString(msg.getData().getByteArray("data"));
	        				sendInSuccess();
	        			}
	        			break;
	        		case PROFILE_SAVE_CALL_BACK_CODE:
	        			sendInSuccess();
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
