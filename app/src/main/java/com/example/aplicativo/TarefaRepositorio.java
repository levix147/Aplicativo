package com.example.aplicativo;

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

    /**
     * Salva uma nova tarefa no Firestore.
     * @param tarefa A tarefa a ser salva.
     */
    public void salvarTarefa(Tarefa tarefa) {
        tarefasCollection.add(tarefa)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Tarefa salva com ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar tarefa", e));
    }

    /**
     * Atualiza o status de uma tarefa existente no Firestore.
     * @param tarefaId O ID do documento da tarefa a ser atualizada.
     * @param novoStatus O novo status para a tarefa ("A_FAZER", "FAZENDO", "CONCLUIDO").
     */
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


    /**
     * Retorna a referencia para a colecao de tarefas, para que as Activities
     * possam adicionar listeners para atualizacoes em tempo real.
     * @return A referencia da colecao do Firestore.
     */
    public CollectionReference getTarefasCollection() {
        return tarefasCollection;
    }
}