package com.example.aplicativo;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TelaPrincipal extends AppCompatActivity {

    private TextView text_tela_cadastro;
    private EditText edit_email, edit_senha;
    private AppCompatButton bt_entrar;
    private ImageView btn_voltar;

    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_principal);

        IniciarComponentes();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configurarVisibilidadeSenha();


        btn_voltar.setOnClickListener(v -> {



            Intent intent = new Intent(TelaPrincipal.this, TeladeVisualizacao.class);
            startActivity(intent);
            finish();
        });

        text_tela_cadastro.setOnClickListener(v -> {
            startActivity(new Intent(TelaPrincipal.this, FormCadastro.class));
        });

        bt_entrar.setOnClickListener(v -> {
            String email = edit_email.getText().toString().trim();
            String senha = edit_senha.getText().toString().trim();

            if (email.isEmpty()) {
                edit_email.setError("Preencha o e-mail");
                return;
            }

            if (senha.isEmpty()) {
                edit_senha.setError("Preencha a senha");
                return;
            }

            Intent intent = new Intent(TelaPrincipal.this, TeladeVisualizacao.class);
            startActivity(intent);
            finish();
        });
    }

    private void configurarVisibilidadeSenha() {
        edit_senha.setTransformationMethod(PasswordTransformationMethod.getInstance());

        edit_senha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (edit_senha.getCompoundDrawables() == null) return false;
                if (edit_senha.getCompoundDrawables()[DRAWABLE_END] == null) return false;

                int drawableWidth = edit_senha.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                float touchX = event.getX();
                float width = edit_senha.getWidth();
                float paddingRight = edit_senha.getPaddingRight();

                final float extraTapArea = (8 * getResources().getDisplayMetrics().density);
                float startXOfDrawable = width - paddingRight - drawableWidth - extraTapArea;

                if (touchX >= startXOfDrawable) {
                    TransformationMethod current = edit_senha.getTransformationMethod();

                    if (senhaVisivel) {
                        edit_senha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        edit_senha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                    } else {
                        edit_senha.setTransformationMethod(null);
                        edit_senha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                    }

                    senhaVisivel = !senhaVisivel;
                    edit_senha.setSelection(edit_senha.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void IniciarComponentes() {
        text_tela_cadastro = findViewById(R.id.text_telacadastro);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_entrar = findViewById(R.id.bt_entrar);
        btn_voltar = findViewById(R.id.btn_voltar);
    }
}