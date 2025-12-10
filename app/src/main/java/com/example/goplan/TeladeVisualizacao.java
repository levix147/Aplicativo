package com.example.goplan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeladeVisualizacao extends AppCompatActivity {

    private static final String TAG = "TeladeVisualizacao";

    private RecyclerView recyclerViewEventos;
    private EventoAdapter eventoAdapter;
    private TarefaRepositorio tarefaRepositorio;
    private EditText editBusca;

    private List<Tarefa> todasAsTarefas = new ArrayList<>();
    private ListenerRegistration listenerDoFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telade_visualizacao);

        iniciarComponentes();
        configurarRecyclerView();
        configurarListenerDoFirestore();
        configurarListeners();
    }

    private void iniciarComponentes() {
        recyclerViewEventos = findViewById(R.id.recycler_view_eventos);
        editBusca = findViewById(R.id.edit_busca);
        tarefaRepositorio = new TarefaRepositorio();
    }

    private void configurarRecyclerView() {
        eventoAdapter = new EventoAdapter(new ArrayList<>());
        recyclerViewEventos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEventos.setAdapter(eventoAdapter);
    }

    private void configurarListeners() {
        findViewById(R.id.btnAdicionarEvento).setOnClickListener(v -> {
            startActivity(new Intent(TeladeVisualizacao.this, AdicionarAtividade.class));
        });

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLista(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarListenerDoFirestore() {
        listenerDoFirestore = tarefaRepositorio.getTarefasCollection()
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao ouvir as mudancas do Firestore", error);
                        return;
                    }
                    if (snapshot != null) {
                        todasAsTarefas = snapshot.toObjects(Tarefa.class);
                        filtrarLista(editBusca.getText().toString());
                    }
                });
    }

    private void filtrarLista(String consulta) {
        List<Tarefa> tarefasFiltradas;
        String consultaFormatada = consulta.toLowerCase().trim();

        if (consultaFormatada.isEmpty()) {
            tarefasFiltradas = new ArrayList<>(todasAsTarefas);
        } else {
            tarefasFiltradas = todasAsTarefas.stream()
                    .filter(tarefa -> tarefa.getTitulo().toLowerCase().contains(consultaFormatada))
                    .collect(Collectors.toList());
        }
        eventoAdapter.atualizarLista(tarefasFiltradas);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerDoFirestore != null) {
            listenerDoFirestore.remove();
        }
    }
}
