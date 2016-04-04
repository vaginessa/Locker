package net.zygotelabs.locker.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import net.zygotelabs.locker.DeviceAdmin;

/**
 * Created by Zygote on 04.04.2016.
 */
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
}
