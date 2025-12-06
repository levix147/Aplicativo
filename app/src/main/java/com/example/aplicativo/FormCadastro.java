package com.example.aplicativo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.EditText;
import androidx.appcompat.widget.AppCompatButton;
import android.view.MotionEvent;
import android.text.InputType;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class FormCadastro extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        TextView jaTenhoConta = findViewById(R.id.text_ja_tenho_conta);
        View btnVoltar = findViewById(R.id.bt_voltar);

        EditText editNome = findViewById(R.id.edit_nome);
        EditText editEmail = findViewById(R.id.edit_email);
        EditText editSenha = findViewById(R.id.edit_senha);
        EditText editConfirmar = findViewById(R.id.edit_confirma_senha);
        AppCompatButton btCadastrar = findViewById(R.id.bt_cadastrar);

        // Lógica para mostrar/ocultar senha (simples, via touch)
        editSenha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editSenha.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                        event.getRawX() >= (editSenha.getRight() - editSenha.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                    if (editSenha.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        editSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        editSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    editSenha.setSelection(editSenha.length());
                    return true;
                }
            }
            return false;
        });

        View.OnClickListener voltarListener = v -> {
            Intent intent = new Intent(FormCadastro.this, TelaPrincipal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        };

        jaTenhoConta.setOnClickListener(voltarListener);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(voltarListener);
        }

        btCadastrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();
            String confirmar = editConfirmar.getText().toString().trim();

            if (nome.isEmpty()) {
                editNome.setError("Digite seu nome");
                editNome.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                editEmail.setError("Digite seu email");
                editEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Email inválido");
                editEmail.requestFocus();
                return;
            }

            if (senha.isEmpty()) {
                editSenha.setError("Digite sua senha");
                editSenha.requestFocus();
                return;
            }

            if (!senha.equals(confirmar)) {
                editConfirmar.setError("As senhas não coincidem");
                editConfirmar.requestFocus();
                return;
            }

            // Salvar cadastro no SharedPreferences para simular banco de dados
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_email", email);
            editor.putString("user_password", senha);
            editor.putString("user_name", nome);
            editor.apply();

            Toast.makeText(FormCadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

            // Redirecionar para TelaPrincipal (Login) para o usuário entrar
            Intent intent = new Intent(FormCadastro.this, TelaPrincipal.class);
            startActivity(intent);
            finish();
        });
    }
}