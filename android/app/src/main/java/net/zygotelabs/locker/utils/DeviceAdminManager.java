package net.zygotelabs.locker.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import net.zygotelabs.locker.DeviceAdmin;
import net.zygotelabs.locker.R;


public class DeviceAdminManager {
    private Context context;
    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;

    public DeviceAdminManager(Context context) {
        this.context = context;
        mDeviceAdmin = new ComponentName(context, DeviceAdmin.class);
        mDPM = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
    }


    public boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdmin);
    }

    public Intent getStartAdminEnableIntent(){
        // Launch the activity to have the user enable our admin.
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.add_admin_extra_app_text));
       return intent;
    }

    public boolean removeAdminRole(){
        try {
            mDPM.removeActiveAdmin(mDeviceAdmin);
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean enableLockScreenProtection(int maxAttempts){
        try {
            mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, maxAttempts);
            return true;
        } catch (Exception ex){
            return false;
        }
    }
}
