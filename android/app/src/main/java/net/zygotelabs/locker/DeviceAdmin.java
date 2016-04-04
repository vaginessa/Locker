package net.zygotelabs.locker;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.zygotelabs.locker.utils.DeviceAdminManager;

public class DeviceAdmin extends DeviceAdminReceiver {

    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled));
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_disabled));
    }

    @Override
    public void onPasswordFailed(Context ctxt, Intent intent) {
        DeviceAdminManager dam = new DeviceAdminManager(ctxt);
        dam.failedUnlockAttemptOccurred();
    }

}
