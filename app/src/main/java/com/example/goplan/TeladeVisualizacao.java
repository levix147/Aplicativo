package com.example.goplan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeladeVisualizacao extends AppCompatActivity implements EventoAdapter.OnItemInteractionListener {

    private static final String TAG = "TeladeVisualizacao";

    private RecyclerView recyclerViewEventos;
    private EventoAdapter eventoAdapter;
    private TarefaRepositorio tarefaRepositorio;
    private EditText editBusca;
    private ImageView btnPerfil, btnJoinEvent;

    private List<Tarefa> todasAsTarefas = new ArrayList<>();
    private ListenerRegistration listenerDoFirestore;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telade_visualizacao);

        mAuth = FirebaseAuth.getInstance();
        tarefaRepositorio = new TarefaRepositorio();

        // Configura o Google Sign-In Client para o logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        iniciarComponentes();
        configurarRecyclerView();
        configurarListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        atualizarUI(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerDoFirestore != null) {
            listenerDoFirestore.remove();
        }
    }

    private void iniciarComponentes() {
        recyclerViewEventos = findViewById(R.id.recycler_view_eventos);
        editBusca = findViewById(R.id.edit_busca);
        btnPerfil = findViewById(R.id.btn_logout);
        btnJoinEvent = findViewById(R.id.btn_join_event);
    }

    private void configurarRecyclerView() {
        eventoAdapter = new EventoAdapter(new ArrayList<>(), this);
        recyclerViewEventos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEventos.setAdapter(eventoAdapter);
    }

    private void configurarListeners() {
        findViewById(R.id.btnAdicionarEvento).setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                startActivity(new Intent(TeladeVisualizacao.this, AdicionarAtividade.class));
            } else {
                pedirLogin();
            }
        });

        btnPerfil.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                // Logout completo
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    atualizarUI(null);
                    Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show();
                });
            } else {
                startActivity(new Intent(TeladeVisualizacao.this, LoginActivity.class));
            }
        });

        btnJoinEvent.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                mostrarDialogoJuntarEvento();
            } else {
                pedirLogin();
            }
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

    private void configurarListenerDoFirestore(FirebaseUser user) {
        if (listenerDoFirestore != null) {
            listenerDoFirestore.remove();
        }

        if (user != null) {
            String userId = user.getUid();
            Query query = tarefaRepositorio.getTarefasCollection().whereArrayContains("membros", userId);

            listenerDoFirestore = query.addSnapshotListener(this, (snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Erro ao ouvir as mudancas do Firestore", error);
                    return;
                }
                if (snapshot != null) {
                    todasAsTarefas = snapshot.toObjects(Tarefa.class);
                    filtrarLista(editBusca.getText().toString());
                } else {
                    Log.d(TAG, "Snapshot nulo recebido");
                }
            });
        } else {
            todasAsTarefas.clear();
            eventoAdapter.atualizarLista(new ArrayList<>());
        }
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
    public void onEditarClick(Tarefa tarefa) {
        if (isUserLoggedIn() && tarefa.getUserId().equals(mAuth.getUid())) {
            Intent intent = new Intent(this, AdicionarAtividade.class);
            intent.putExtra("TAREFA_PARA_EDITAR", tarefa);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Você não tem permissão para editar este evento.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onExcluirClick(Tarefa tarefa) {
        if (isUserLoggedIn() && tarefa.getUserId().equals(mAuth.getUid())) {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir Evento")
                    .setMessage("Tem certeza que deseja excluir '" + tarefa.getTitulo() + "'?")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        tarefaRepositorio.excluirTarefa(tarefa.getId()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Evento excluído com sucesso", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Falha ao excluir evento", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Erro ao excluir tarefa", task.getException());
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            Toast.makeText(this, "Você não tem permissão para excluir este evento.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void pedirLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Login Necessário")
                .setMessage("Você precisa fazer login para realizar esta ação.")
                .setPositiveButton("Fazer Login", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void atualizarUI(FirebaseUser user) {
        configurarListenerDoFirestore(user);
    }

    private void mostrarDialogoJuntarEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Juntar-se a um Evento");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Digite o código do convite");
        builder.setView(input);

        builder.setPositiveButton("Entrar", (dialog, which) -> {
            String codigo = input.getText().toString().toUpperCase().trim();
            if (!codigo.isEmpty()) {
                juntarEvento(codigo);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void juntarEvento(String codigo) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        tarefaRepositorio.juntarEvento(codigo, user.getUid())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Você entrou no evento!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Código inválido ou evento não encontrado.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao juntar evento", e);
            });
    }
}
