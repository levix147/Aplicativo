package com.example.goplan;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TarefaRepositorio {

    private static final String TAG = "TarefaRepositorio";
    private final CollectionReference tarefasCollection;

    public TarefaRepositorio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        tarefasCollection = db.collection("tarefas");
    }

    public void salvarTarefa(Tarefa tarefa) {
        tarefasCollection.add(tarefa)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Tarefa salva com ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar tarefa", e));
    }

    public void atualizarStatusTarefa(String tarefaId, String novoStatus) {
        if (tarefaId == null || tarefaId.isEmpty()) {
            Log.e(TAG, "ID da tarefa invalido para atualizacao.");
            return;
        }
        tarefasCollection.document(tarefaId)
                .update("status", novoStatus)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Status da tarefa atualizado com sucesso."))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao atualizar status da tarefa", e));
    }

    public CollectionReference getTarefasCollection() {
        return tarefasCollection;
    }
}
