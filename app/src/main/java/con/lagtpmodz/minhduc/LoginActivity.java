package con.lagtpmodz.minhduc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private EditText keyInput;
    private TextView errorText;
    private Button btnOk;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("TP_MODZ_LOGIN_PREFS", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            startMainActivity();
            return;
        }

        keyInput = findViewById(R.id.keyInput);
        errorText = findViewById(R.id.errorText);
        btnOk = findViewById(R.id.btnOk);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = keyInput.getText().toString().trim();
                if ("123".equals(input)) {
                    errorText.setVisibility(View.GONE);
                    sharedPreferences.edit().putBoolean("is_logged_in", true).apply();
                    startMainActivity();
                } else {
                    errorText.setText("Sai key");
                    errorText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
