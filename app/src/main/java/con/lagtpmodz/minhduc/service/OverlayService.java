package con.lagtpmodz.minhduc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import con.lagtpmodz.minhduc.R;

public class OverlayService extends Service {

    public static boolean isServiceRunning = false;

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    private LinearLayout bubbleLayout;
    private LinearLayout panelLayout;

    private TextView[] passcodeSlots = new TextView[7];
    private StringBuilder enteredDigits = new StringBuilder();

    private TextView txtHttpStatus;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String CHANNEL_ID = "OverlayServiceChannel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;

        createNotificationChannel();
        startForeground(1, buildForegroundNotification());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 100;

        bubbleLayout = overlayView.findViewById(R.id.bubble_layout);
        panelLayout = overlayView.findViewById(R.id.panel_layout);

        setupPasscodeSlots();
        setupKeyboard();
        setupDragAndCollapse();

        windowManager.addView(overlayView, params);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Dịch vụ nút nổi Lag TP×MDUC",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification buildForegroundNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("Lag TP×MDUC")
                .setContentText("Dịch vụ nút nổi đang chạy...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }

    private void setupPasscodeSlots() {
        passcodeSlots[0] = overlayView.findViewById(R.id.slot0);
        passcodeSlots[1] = overlayView.findViewById(R.id.slot1);
        passcodeSlots[2] = overlayView.findViewById(R.id.slot2);
        passcodeSlots[3] = overlayView.findViewById(R.id.slot3);
        passcodeSlots[4] = overlayView.findViewById(R.id.slot4);
        passcodeSlots[5] = overlayView.findViewById(R.id.slot5);
        passcodeSlots[6] = overlayView.findViewById(R.id.slot6);
        updateSlotsUI();
    }

    private void updateSlotsUI() {
        for (int i = 0; i < 7; i++) {
            if (i < enteredDigits.length()) {
                passcodeSlots[i].setText(String.valueOf(enteredDigits.charAt(i)));
            } else {
                passcodeSlots[i].setText("");
            }
        }
    }

    private void setupKeyboard() {
        int[] buttonIds = {
                R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
                R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        };

        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredDigits.length() < 7) {
                    Button b = (Button) v;
                    enteredDigits.append(b.getText().toString());
                    updateSlotsUI();
                    clearHttpStatus();
                }
            }
        };

        for (int id : buttonIds) {
            overlayView.findViewById(id).setOnClickListener(numberClickListener);
        }

        overlayView.findViewById(R.id.keyDel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredDigits.length() > 0) {
                    enteredDigits.deleteCharAt(enteredDigits.length() - 1);
                    updateSlotsUI();
                    clearHttpStatus();
                }
            }
        });

        overlayView.findViewById(R.id.keyC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredDigits.setLength(0);
                updateSlotsUI();
                clearHttpStatus();
            }
        });

        txtHttpStatus = overlayView.findViewById(R.id.txtHttpStatus);
        Button btnSend = overlayView.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredDigits.length() < 7) {
                    showHttpStatus("Vui lòng nhập đủ 7 chữ số", 0xFFFF3D00); // red color
                    return;
                }
                sendHttpRequest(enteredDigits.toString());
            }
        });
    }

    private void clearHttpStatus() {
        txtHttpStatus.setVisibility(View.GONE);
        txtHttpStatus.setText("");
    }

    private void showHttpStatus(final String text, final int color) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                txtHttpStatus.setText(text);
                txtHttpStatus.setTextColor(color);
                txtHttpStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendHttpRequest(final String code) {
        showHttpStatus("Đang gửi...", 0xFF00E5FF); // Cyan
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    // Create exact URL keeping the curly brackets { }
                    String urlString = "http://192.168.1.8:2081/kick?tc={" + code + "}";
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        showHttpStatus("Đã gửi.", 0xFF00FF00); // Green
                    } else {
                        showHttpStatus("Không kết nối được máy chủ.", 0xFFFF3D00); // Red
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showHttpStatus("Không kết nối được máy chủ.", 0xFFFF3D00); // Red
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception ignored) {}
                    }
                }
            }
        }).start();
    }

    private void setupDragAndCollapse() {
        bubbleLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        float diffX = event.getRawX() - initialTouchX;
                        float diffY = event.getRawY() - initialTouchY;
                        // Distinguish between click and drag
                        if (Math.abs(diffX) < 10 && Math.abs(diffY) < 10) {
                            expandPanel();
                        }
                        return true;
                }
                return false;
            }
        });

        overlayView.findViewById(R.id.btnMinimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapsePanel();
            }
        });

        overlayView.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
    }

    private void expandPanel() {
        bubbleLayout.setVisibility(View.GONE);
        panelLayout.setVisibility(View.VISIBLE);

        // Allow panel input interaction
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(overlayView, params);
    }

    private void collapsePanel() {
        panelLayout.setVisibility(View.GONE);
        bubbleLayout.setVisibility(View.VISIBLE);

        // Return to click-through but touchable on bubble state
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(overlayView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        if (windowManager != null && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
