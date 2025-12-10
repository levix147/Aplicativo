package com.example.goplan;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class TarefaRepositorio {

    private static final String TAG = "TarefaRepositorio";
    private final CollectionReference tarefasCollection;

    public TarefaRepositorio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        tarefasCollection = db.collection("tarefas");
    }

    public Task<DocumentReference> salvarTarefa(Tarefa tarefa) {
        return tarefasCollection.add(tarefa);
    }

    public Task<Void> atualizarTarefa(Tarefa tarefa) {
        if (tarefa.getId() == null || tarefa.getId().isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("ID da tarefa inválido"));
        }
        return tarefasCollection.document(tarefa.getId()).set(tarefa);
    }

    public Task<Void> excluirTarefa(String tarefaId) {
        if (tarefaId == null || tarefaId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("ID da tarefa inválido"));
        }
        return tarefasCollection.document(tarefaId).delete();
    }

    public Task<Void> juntarEvento(String codigo, String userId) {
        return tarefasCollection.whereEqualTo("codigoDeConvite", codigo)
                .limit(1)
                .get()
                .onSuccessTask(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        throw new RuntimeException("Evento não encontrado.");
                    }
                    DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                    return docRef.update("membros", FieldValue.arrayUnion(userId));
                });
    }

    public void atualizarStatusTarefa(String tarefaId, String novoStatus) {
        if (tarefaId == null || tarefaId.isEmpty()) {
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
