package com.example.aplicativo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TeladeVisualizacao extends AppCompatActivity {

    private LinearLayout colunaAFazer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_telade_visualizacao);


        colunaAFazer = findViewById(R.id.coluna_a_fazer);


        Intent intentRecebido = getIntent();
        if (intentRecebido != null && intentRecebido.getExtras() != null) {
            String novoTitulo = intentRecebido.getStringExtra("NOVO_TITULO");
            String novaData = intentRecebido.getStringExtra("NOVA_DATA");


            if (novoTitulo != null && !novoTitulo.isEmpty()) {
                adicionarCartao(novoTitulo, novaData);
            }
        }


        FloatingActionButton btnAdicionarEvento = findViewById(R.id.btnAdicionarEvento);
        btnAdicionarEvento.setOnClickListener(v -> {
            Intent intent = new Intent(TeladeVisualizacao.this, AdicionarAtividade.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            return insets;
        });
    }


    private void adicionarCartao(String titulo, String data) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View novoCartao = inflater.inflate(R.layout.item_card_kanban, colunaAFazer, false);

        TextView txtTitulo = novoCartao.findViewById(R.id.txtTitulo);

        txtTitulo.setText(titulo);

        colunaAFazer.addView(novoCartao, 1);
    }
}