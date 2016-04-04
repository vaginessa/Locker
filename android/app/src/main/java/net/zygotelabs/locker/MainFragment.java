package net.zygotelabs.locker;

import net.zygotelabs.locker.dialogs.DisableLockProtectionDialog;
import net.zygotelabs.locker.dialogs.EnableLockProtectionDialog;
import net.zygotelabs.locker.utils.DeviceAdminManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private Button button;
    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;
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
        
		mDeviceAdmin = new ComponentName(getActivity(), DeviceAdmin.class);
		mDPM = (DevicePolicyManager)getActivity().getSystemService(getActivity().DEVICE_POLICY_SERVICE);
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
		button = (Button) rootView.findViewById(R.id.buttonApply);
		statusLayout = (RelativeLayout) rootView.findViewById(R.id.top_layout);
		statusTextTitle = (TextView) rootView.findViewById(R.id.textViewTopTitle);
		statusTextSummary = (TextView) rootView.findViewById(R.id.textViewTopTitleSummary);
		seekTextValue = (TextView) rootView.findViewById(R.id.textViewLockerCount);
		lockProgress = (SeekBar) rootView.findViewById(R.id.seekBarLocker);
		
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
	    	
	    	// Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getActivity().getString(R.string.add_admin_extra_app_text));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            
	    }else {
	    	 mDPM.removeActiveAdmin(mDeviceAdmin);
	    	 adjustAdminUI(false);
	    }
	}
	
	private void updateAdminCheck(){   
		adjustAdminUI(dam.isActiveAdmin());
    	lockProgress.setProgress(settings.getInt("unlockLimit", 5));
    	
	}
	
	private void updateLockStatus(){
		boolean isProtected = settings.getBoolean("lockEnabled", false);
		
    	if (isProtected){
			statusLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGreen));
    		statusTextTitle.setText(getActivity().getString(R.string.protect));
    		int unlockLimit = settings.getInt("unlockLimit", 5);
    		statusTextSummary.setText(getActivity().getString(R.string.protected_summary_one)
    				+ " " + Integer.toString(unlockLimit) + " "
    				+ getActivity().getString(R.string.protected_summary_two));
    		button.setText(getActivity().getString(R.string.disable));
    		lockProgress.setEnabled(false);
    		
    	}else{
    		statusLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
    		statusTextTitle.setText(getActivity().getString(R.string.not_protected));
    		statusTextSummary.setText(getActivity().getString(R.string.not_protected_summary));
    		button.setText(getActivity().getString(R.string.enable));
    		lockProgress.setEnabled(true);
    	}
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
		 mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, lockProgress.getProgress());
		 editor.putInt("unlockLimit",  lockProgress.getProgress());
		 editor.putBoolean("lockEnabled", true);
		 editor.commit();
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
            DialogFragment dialogFrag = EnableLockProtectionDialog.newInstance(100);
            dialogFrag.setTargetFragment(this, ENABLE_PROTECTION_DIALOG_FRAGMENT);
            dialogFrag.show(getFragmentManager().beginTransaction(), "EnableLockProtectionDialog");
		    }
	 
	 private void disableLockProtection(){
		 editor.putBoolean("lockEnabled", false);
		 editor.commit();
		 mDPM.removeActiveAdmin(mDeviceAdmin);
		 updateAdminCheck();
		 checkBox.setChecked(false);
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
         DialogFragment dialogFrag = DisableLockProtectionDialog.newInstance(101);
         dialogFrag.setTargetFragment(this, DISABLE_PROTECTION_DIALOG_FRAGMENT);
         dialogFrag.show(getFragmentManager().beginTransaction(), "DisableLockProtectionDialog");
	 }

}