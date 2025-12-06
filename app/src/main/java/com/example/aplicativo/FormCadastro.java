package com.example.aplicativo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.widget.EditText;
import androidx.appcompat.widget.AppCompatButton;
import android.view.MotionEvent;
import android.text.InputType;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class FormCadastro extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        View root = findViewById(R.id.main);

        TextView jaTenhoConta = findViewById(R.id.text_ja_tenho_conta);

        EditText editNome = findViewById(R.id.edit_nome);
        EditText editEmail = findViewById(R.id.edit_email);
        EditText editSenha = findViewById(R.id.edit_senha);
        EditText editConfirmar = findViewById(R.id.edit_confirma_senha);
        AppCompatButton btCadastrar = findViewById(R.id.bt_cadastrar);

        editSenha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editSenha.getRight() -
                        editSenha.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

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

        jaTenhoConta.setOnClickListener(v -> {
            Intent intent = new Intent(FormCadastro.this, TelaPrincipal.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        btCadastrar.setOnClickListener(v -> {

            if (editNome.getText().toString().isEmpty()) {
                editNome.setError("Digite seu nome");
                return;
            }

            if (editEmail.getText().toString().isEmpty()) {
                editEmail.setError("Digite seu email");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.getText()).matches()) {
                editEmail.setError("Email inválido");
                return;
            }

            if (editSenha.getText().toString().isEmpty()) {
                editSenha.setError("Digite sua senha");
                return;
            }

            if (!editSenha.getText().toString().equals(editConfirmar.getText().toString())) {
                editConfirmar.setError("As senhas não coincidem");
                return;
            }

            Intent intent = new Intent(FormCadastro.this, TelaPrincipal.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}