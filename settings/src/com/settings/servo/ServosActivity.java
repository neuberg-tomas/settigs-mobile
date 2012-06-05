package com.settings.servo;

import java.util.ArrayList;
import java.util.HashMap;

import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ServosActivity extends BaseActivity{
	final private String TAG = "ServosActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.servos);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.servos_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://servo type
            			openServosTypeActivity(view);
            			break;
            		case 1://subtrim
            			openServosSubtrimActivity(view);
            			break;
            		case 2://limit
            			openServosLimitActivity(view);
            			break;
            	}
            }
		});
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
	 * vytvoreni pole pro adapter menu listu
	 * 
	 * tohle se bude vytvaret dynamicky z pole
	 * 
	 * @return
	 */
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();
		//type
		HashMap<Integer, Integer> type = new HashMap<Integer, Integer>();
		type.put(TITLE_FOR_MENU, R.string.type);
		type.put(ICO_RESOURCE_ID, R.drawable.i9);
		menuListData.add(type);
		
		//subtrim
		HashMap<Integer, Integer> subtrim = new HashMap<Integer, Integer>();
		subtrim.put(TITLE_FOR_MENU, R.string.subtrim);
		subtrim.put(ICO_RESOURCE_ID, R.drawable.i10);
		menuListData.add(subtrim);
		
		//limit
		HashMap<Integer, Integer> limit = new HashMap<Integer, Integer>();
		limit.put(TITLE_FOR_MENU, R.string.limit);
		limit.put(ICO_RESOURCE_ID, R.drawable.i11);
		menuListData.add(limit);
		
		return menuListData;
	}
	
	/**
	 * otevreni aktivity pro typ serva
	 * 
	 * @param v
	 */
	public void openServosTypeActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosTypeActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity pro subtrim serva
	 * 
	 * @param v
	 */
	public void openServosSubtrimActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosSubtrimActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity pro limit serva
	 * 
	 * @param v
	 */
	public void openServosLimitActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosLimitActivity.class);
    	startActivity(i);
	}
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
							finish();
						}else{
							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
						}
						break;
	        	}
	        }
	    };
	
}