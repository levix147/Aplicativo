package com.example.aplicativo;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeladeVisualizacao extends AppCompatActivity {

    private static final String TAG = "TeladeVisualizacao";

    private LinearLayout colunaAFazer, colunaFazendo, colunaFeito;
    private TarefaRepositorio tarefaRepositorio;
    private EditText editBusca;

    private List<Tarefa> todasAsTarefas = new ArrayList<>();
    private ListenerRegistration listenerDoFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telade_visualizacao);

        // Inicializa as colunas e outros componentes
        iniciarComponentes();

        // Configura os listeners de Drag and Drop para as colunas
        configurarDragAndDropListeners();

        // Configura o listener do Firebase
        configurarListenerDoFirestore();

        // Configura o listener do botao de adicionar evento
        findViewById(R.id.btnAdicionarEvento).setOnClickListener(v -> {
            startActivity(new Intent(TeladeVisualizacao.this, AdicionarAtividade.class));
        });

        // Configura o listener do campo de busca
        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarInterface();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void iniciarComponentes() {
        colunaAFazer = findViewById(R.id.coluna_a_fazer);
        colunaFazendo = findViewById(R.id.coluna_fazendo);
        colunaFeito = findViewById(R.id.coluna_feito);
        editBusca = findViewById(R.id.edit_busca);
        tarefaRepositorio = new TarefaRepositorio();
    }

    private void configurarDragAndDropListeners() {
        colunaAFazer.setOnDragListener(new MeuDragListener("A_FAZER"));
        colunaFazendo.setOnDragListener(new MeuDragListener("FAZENDO"));
        colunaFeito.setOnDragListener(new MeuDragListener("CONCLUIDO")); // Use um nome consistente
    }

    private void configurarListenerDoFirestore() {
        listenerDoFirestore = tarefaRepositorio.getTarefasCollection()
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Erro ao ouvir as mudancas do Firestore", error);
                        return;
                    }
                    if (snapshot != null) {
                        todasAsTarefas = snapshot.toObjects(Tarefa.class);
                        atualizarInterface();
                    }
                });
    }

    private void atualizarInterface() {
        // Limpa todas as colunas (mantendo os cabecalhos)
        limparColunas();

        String consulta = editBusca.getText().toString().toLowerCase().trim();
        List<Tarefa> tarefasFiltradas;

        if (consulta.isEmpty()) {
            tarefasFiltradas = new ArrayList<>(todasAsTarefas);
        } else {
            tarefasFiltradas = todasAsTarefas.stream()
                    .filter(tarefa -> tarefa.getTitulo().toLowerCase().contains(consulta))
                    .collect(Collectors.toList());
        }

        // Adiciona os cartoes nas colunas corretas
        for (Tarefa tarefa : tarefasFiltradas) {
            adicionarCartaoNaColunaCerta(tarefa);
        }
    }

    private void limparColunas() {
        if (colunaAFazer.getChildCount() > 1) {
            colunaAFazer.removeViews(1, colunaAFazer.getChildCount() - 1);
        }
        if (colunaFazendo.getChildCount() > 1) {
            colunaFazendo.removeViews(1, colunaFazendo.getChildCount() - 1);
        }
        if (colunaFeito.getChildCount() > 1) {
            colunaFeito.removeViews(1, colunaFeito.getChildCount() - 1);
        }
    }

    private void adicionarCartaoNaColunaCerta(Tarefa tarefa) {
        switch (tarefa.getStatus()) {
            case "A_FAZER":
                adicionarCartao(tarefa, colunaAFazer);
                break;
            case "FAZENDO":
                adicionarCartao(tarefa, colunaFazendo);
                break;
            case "CONCLUIDO":
                adicionarCartao(tarefa, colunaFeito);
                break;
        }
    }

    private void adicionarCartao(Tarefa tarefa, LinearLayout coluna) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View novoCartao = inflater.inflate(R.layout.item_card_kanban, coluna, false);

        // Guarda a tarefa inteira na tag da view para referencia no Drag and Drop
        novoCartao.setTag(tarefa);

        TextView txtTitulo = novoCartao.findViewById(R.id.txtTitulo);
        TextView txtData = novoCartao.findViewById(R.id.txtData);

        txtTitulo.setText(tarefa.getTitulo());
        txtData.setText(tarefa.getData() + " " + tarefa.getHora());

        // Configura o clique longo para iniciar o modo de arrastar
        novoCartao.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag().toString());
            ClipData dragData = new ClipData(
                    (CharSequence) v.getTag().toString(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item);

            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(novoCartao);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(dragData, myShadow, v, 0);
            } else {
                v.startDrag(dragData, myShadow, v, 0);
            }
            v.setVisibility(View.INVISIBLE); // Torna a view original invisivel
            return true;
        });

        coluna.addView(novoCartao);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerDoFirestore != null) {
            listenerDoFirestore.remove();
        }
    }

    // Classe interna para lidar com os eventos de Drag and Drop
    private class MeuDragListener implements View.OnDragListener {
        private final String novoStatus;

        MeuDragListener(String novoStatus) {
            this.novoStatus = novoStatus;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View viewArrastada = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Nenhuma acao especifica necessaria aqui
                    return true;

                case DragEvent.ACTION_DROP:
                    Tarefa tarefaArrastada = (Tarefa) viewArrastada.getTag();

                    // Verifica se o status e realmente diferente para evitar escritas desnecessarias
                    if (!tarefaArrastada.getStatus().equals(novoStatus)) {
                        tarefaRepositorio.atualizarStatusTarefa(tarefaArrastada.getId(), novoStatus);
                        Toast.makeText(TeladeVisualizacao.this, "Tarefa movida para " + novoStatus, Toast.LENGTH_SHORT).show();
                    }
                    viewArrastada.setVisibility(View.VISIBLE); // Restaura a visibilidade da view original
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    // Se o drop nao foi bem sucedido (soltou fora de uma coluna valida),
                    // a view original precisa se tornar visivel novamente.
                    if (!event.getResult()) {
                        viewArrastada.setVisibility(View.VISIBLE);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    // Opcional: Mudar o background da coluna para indicar que e um alvo
                    v.setBackgroundColor(0x20000000); // Um cinza translucido
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // Opcional: Restaurar o background da coluna
                    v.setBackgroundResource(R.drawable.bg_coluna_kanban);
                    return true;

                default:
                    break;
            }
            return true;
        }
    }
}