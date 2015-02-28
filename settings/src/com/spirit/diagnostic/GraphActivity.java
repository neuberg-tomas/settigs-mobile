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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.analytics.tracking.android.EasyTracker;
import com.lib.BluetoothCommandService;
import com.lib.FFT;
import com.spirit.BaseActivity;
import com.spirit.PrefsActivity;
import com.spirit.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GraphActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "GraphActivity";


	//KODY PRO CALLBACKY //////////////////////////////
	final private int GRAPH_CALL_BACK_CODE = 21;

	final private int GROUP_AXIS = 222;
	final private int AXIS_X = 2222;
	final private int AXIS_Y = 2223;
	final private int AXIS_Z = 2224;


	final private int GROUP_ACTION = 333;
	final private int CHOOSE_SCREENSHOT = 444;
    final private int CHOOSE_FREEZE = 555;
    final private int CHOOSE_FORCE_SAVEGRAPH= 666;


	@SuppressWarnings("unused")
	final private int MESSAGE_FFT = 100;
	////////////////////////////////////////////////////

	//pro graf ////////////////////////////////////////
	private XYPlot aprLevelsPlot = null;
	private SimpleXYSeries aprLevelsSeries = null;
    private SimpleXYSeries aprLevelsSeriesFreeze  = null;
	///////////////////////////////////////////////////

    //jestli pri kliku na graf se ulozi screenshot
    final private int TAP_TO_NONE       = 0;
    final private int TAP_TO_SCREENSHOT = 1;
    final private int TAP_TO_FREEZE     = 2;
    private int tapToAction = TAP_TO_NONE;
    private boolean stateGraphFreeze = false;
    private boolean stateGraphFreezeSet = false;

    ////
	private byte[] dataBuffer;
	private int dataBuffer_len = 0;
	final private int DATABUFFER_SIZE = 3000;

	Number[] seriesX        = null;
    Number[] seriesXFreeze  = null;
	//Number[] seriesY = null;

	// FFT stuff
	final private int FFT_N = 1024;
	final private int FFT_NYQUIST = (FFT_N / 2) + 1;

	FFT fft = null;

	private double[] input_xr = null;
	private double[] input_xi = null;

	private float vibDelay = 0;

	private CharSequence baseTitle = getTitle();

	@SuppressLint("SimpleDateFormat")
	protected SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd_HHmmss");
	
	protected int[] topThree = {0,0,0};

	private LineAndPointFormatter formater;

    private LineAndPointFormatter formaterFreeze;

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.graph);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
	}
	
	/**
	 * handle for change banks
	 *
	 * @param v
	 */
	public void changeBankOpenDialog(View v){
		//disabled change bank in this activity
	}

	/**
	 * inicializace a nastaveni FFT
	 */
	private void initFFT()
	{
		fft = new FFT(10);    // nastavime rozliseni na 2^10

		// realna + imaginarni slozka pro vypocet FFT
		input_xr = new double[FFT_N];
		input_xi = new double[FFT_N];
	}

	/**
	 * inicializace a nastaveni zobrazeni grafu
	 */
	private void inicializeGraph()
	{
		seriesX         = new Number[FFT_NYQUIST];
        seriesXFreeze   = new Number[FFT_NYQUIST];

		aprLevelsSeries         = new SimpleXYSeries(Arrays.asList(seriesX), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
        aprLevelsSeriesFreeze   = new SimpleXYSeries(Arrays.asList(seriesXFreeze), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");

		PointLabelFormatter plf = new PointLabelFormatter(Color.WHITE);
		plf.getTextPaint().setTextSize(13);

        PointLabeler pointLabel = new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                if((index > 5 && (topThree[0] == index || topThree[1] == index || topThree[2] == index)) && series.getY(index).intValue() > 5){
                    return String.valueOf(Math.round(index * 60)) + " RPM";
                }
                return "";
            }

        };

		formater = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(238, 255, 182), plf);
		formater.setPointLabeler(pointLabel);

        formaterFreeze      = new LineAndPointFormatter(Color.rgb(200, 0, 0), null, null, null);

		// setup the APR Levels plot:
		aprLevelsPlot = (XYPlot) findViewById(R.id.vibration);
		aprLevelsPlot.addSeries(aprLevelsSeries, formater);

		//aprLevelsPlot.disableAllMarkup();
		aprLevelsPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);

		aprLevelsPlot.setRangeBoundaries(0, 110, BoundaryMode.FIXED);
		aprLevelsPlot.setRangeLabel(getString(R.string.amplitude));
		aprLevelsPlot.setRangeStepValue(50);

		aprLevelsPlot.setDomainBoundaries(0, 500, BoundaryMode.FIXED);
		aprLevelsPlot.setDomainLabel(String.valueOf(TextUtils.concat(getString(R.string.frequency), " ", getString(R.string.hz), " / ", getString(R.string.axis_X))));
		aprLevelsPlot.setRangeStepValue(10);
		aprLevelsPlot.setOnClickListener(clickActionistener);
		aprLevelsPlot.getLayoutManager().remove(aprLevelsPlot.getLegendWidget());
	}

	/**
	 * update grafu po vypocteni novych dat
	 *
	 * @param seriesX
	 */
	private void updateGraph(Number[] seriesX)
	{
        if(stateGraphFreeze){
            if(!stateGraphFreezeSet) {
                aprLevelsPlot.addSeries(aprLevelsSeriesFreeze, formaterFreeze);
                stateGraphFreezeSet = true;
            }
        }else{
            aprLevelsPlot.removeSeries(aprLevelsSeriesFreeze);
            aprLevelsSeriesFreeze.setModel(Arrays.asList(seriesX), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
            stateGraphFreezeSet = false;
        }



		topThree = this.topThree(seriesX);
		aprLevelsSeries.setModel(Arrays.asList(seriesX), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		aprLevelsPlot.redraw();
	}

	/**
	 * stopnuti aktovity, posle pozadavek na ukonceni streamu
	 */
	@Override
	public void onStop()
	{
		super.onStop();
		stabiProvider.stopGraph();
		vibDelay = 0;
        EasyTracker.getInstance(this).activityStop(this);
	}


	/**
	 * zapnem stream
	 */
	public void startGraph()
	{
		Runnable thread = new Runnable()
		{
			@Override
			public void run()
			{
				stabiProvider.getGraph(GRAPH_CALL_BACK_CODE);
			}
		};

		//startTime = System.nanoTime();//START
        stateGraphFreeze    = false;
		thread.run();
	}

	/**
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();

        baseTitle = TextUtils.concat("... \u2192 ", getString(R.string.diagnostic_button_text), " \u2192 ", getString(R.string.graph_button_text));
        ((TextView) findViewById(R.id.title)).setText(baseTitle);
        ((TextView) findViewById(R.id.vibration_level_text)).setText(R.string.vibration_level);

		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			showConfirmDialogWithCancel(R.string.graph_warn,
				new OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// inicializace FFT
						initFFT();
						// inicializujeme graf
						inicializeGraph();

						startGraph();
					}
				}
				, new OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				});


			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);

			((TextView) findViewById(R.id.title)).setText(TextUtils.concat(baseTitle, " ", getString(R.string.axis_X)));

		} else {
			finish();
		}
	}

	/**
	 * pocitani velikosti vibraci v %
	 *
	 * @param seriesX
	 * @return void
	 */
	protected void updateVibratonLevel(Number[] seriesX)
	{
		int vib = 0;
		for (int i = 0; i < seriesX.length; i++) {
			vib += seriesX[i].intValue();
		}

		vib /= (FFT_NYQUIST / 25);

		vibDelay += 0.5f * (vib - vibDelay);

		if (vibDelay < 2) {
			vibDelay = 0;
		}

		((TextView) findViewById(R.id.vibrationLevel)).setText(String.valueOf(Math.round(vibDelay)) + "%");

	}

	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case GRAPH_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					int len = msg.getData().getByteArray("data").length;
					byte[] data = msg.getData().getByteArray("data");

					if (dataBuffer == null){
						dataBuffer = new byte[DATABUFFER_SIZE + 72];    // 3byte*1024
					}

					if ((dataBuffer_len + len) > DATABUFFER_SIZE){        // osetreni preteceni
						len = DATABUFFER_SIZE - dataBuffer_len;
					}

					// rychlejsi konkatenace
					System.arraycopy(data, 0, dataBuffer, dataBuffer_len, len);
					dataBuffer_len += len;

					// buffer je plny
					if (dataBuffer_len == DATABUFFER_SIZE) {
						dataBuffer_len = 0;

						for (int i = 0; i < DATABUFFER_SIZE; ) {
							if ((int) dataBuffer[i] == -5) {    // nasli jsme magic byte	-5 & 0xFF = 251 = 0xFB
								short val = (short) ((dataBuffer[i + 1] & 0xFF) | ((dataBuffer[i + 2] & 0xFF) << 8));

								input_xr[i / 3] = val;
								input_xi[i / 3] = 0;

								//input_xr[i/3] = (Math.cos (i/3*5)*50 + Math.cos (i/3*4)*30);

								i += 3;
							} else {
								i++;
							}
						}

						// aplikujeme Hamming okno
						for (int i = 0; i < FFT_N; i++){
							input_xr[i] = (double) ((input_xr[i])) * (0.54f - (0.46f * Math.cos((2 * Math.PI * i) / (FFT_N - 1))));
						}

						// fft pocitam zatim primo tady - je to docela rychle
						// TODO udelat v samostatnem vlaknu
						fft.doFFT(input_xr, input_xi, false);

						// vypocet amplitud celeho spektra
						for (int i = 0; i < FFT_NYQUIST; i++) {
							seriesX[i] = (double) Math.sqrt((input_xr[i] * input_xr[i]) + (input_xi[i] * input_xi[i])) * 5.07;

							// nezobrazujeme prvnich par Hz - je to urcite pohyb heli
							if (i < 5) seriesX[i] = 0;
						}

						// prekreslime graf - TODO udelat v samostatnem vlaknu
						updateGraph(seriesX);
						updateVibratonLevel(seriesX);
					}
				}
				break;
			default:
				super.handleMessage(msg);
		}

		return true;
	}

    protected void saveGraphToImage()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(GraphActivity.this);
        String filename = sharedPrefs.getString(PrefsActivity.PREF_APP_DIR, "") + PrefsActivity.PREF_APP_PREFIX + PrefsActivity.PREF_APP_GRAPH_DIR + "/" + sdf.format(new Date()) + "-log.png";

        try {
            aprLevelsPlot.setDrawingCacheEnabled(true);
            int width = aprLevelsPlot.getWidth();
            int height = aprLevelsPlot.getHeight();
            aprLevelsPlot.measure(width, height);
            Bitmap bmp = Bitmap.createBitmap(aprLevelsPlot.getDrawingCache());
            aprLevelsPlot.setDrawingCacheEnabled(false);
            FileOutputStream fos;

            fos = new FileOutputStream(filename, true);
            bmp.compress(CompressFormat.PNG, 100, fos);

            Toast.makeText(getApplicationContext(), R.string.save_done, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.not_save, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

	View.OnClickListener clickActionistener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			if (tapToAction == TAP_TO_SCREENSHOT) {
                saveGraphToImage();
			}else if(tapToAction == TAP_TO_FREEZE){
                if(stateGraphFreeze){
                    Toast.makeText(getApplicationContext(), R.string.tap_to_freeze_is_off, Toast.LENGTH_SHORT).show();
                    stateGraphFreeze    = false;
                }else{
                    Toast.makeText(getApplicationContext(), R.string.tap_to_freeze_is_on, Toast.LENGTH_SHORT).show();
                    stateGraphFreeze    = true;
                }

            }
		}
	};

	/**
	 * vytvoreni kontextoveho menu
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();

		menu.add(GROUP_AXIS, AXIS_X, Menu.NONE, R.string.axis_X);
		menu.add(GROUP_AXIS, AXIS_Y, Menu.NONE, R.string.axis_Y);
		menu.add(GROUP_AXIS, AXIS_Z, Menu.NONE, R.string.axis_Z);

        SubMenu taptoActionSubMen = menu.addSubMenu(R.string.tap_to_action);

        switch(tapToAction){
            case TAP_TO_NONE:
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_SCREENSHOT, Menu.NONE, R.string.tap_to_screenshot_on);
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_FREEZE, Menu.NONE, R.string.tap_to_freeze_on);
                break;
            case TAP_TO_SCREENSHOT:
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_SCREENSHOT, Menu.NONE, R.string.tap_to_screenshot_off);
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_FREEZE, Menu.NONE, R.string.tap_to_freeze_on);
                break;
            case TAP_TO_FREEZE:
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_SCREENSHOT, Menu.NONE, R.string.tap_to_screenshot_on);
                taptoActionSubMen.add(GROUP_ACTION, CHOOSE_FREEZE, Menu.NONE, R.string.tap_to_freeze_off);

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(GraphActivity.this);
                if(sharedPrefs.contains(PrefsActivity.PREF_APP_DIR) && stateGraphFreeze) {
                    taptoActionSubMen.add(GROUP_ACTION, CHOOSE_FORCE_SAVEGRAPH, Menu.NONE, R.string.force_save_graph);
                }
                break;
        }

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * reakce na kliknuti polozky v kontextovem menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		//zobrazeni osy X
		if (item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_X) {
			Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + " " + getString(R.string.axis_X), Toast.LENGTH_SHORT).show();
			stabiProvider.sendDataImmediately("4DA\1".getBytes());            // HACK, chtelo by to vylepsit :)
			aprLevelsPlot.setDomainLabel(String.valueOf(TextUtils.concat(getString(R.string.frequency), " ", getString(R.string.hz), " / ", getString(R.string.axis_X))));
			((TextView) findViewById(R.id.title)).setText(TextUtils.concat(baseTitle, " ", getString(R.string.axis_X)));
            formater.getFillPaint().setColor(Color.rgb(238, 255, 182));
		}

		//zobrazeni osy Y
		if (item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Y) {
			Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + " " + getString(R.string.axis_Y), Toast.LENGTH_SHORT).show();
			stabiProvider.sendDataImmediately("4DA\2".getBytes());            // HACK, chtelo by to vylepsit :)
			aprLevelsPlot.setDomainLabel(String.valueOf(TextUtils.concat(getString(R.string.frequency), " ", getString(R.string.hz), " / ", getString(R.string.axis_Y))));
			((TextView) findViewById(R.id.title)).setText(TextUtils.concat(baseTitle, " ", getString(R.string.axis_Y)));
            formater.getFillPaint().setColor(Color.rgb(197, 236, 255));
		}

		//zobrazeni osy Z
		if (item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Z) {
			Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + " " + getString(R.string.axis_Z), Toast.LENGTH_SHORT).show();
			stabiProvider.sendDataImmediately("4DA\3".getBytes());            // HACK, chtelo by to vylepsit :)
			aprLevelsPlot.setDomainLabel(String.valueOf(TextUtils.concat(getString(R.string.frequency), " ", getString(R.string.hz), " / ", getString(R.string.axis_Z))));
			((TextView) findViewById(R.id.title)).setText(TextUtils.concat(baseTitle, " ", getString(R.string.axis_Z)));
            formater.getFillPaint().setColor(Color.rgb(246, 183, 240));
		}

		//ulozeni obrazku grafu
		if (item.getGroupId() == GROUP_ACTION) {
            if(item.getItemId() == CHOOSE_SCREENSHOT){
                switch(tapToAction){
                    case TAP_TO_FREEZE:
                        stateGraphFreeze = false;
                    case TAP_TO_NONE:
                        //zjistime jestli je nastaven adresar pro ulozeni obrazku z grafu
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(GraphActivity.this);
                        if(!sharedPrefs.contains(PrefsActivity.PREF_APP_DIR)){
                            Toast.makeText(getApplicationContext(), R.string.first_choose_directory, Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(GraphActivity.this, PrefsActivity.class);
                            startActivity(i);
                            return false;
                        }
                        Toast.makeText(getApplicationContext(), R.string.tap_to_screenshot_on_state, Toast.LENGTH_SHORT).show();
                        tapToAction = TAP_TO_SCREENSHOT;
                        break;
                    case TAP_TO_SCREENSHOT:
                        tapToAction = TAP_TO_NONE;
                        Toast.makeText(getApplicationContext(), R.string.tap_to_screenshot_off_state, Toast.LENGTH_SHORT).show();
                        break;
                }
            }else if(item.getItemId() == CHOOSE_FREEZE){
                switch(tapToAction){
                    case TAP_TO_SCREENSHOT:
                    case TAP_TO_NONE:
                        tapToAction = TAP_TO_FREEZE;
                        Toast.makeText(getApplicationContext(), R.string.tap_to_freeze_on_state, Toast.LENGTH_SHORT).show();
                        break;
                    case TAP_TO_FREEZE:
                        tapToAction = TAP_TO_NONE;
                        stateGraphFreeze = false;
                        Toast.makeText(getApplicationContext(), R.string.tap_to_freeze_off_state, Toast.LENGTH_SHORT).show();
                        break;
                }
            }else if(item.getItemId() == CHOOSE_FORCE_SAVEGRAPH)
            {
                if(tapToAction == TAP_TO_FREEZE){
                    saveGraphToImage();
                }
            }



		}
		return false;
	}

	/**
	 * tohle je zatim nastrel
	 * 
	 * @param seriesX2
	 * @return
	 */
	public int[] topThree(Number[] seriesX2) {
		int max1 = Integer.MIN_VALUE;
		int max2 = Integer.MIN_VALUE;
		int max3 = Integer.MIN_VALUE;
		
		Number max1Value = 0;
		Number max2Value = 0;
		Number max3Value = 0;
		
		Number prewValueValue = 0;
		
		boolean lock = false;
		
		int i = 0;
		
        for (Number number : seriesX2) {
        	
        	if(number.floatValue() > prewValueValue.floatValue() || number.floatValue() < (/*prewValueValue.floatValue() +*/ 5f) ){
        		lock = false;
        	}
        	
            if (number.floatValue() > max1Value.floatValue() && !lock) {
                max3 = max2;
                max2 = max1;
                max1 = i;
                
                max3Value = max2Value;
                max2Value = max1Value;
                max1Value = number;
                lock = true;
                
            } else if (number.floatValue() > max2Value.floatValue() && !lock) {
            	max3 = max2;
                max2 = i;
                
                max3Value = max2Value;
                max2Value = number;
                
                lock = true;
            }else if (number.floatValue() > max3Value.floatValue() && !lock) {
                max3 = i;
                
                max3Value = number;
                lock = true;
            }
            
            prewValueValue = number;
            i++;
        }
        
        int[] ret = { max1, max2, max3};
        return ret;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }
}
