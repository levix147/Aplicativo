package com.example.aplicativo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

    private FrameLayout btnGoogle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        mAuth = FirebaseAuth.getInstance();

        // Esconde o botão de entrar e campos de email/senha, pois vamos focar no login social
        // (Em um app real, você poderia manter ambos)
        findViewById(R.id.bt_entrar).setVisibility(View.GONE);
        findViewById(R.id.edit_email).setVisibility(View.GONE);
        findViewById(R.id.edit_senha).setVisibility(View.GONE);
        findViewById(R.id.text_esqueci_senha).setVisibility(View.GONE);

        btnGoogle = findViewById(R.id.btn_google);
        progressBar = findViewById(R.id.progressbar);

        configurarGoogleSignIn();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Login com Google bem-sucedido, agora autenticar com Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthComGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Falha no login com Google
                            Log.w(TAG, "Google sign in failed", e);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(this, "Falha no login com Google.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnGoogle.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            iniciarLoginComGoogle();
        });
        
        // Mover para a tela de cadastro (se houver)
        TextView textTelaCadastro = findViewById(R.id.text_telacadastro);
        textTelaCadastro.setOnClickListener(v -> {
            // Intent para a tela de cadastro, se existir
            // startActivity(new Intent(TelaPrincipal.this, FormCadastro.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Checar se o usuario ja esta logado. Se sim, vai para a tela de visualizacao.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irParaTelaDeVisualizacao();
        }
    }

    private void configurarGoogleSignIn() {
        // Configura o Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Essencial para Firebase
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
                        // Login com Firebase bem-sucedido
                        Log.d(TAG, "signInWithCredential:success");
                        irParaTelaDeVisualizacao();
                    } else {
                        // Se o login falhar
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(TelaPrincipal.this, "Falha na autenticação.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irParaTelaDeVisualizacao() {
        Intent intent = new Intent(TelaPrincipal.this, TeladeVisualizacao.class);
        startActivity(intent);
        finish(); // Finaliza a tela principal para nao voltar para ela ao apertar "voltar"
    }
}
