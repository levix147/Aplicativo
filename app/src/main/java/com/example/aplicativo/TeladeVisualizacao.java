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

import java.util.List;

public class TeladeVisualizacao extends AppCompatActivity {

    private LinearLayout colunaAFazer;
    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_telade_visualizacao);

        colunaAFazer = findViewById(R.id.coluna_a_fazer);
        taskRepository = new TaskRepository(this);

        FloatingActionButton btnAdicionarEvento = findViewById(R.id.btnAdicionarEvento);
        btnAdicionarEvento.setOnClickListener(v -> {
            Intent intent = new Intent(TeladeVisualizacao.this, AdicionarAtividade.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas();
    }

    private void carregarTarefas() {
        // Limpar colunas (no momento so temos A Fazer implementada visualmente para receber cards)
        // O ideal seria limpar todas as colunas se fossemos exibir status diferentes
        
        // Remove views extras (mantendo o cabeçalho "A Fazer" que é o index 0)
        // Nota: O layout XML tem um LinearLayout interno com titulo. Vamos remover tudo apos o index 0.
        // Verificando o layout XML: O primeiro filho de coluna_a_fazer é o header (LinearLayout horizontal).
        // Então removemos do index 1 em diante.
        if (colunaAFazer.getChildCount() > 1) {
            colunaAFazer.removeViews(1, colunaAFazer.getChildCount() - 1);
        }

        List<Task> tasks = taskRepository.getTasks();
        for (Task task : tasks) {
            if ("A_FAZER".equals(task.getStatus())) {
                adicionarCartao(task);
            }
        }
    }

    private void adicionarCartao(Task task) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View novoCartao = inflater.inflate(R.layout.item_card_kanban, colunaAFazer, false);

        TextView txtTitulo = novoCartao.findViewById(R.id.txtTitulo);
        TextView txtData = novoCartao.findViewById(R.id.txtData);
        TextView txtTag = novoCartao.findViewById(R.id.txtTag); // Opcional, se quiser mudar texto da tag

        txtTitulo.setText(task.getTitulo());
        txtData.setText(task.getData() + " " + task.getHora());

        // Adiciona ao final da lista
        colunaAFazer.addView(novoCartao);
    }
}