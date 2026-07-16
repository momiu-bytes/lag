package con.lagtpmodz.minhduc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import con.lagtpmodz.minhduc.service.OverlayService;

public class MainActivity extends Activity {

    private SharedPreferences sharedPreferences;
    private LinearLayout layoutPermission;
    private LinearLayout layoutDashboard;
    private Button btnGrantPermission;
    private TextView txtBackToLogin;
    private ImageView imgLogout;
    private TextView txtServiceStatus;
    private TextView txtToggleStatus;
    private Switch switchOverlay;

    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("TP_MODZ_LOGIN_PREFS", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            goToLogin();
            return;
        }

        layoutPermission = findViewById(R.id.layoutPermission);
        layoutDashboard = findViewById(R.id.layoutDashboard);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        imgLogout = findViewById(R.id.imgLogout);
        txtServiceStatus = findViewById(R.id.txtServiceStatus);
        txtToggleStatus = findViewById(R.id.txtToggleStatus);
        switchOverlay = findViewById(R.id.switchOverlay);

        btnGrantPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                } else {
                    Toast.makeText(MainActivity.this, "Hệ điều hành không hỗ trợ vẽ đè", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        switchOverlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startFloatingService();
                } else {
                    stopFloatingService();
                }
                refreshUIState();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            refreshUIState();
            // Auto start service if permitted and not already running
            if (hasOverlayPermission() && !OverlayService.isServiceRunning) {
                startFloatingService();
                refreshUIState();
            }
        }
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void refreshUIState() {
        if (hasOverlayPermission()) {
            layoutPermission.setVisibility(View.GONE);
            layoutDashboard.setVisibility(View.VISIBLE);

            if (OverlayService.isServiceRunning) {
                switchOverlay.setChecked(true);
                txtServiceStatus.setText("Đã khởi động dịch vụ nút nổi.");
                txtToggleStatus.setText("Đang hiển thị trên màn hình");
                txtToggleStatus.setTextColor(0xFF00E5FF); // Cyan
            } else {
                switchOverlay.setChecked(false);
                txtServiceStatus.setText("Dịch vụ nút nổi chưa kích hoạt.");
                txtToggleStatus.setText("Đang tạm dừng");
                txtToggleStatus.setTextColor(0xFF808080); // Gray
            }
        } else {
            layoutPermission.setVisibility(View.VISIBLE);
            layoutDashboard.setVisibility(View.GONE);
        }
    }

    private void startFloatingService() {
        if (hasOverlayPermission()) {
            Intent intent = new Intent(this, OverlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            Toast.makeText(this, "Chưa cấp quyền Vẽ lên ứng dụng khác!", Toast.LENGTH_SHORT).show();
            switchOverlay.setChecked(false);
        }
    }

    private void stopFloatingService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
    }

    private void logout() {
        sharedPreferences.edit().putBoolean("is_logged_in", false).apply();
        stopFloatingService();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            refreshUIState();
            if (hasOverlayPermission()) {
                startFloatingService();
                refreshUIState();
            }
        }
    }
}
