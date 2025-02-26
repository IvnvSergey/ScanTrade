package com.example.scantrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthorizationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private static final String TAG = "FirebaseAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        // Инициализация Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Настройка Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("51869228720-ic5jsbto1um4l4r38jhtdk06gj5i94tr.apps.googleusercontent.com") // Замените на ваш Web Client ID из Firebase
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Проверка предыдущей авторизации
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Уже авторизован: " + currentUser.getEmail());
            proceedToMainActivity(currentUser.getDisplayName());
            return;
        }

        // Инициализация кнопки
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> {
            Log.d(TAG, "Запуск процесса входа");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });

        // Обработка результата
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Log.d(TAG, "Результат входа получен, resultCode: " + resultCode);
                    if (resultCode == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        Log.w(TAG, "Вход отменён, resultCode: " + resultCode);
                        Toast.makeText(this, "Вход отменён: код " + resultCode, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In успешен: " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.e(TAG, "Ошибка Google Sign-In, код: " + e.getStatusCode(), e);
            Toast.makeText(this, "Ошибка входа: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase авторизация успешна");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            proceedToMainActivity(user.getDisplayName());
                        }
                    } else {
                        Log.w(TAG, "Ошибка Firebase авторизации", task.getException());
                        Toast.makeText(this, "Ошибка Firebase: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void proceedToMainActivity(String name) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }
}