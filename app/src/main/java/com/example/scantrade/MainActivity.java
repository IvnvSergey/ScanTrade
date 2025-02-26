package com.example.scantrade;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private boolean isAdmin = false;
    private final String ADMIN_CODE = "1234";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String name = getIntent().getStringExtra("name");
        if (name == null) {
            name = "Неизвестно";
        }
        Log.d(TAG, "Получено имя: " + name);

        TextView positionText = findViewById(R.id.position_text);
        TextView nameText = findViewById(R.id.name_text);

        positionText.setText("Сотрудник: ");
        nameText.setText(name);

        setupDriveService();

        View scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(v ->
                Toast.makeText(this, "Функционал сканирования пока не реализован.", Toast.LENGTH_SHORT).show());

        View receiveButton = findViewById(R.id.receive_button);
        receiveButton.setOnClickListener(v ->
                Toast.makeText(this, "Функционал приёмки пока не реализован.", Toast.LENGTH_SHORT).show());

        View writeOffButton = findViewById(R.id.write_off_button);
        writeOffButton.setOnClickListener(v ->
                Toast.makeText(this, "Функционал списания пока не реализован.", Toast.LENGTH_SHORT).show());

        View productsButton = findViewById(R.id.products_button);
        productsButton.setOnClickListener(v ->
                Toast.makeText(this, "Функционал товаров пока не реализован.", Toast.LENGTH_SHORT).show());

        View userInfoLayout = findViewById(R.id.user_info_layout);
        userInfoLayout.setOnClickListener(v -> showAdminCodeDialog());
    }

    private void setupDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton("https://www.googleapis.com/auth/drive"));
            credential.setSelectedAccount(account.getAccount());
            Drive driveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential
            ).setApplicationName("ScanTrade").build();
            Log.d(TAG, "Подключено к Google Drive для: " + account.getEmail());
            Toast.makeText(this, "Подключено к Google Drive", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Не авторизован в Google");
            Toast.makeText(this, "Не авторизован", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updatePositionText() {
        TextView positionText = findViewById(R.id.position_text);
        positionText.setText(isAdmin ? "Администратор: " : "Сотрудник: ");
    }

    private void showAdminCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите код администратора")
                .setMessage("Пожалуйста, введите код для переключения в режим администратора.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredCode = input.getText().toString();
            if (enteredCode.equals(ADMIN_CODE)) {
                isAdmin = true;
                updatePositionText();
                Toast.makeText(this, "Вы вошли как администратор", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверный код", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", null);

        builder.show();
    }
}