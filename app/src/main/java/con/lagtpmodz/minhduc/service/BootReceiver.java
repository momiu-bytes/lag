package con.lagtpmodz.minhduc.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("TP_MODZ_LOGIN_PREFS", Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

            if (isLoggedIn) {
                Intent serviceIntent = new Intent(context, OverlayService.class);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
