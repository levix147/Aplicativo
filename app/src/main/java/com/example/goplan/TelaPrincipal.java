package com.example.goplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class TelaPrincipal extends AppCompatActivity {

    private static final String TAG = "TelaPrincipal";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private EditText editEmail, editSenha;
    private AppCompatButton btnEntrar;
    private FrameLayout btnGoogle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        mAuth = FirebaseAuth.getInstance();

        iniciarComponentes();
        configurarGoogleSignIn();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthComGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(this, "Falha no login com Google.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        configurarListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irParaTelaDeVisualizacao();
        }
    }

    private void iniciarComponentes() {
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        btnEntrar = findViewById(R.id.bt_entrar);
        btnGoogle = findViewById(R.id.btn_google);
        progressBar = findViewById(R.id.progressbar);
    }

    private void configurarListeners() {
        btnGoogle.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            iniciarLoginComGoogle();
        });

        btnEntrar.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Login com e-mail/senha não implementado.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        });

        TextView textTelaCadastro = findViewById(R.id.text_telacadastro);
        textTelaCadastro.setOnClickListener(v -> {
             startActivity(new Intent(TelaPrincipal.this, FormCadastro.class));
        });
    }

    private void configurarGoogleSignIn() {
        // CORRECAO: Usando a chave do BuildConfig em vez do R.string
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void iniciarLoginComGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthComGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        irParaTelaDeVisualizacao();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(TelaPrincipal.this, "Falha na autenticação.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irParaTelaDeVisualizacao() {
        Intent intent = new Intent(TelaPrincipal.this, TeladeVisualizacao.class);
        startActivity(intent);
        finish();
    }
}
