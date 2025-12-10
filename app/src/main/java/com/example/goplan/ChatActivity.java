package com.example.goplan;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText editTextMensagem;
    private ImageView btnEnviarMensagem;

    private List<Mensagem> listaDeMensagens = new ArrayList<>();
    private String eventoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        eventoId = getIntent().getStringExtra("EVENTO_ID");
        String nomeDoEvento = getIntent().getStringExtra("NOME_DO_EVENTO");

        if (eventoId == null) {
            Toast.makeText(this, "Erro: Evento não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarToolbar(nomeDoEvento);
        iniciarComponentes();
        configurarRecyclerView();
        configurarListeners();
        ouvirMensagens();
    }

    private void configurarToolbar(String nomeDoEvento) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_chat);
        toolbar.setTitle(nomeDoEvento);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void iniciarComponentes() {
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
        editTextMensagem = findViewById(R.id.edit_text_mensagem);
        btnEnviarMensagem = findViewById(R.id.btn_enviar_mensagem);
    }

    private void configurarRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void configurarListeners() {
        btnEnviarMensagem.setOnClickListener(v -> enviarMensagem());
    }

    private void ouvirMensagens() {
        CollectionReference mensagensRef = FirebaseFirestore.getInstance()
                .collection("tarefas").document(eventoId).collection("mensagens");

        mensagensRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar mensagens.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    listaDeMensagens = snapshots.toObjects(Mensagem.class);
                    chatAdapter.setListaDeMensagens(listaDeMensagens);
                    recyclerViewChat.scrollToPosition(listaDeMensagens.size() - 1);
                }
            });
    }

    private void enviarMensagem() {
        String textoMensagem = editTextMensagem.getText().toString().trim();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (textoMensagem.isEmpty() || currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();

        Mensagem novaMensagem = new Mensagem(textoMensagem, userId, userName);

        FirebaseFirestore.getInstance()
            .collection("tarefas").document(eventoId).collection("mensagens")
            .add(novaMensagem)
            .addOnSuccessListener(documentReference -> {
                editTextMensagem.setText("");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Falha ao enviar mensagem.", Toast.LENGTH_SHORT).show();
            });
    }
}
