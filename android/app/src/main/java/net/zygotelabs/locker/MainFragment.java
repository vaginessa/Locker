package net.zygotelabs.locker;

import net.zygotelabs.locker.dialogs.DisableLockProtectionDialog;
import net.zygotelabs.locker.dialogs.EnableLockProtectionDialog;
import net.zygotelabs.locker.utils.DeviceAdminManager;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainFragment extends Fragment  {
	private CheckBox checkBox;
    private CheckBox checkBoxHideWarning;
    private CheckBox checkBoxSafeMode;
    private RelativeLayout layoutSafeMode;
	private Button button;
    private RelativeLayout statusLayout;
    private TextView statusTextTitle;
    private TextView statusTextSummary;
    private TextView seekTextValue;
    private SeekBar lockProgress;
	private DeviceAdminManager dam;

    private int mStackLevel = 0;
	private static final int ENABLE_PROTECTION_DIALOG_FRAGMENT = 5;
	private static final int DISABLE_PROTECTION_DIALOG_FRAGMENT = 6;

	/* Our preferences */
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	
	protected static final int REQUEST_CODE_ENABLE_ADMIN=1;
	
	public MainFragment() {

	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }

        dam = new DeviceAdminManager(getActivity());
		/* Load our preferences */
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = settings.edit();

    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		checkBox = (CheckBox) rootView.findViewById(R.id.checkBoxAdmin);
        checkBoxHideWarning = (CheckBox) rootView.findViewById(R.id.checkBoxHideWarning);
        checkBoxSafeMode = (CheckBox) rootView.findViewById(R.id.checkBoxSafeMode);
        layoutSafeMode = (RelativeLayout) rootView.findViewById(R.id.safety_layout);
		button = (Button) rootView.findViewById(R.id.buttonApply);
		statusLayout = (RelativeLayout) rootView.findViewById(R.id.top_layout);
		statusTextTitle = (TextView) rootView.findViewById(R.id.textViewTopTitle);
		statusTextSummary = (TextView) rootView.findViewById(R.id.textViewTopTitleSummary);
		seekTextValue = (TextView) rootView.findViewById(R.id.textViewLockerCount);
		lockProgress = (SeekBar) rootView.findViewById(R.id.seekBarLocker);

        //If API level = 21 we hide checkBoxHideWarning and checkBoxSafeMode completely
        if (Build.VERSION.SDK_INT == 21){
            checkBoxHideWarning.setVisibility(View.INVISIBLE);
            checkBoxHideWarning.setChecked(false);
            layoutSafeMode.setVisibility(View.INVISIBLE);
            checkBoxSafeMode.setChecked(false);
            editor.putBoolean("safeMode", false);
            editor.putBoolean("hideWarning", false);
            editor.commit();
        }
		
		lockProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                if (progress == 0) {
                    lockProgress.setProgress(progress += 1);
                }
                seekTextValue.setText(String.valueOf(progress));
                editor.putInt("unlockLimit", lockProgress.getProgress());
                editor.commit();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });
		updateAdminCheck();
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	    super.onActivityResult(requestCode, resultCode, data);
	    updateAdminCheck();
	    
	    switch(requestCode) {
        case ENABLE_PROTECTION_DIALOG_FRAGMENT:

            if (resultCode == Activity.RESULT_OK) {
            	enableLockProtection();
            } else if (resultCode == Activity.RESULT_CANCELED){
                // After Cancel code.
            }

            break;
        case DISABLE_PROTECTION_DIALOG_FRAGMENT:
        	if (resultCode == Activity.RESULT_OK) {
        		disableLockProtection();
        	}
        	break;
        }
	}
	
	public void onCheckBoxClicked(boolean checked){
	    if (checked) {
	    	// Launch the activity to have the user enable admin privileges for Locker
	    	startActivityForResult(dam.getStartAdminEnableIntent(), REQUEST_CODE_ENABLE_ADMIN);
            
	    }else {
            // Remove admin privileges for app.
            if (dam.removeAdminRole()){
                adjustAdminUI(false);
            }
	    }
	}


    public void onSafeModeCheckBoxClicked(boolean checked){
        /* SafeMode checkbox was ticked */
        if (checked) {
           /* Also check hide warning dialog */
            checkBoxHideWarning.setChecked(true);
            checkBoxHideWarning.setEnabled(false);
        } else {
            checkBoxHideWarning.setEnabled(true);
        }
    }
	
	private void updateAdminCheck(){   
		adjustAdminUI(dam.isActiveAdmin());
    	lockProgress.setProgress(settings.getInt("unlockLimit", 5));
    	
	}
	
	private void updateLockStatus(){
		boolean isProtected = dam.isProtected();
		
    	if (isProtected){
            int unlockLimit = settings.getInt("unlockLimit", 5);

			// Check if safe mode is enabled
			if (settings.getBoolean("safeMode", false)){
				statusTextTitle.setText(getActivity().getString(R.string.protected_safe_mode));
                statusTextSummary.setText(getActivity().getString(R.string.protected_safe_mode_summary));
                animateStatusLayoutColor(R.color.colorOrange);
			} else {
                animateStatusLayoutColor(R.color.colorGreen);
				//statusLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGreen));
				statusTextTitle.setText(getActivity().getString(R.string.protect));
                statusTextSummary.setText(getActivity().getString(R.string.protected_summary_one)
                        + " " + Integer.toString(unlockLimit) + " "
                        + getActivity().getString(R.string.protected_summary_two));
			}

    		button.setText(getActivity().getString(R.string.disable));
            checkBoxHideWarning.setChecked(settings.getBoolean("hideWarning", false));
            checkBoxSafeMode.setChecked(settings.getBoolean("safeMode", false));
            checkBoxHideWarning.setEnabled(false);
            checkBoxSafeMode.setEnabled(false);
    		lockProgress.setEnabled(false);
    		
    	}else{
            animateStatusLayoutColor(R.color.colorRed);
    		statusTextTitle.setText(getActivity().getString(R.string.not_protected));
    		statusTextSummary.setText(getActivity().getString(R.string.not_protected_summary));
    		button.setText(getActivity().getString(R.string.enable));
            lockProgress.setEnabled(true);
            if (!checkBoxSafeMode.isChecked()) {
                checkBoxHideWarning.setEnabled(true);
            }
            checkBoxSafeMode.setEnabled(true);
    	}
	}

    private void animateStatusLayoutColor(int colorToId){
        ColorDrawable viewColor = (ColorDrawable) statusLayout.getBackground();
        int colorFrom = viewColor.getColor();
        int colorTo = ContextCompat.getColor(getActivity(), colorToId);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);
        colorAnimation.setRepeatCount(0);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                statusLayout.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }
	
	private void adjustAdminUI(boolean adminState){
			checkBox.setChecked(adminState);
	    	button.setEnabled(adminState);
	    	if (!adminState){
	    		editor.putBoolean("lockEnabled", false);
	    		editor.commit();
	    	}
	    	updateLockStatus();
	}

	
	 public void toggleLockProtection(){
         if (settings.getBoolean("lockEnabled", false)){
			 showDisableProtectionDialog();

		 }else{
			 showEnableProtectionDialog();
		 }
	 }
	 
	 private void enableLockProtection(){
         if (dam.isActiveAdmin()) {
             if (dam.enableLockScreenProtection(lockProgress.getProgress(), checkBoxHideWarning.isChecked(), checkBoxSafeMode.isChecked())) {
                 editor.putInt("unlockLimit", lockProgress.getProgress());
                 editor.putBoolean("lockEnabled", true);
                 editor.putBoolean("hideWarning", checkBoxHideWarning.isChecked());
                 editor.putBoolean("safeMode", checkBoxSafeMode.isChecked());
                 editor.commit();
             } else {
                 checkBoxHideWarning.setChecked(false);
                 checkBoxSafeMode.setChecked(false);
             }
         }
		 updateAdminCheck();
	 }
	 
	 private void showEnableProtectionDialog() {
		    mStackLevel++;

		    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
		    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("EnableLockProtectionDialog");
		    if (prev != null) {
		        ft.remove(prev);
		    }
		    ft.addToBackStack(null);
			ft.commit();
            DialogFragment dialogFrag = EnableLockProtectionDialog.newInstance();
            dialogFrag.setTargetFragment(this, ENABLE_PROTECTION_DIALOG_FRAGMENT);
            dialogFrag.show(getFragmentManager().beginTransaction(), "EnableLockProtectionDialog");
		    }
	 
	 private void disableLockProtection(){
		 if (dam.removeAdminRole()){
             editor.putBoolean("lockEnabled", false);
             editor.commit();
             checkBox.setChecked(false);
             adjustAdminUI(false);
         }
	 }
	 
	 private void showDisableProtectionDialog() {
        mStackLevel++;

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("DisableLockProtectionDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
		 ft.commit();
         DialogFragment dialogFrag = DisableLockProtectionDialog.newInstance();
         dialogFrag.setTargetFragment(this, DISABLE_PROTECTION_DIALOG_FRAGMENT);
         dialogFrag.show(getFragmentManager().beginTransaction(), "DisableLockProtectionDialog");
	 }

}