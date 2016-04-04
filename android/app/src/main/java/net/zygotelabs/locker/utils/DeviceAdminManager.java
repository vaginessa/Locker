package net.zygotelabs.locker.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.zygotelabs.locker.DeviceAdmin;
import net.zygotelabs.locker.R;


public class DeviceAdminManager {
    private Context context;
    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;
    private SharedPreferences settings;

    public DeviceAdminManager(Context context) {
        this.context = context;
        mDeviceAdmin = new ComponentName(context, DeviceAdmin.class);
        mDPM = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
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

    public boolean enableLockScreenProtection(int maxAttempts, boolean hideWarning){
        if (!hideWarning) {
            try {
                mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, maxAttempts);
                return true;
            } catch (Exception ex) {
                return false;
            }
        } else {
            /**
             * Due to an AOSP bug (https://code.google.com/p/android/issues/detail?id=79971)
             * checking the number of failed unlock attempts will fail on Android 5.0 devices.
             * Therefore we verify that functionality here before enabling the protection.
             * If it fails we inform the user.
             */
            try {
                getNumberOfFailedUnlockAttempts();
            } catch (Exception ex){
                Toast.makeText(context, context.getString(R.string.hide_lockscreen_warning_error), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private int getNumberOfFailedUnlockAttempts(){
        return mDPM.getCurrentFailedPasswordAttempts();
    }

    public void failedUnlockAttemptOccurred(){
        /**
         * Failed unlock attempt occurred. Check number of
         * failed unlock attempts and wipe the device if necessary.
          */

        if (isProtected()){
            if (getNumberOfFailedUnlockAttempts() >= settings.getInt("unlockLimit", 5)){
                mDPM.wipeData(0);
            }
        }

    }

    public boolean isProtected(){
        return settings.getBoolean("lockEnabled", false);
    }
}
