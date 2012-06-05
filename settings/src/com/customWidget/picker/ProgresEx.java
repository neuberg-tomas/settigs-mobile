package com.customWidget.picker;


import com.settings.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProgresEx extends LinearLayout implements OnClickListener,  OnLongClickListener {
	private static final String TAG = "ProgresEx";
	
	static private int DEFAULT_MIN = 0;
	static private int DEFAULT_MAX = 100;
	
	
	//Prvky formulare
	private final TextView  mObjTitle;
	private final TextView  mObjMin;
	private final TextView  mObjMax;
	private final TextView  mObjCurrent;
	private final EditText  mObjProgresValue;
	private final RelativeLayout childLayout;
	private final RelativeLayout mainLayout;
	
	private final ProgressBar  mObjProgres;
	
	private int mCurrent = 50;
	private int mMin = DEFAULT_MIN;
	private int mMax = DEFAULT_MAX;
	private String mTitle = "";
	
	private ProgresExButton mIncrementButton;
	private ProgresExButton mDecrementButton;
	
	private OnChangedListener mListener;
	
	private boolean mIncrement;
    private boolean mDecrement;
	
    private final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (mIncrement) {
                changeCurrent(mCurrent + 1);
                mHandler.postDelayed(this, 100);
            } else if (mDecrement) {
                changeCurrent(mCurrent - 1);
                mHandler.postDelayed(this, 100);
            }
        }
    };
    
	public interface OnChangedListener {
		void onChanged(ProgresEx picker, int newVal);
	}
	
	public ProgresEx(Context context) {
	    this(context, null);
	}
	
	public ProgresEx(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	
	public ProgresEx(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs);
	    
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.progres_ex, this, true);
	    
	    mainLayout = (RelativeLayout) findViewById(R.id.progres_main); 
	    mainLayout.setOnClickListener(this);
	    
	    mObjTitle 	= (TextView) findViewById(R.id.progres_title); 
	    
	    mObjMin 	= (TextView) findViewById(R.id.progres_min); 
	    mObjMax 	= (TextView) findViewById(R.id.progres_max); 
	    mObjCurrent = (TextView) findViewById(R.id.progres_current); 
	    
	    mObjProgres = (ProgressBar) findViewById(R.id.progres_bar); 
	    
	    mObjProgresValue = (EditText) findViewById(R.id.progres_value); 
	    mObjProgresValue.setFocusable(false);
	    
	    mDecrementButton = (ProgresExButton) findViewById(R.id.progres_minus);
	    mDecrementButton.setOnClickListener(this);
	    mDecrementButton.setOnLongClickListener(this);
	    mDecrementButton.setProgresEx(this);
	    
	    mIncrementButton = (ProgresExButton) findViewById(R.id.progres_plus);
	    mIncrementButton.setOnClickListener(this);
	    mIncrementButton.setOnLongClickListener(this);
	    mIncrementButton.setProgresEx(this);

	    childLayout = (RelativeLayout) findViewById(R.id.progres_child);
	    childLayout.setVisibility(View.GONE);
	    childLayout.setOnClickListener(this);;
	    
	    mHandler = new Handler();
	}
	
	/**
	 * nastaveni aktialni hodnoty
	 * 
	 * @param mCurrent
	 */
	public void setCurrent(int mCurrent) {
		this.mCurrent = mCurrent;
		
		notifyChange();
		updateView();
	}
	
	public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }
	
	/**
	 * zmeni se hodnota curent
	 */
	protected void notifyChange() {
        if (mListener != null) {
            mListener.onChanged(this, mCurrent);
        }
    }
	
	/**
	 * nastaveni rozsahu
	 * 
	 * @param mMin
	 * @param mMax
	 */
	public void setRange(int mMin, int mMax) {
		this.mMin = mMin;
		this.mMax = mMax;
		
		mObjProgres.setMax(mMax - mMin);
		
		mCurrent = mMin;
        updateView();
	}
	
	/**
	 * nastaveni titilku
	 * 
	 * @param mTitle
	 */
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
		updateView();
	}
	
	/**
	 * nastaveni titilku
	 * 
	 * @param mTitle
	 */
	public void setTitle(int mTitle) {
		setTitle(getContext().getString(mTitle));
	}
	
	/**
	 * pri zmene nejakeho prvku updatujem view
	 */
	protected void updateView() {
		mObjTitle.setText(mTitle);
		mObjProgres.setProgress(mCurrent - mMin);
		mObjMin.setText(String.valueOf(mMin));
		mObjMax.setText(String.valueOf(mMax));
		
		mObjProgresValue.setText(String.valueOf(mCurrent));
		mObjCurrent.setText(String.valueOf(mCurrent));
    }
	
	protected void changeCurrent(int current) {
        // Wrap around the values if we go past the start or end
        if (current > mMax) {
            current = mMax;
        } else if (current < mMin) {
            current = mMin;
        }
        
        mCurrent = current;

        notifyChange();
        updateView();
    }

	@Override
	public void onClick(View v) {
        //validateInput(mText);
        //if (!mText.hasFocus()) mText.requestFocus();

        // now perform the increment/decrement
        if (R.id.progres_minus == v.getId()) {
            changeCurrent(mCurrent-1);
        } else if (R.id.progres_plus == v.getId()) {
            changeCurrent(mCurrent+1);
        }else if (R.id.progres_main == v.getId()) {
            toogleInput();
        }
    }
	
	/**
     * We start the long click here but rely on the {@link NumberPickerButton}
     * to inform us when the long click has ended.
     */
    public boolean onLongClick(View v) {
        if (R.id.progres_plus == v.getId()) {
            mIncrement = true;
            mHandler.post(mRunnable);
        } else if (R.id.progres_minus == v.getId()) {
            mDecrement = true;
            mHandler.post(mRunnable);
        }
        return true;
    }
    
    public void cancelIncrement() {
        mIncrement = false;
    }

    public void cancelDecrement() {
        mDecrement = false;
    }
        
    private void toogleInput(){
    	if(childLayout.getVisibility() == View.GONE){
    		showInput();
    	}else{
    		hideInput();
    	}
    }
    
    public void showInput(){
    	if(childLayout.getVisibility() == View.GONE){
    		childLayout.setVisibility(View.VISIBLE);
    	}
    }
    
    public void hideInput(){
    	if(childLayout.getVisibility() == View.VISIBLE){
    		childLayout.setVisibility(View.GONE);
    	}
    }
}