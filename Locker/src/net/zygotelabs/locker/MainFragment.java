package net.zygotelabs.locker;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class MainFragment extends Fragment  {
	CheckBox checkBox;
	ComponentName mDeviceAdmin;
	DevicePolicyManager mDPM;
	boolean mAdminActive;
	
	protected static final int REQUEST_CODE_ENABLE_ADMIN=1;
	
	public MainFragment() {
		
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Left", "onCreate()");
		mDeviceAdmin = new ComponentName(getActivity(), DeviceAdmin.class);
		mDPM=(DevicePolicyManager)getActivity().getSystemService(getActivity().DEVICE_POLICY_SERVICE);

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		checkBox = (CheckBox) rootView.findViewById(R.id.checkBoxAdmin);
		updateAdminCheck();
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	    super.onActivityResult(requestCode, resultCode, data);
	    updateAdminCheck();
	    
	}
	
	public void onCheckBoxClicked(boolean checked){

	    if (checked){
	    	// Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getActivity().getString(R.string.add_admin_extra_app_text));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            //disable check - don't update checkbox until we're really active
            //checkBox.setChecked(false);
            
	    }else{
	    	 mDPM.removeActiveAdmin(mDeviceAdmin);
	    }
	}
	
	private void updateAdminCheck(){
	    if (isActiveAdmin()){
	    	checkBox.setChecked(true);
	    }else{
    		checkBox.setChecked(false);
	    }
	}
	
	 private boolean isActiveAdmin() {
	        return mDPM.isAdminActive(mDeviceAdmin);
	    }
	
	
}
