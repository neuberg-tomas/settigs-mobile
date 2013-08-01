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

package com.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

public class AuthorActivity extends BaseActivity{

	final private String TAG = "AuthorActivity";
	
	private DstabiProvider stabiProvider;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.author);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(getText(R.string.full_app_name));
		
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
    }
	
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
		}
	}
	
	// The Handler that gets information back from the 
	 	 private final Handler connectionHandler = new Handler() {
	 	        @Override
	 	        public void handleMessage(Message msg) {
	 	        	switch(msg.what){
	 	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
	 						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
	 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
	 						}else{
	 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
	 						}
	 						break;
	 	        	}
	 	        }
	 	    };
	
}
