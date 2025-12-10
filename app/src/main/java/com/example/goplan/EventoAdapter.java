package com.example.goplan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private List<Tarefa> listaDeTarefas;

    public EventoAdapter(List<Tarefa> listaDeTarefas) {
        this.listaDeTarefas = listaDeTarefas;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_kanban, parent, false);
        return new EventoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Tarefa tarefaAtual = listaDeTarefas.get(position);

        holder.txtTitulo.setText(tarefaAtual.getTitulo());
        holder.txtData.setText(String.format("%s %s", tarefaAtual.getData(), tarefaAtual.getHora()));

        if (holder.txtTag != null) {
            holder.txtTag.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetalheEventoActivity.class);
            intent.putExtra("TAREFA_EXTRA", tarefaAtual);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaDeTarefas.size();
    }

    public void atualizarLista(List<Tarefa> novaLista) {
        this.listaDeTarefas = novaLista;
        notifyDataSetChanged();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitulo;
        public TextView txtData;
        public TextView txtTag;

        public EventoViewHolder(View view) {
            super(view);
            txtTitulo = view.findViewById(R.id.txtTitulo);
            txtData = view.findViewById(R.id.txtData);
            txtTag = view.findViewById(R.id.txtTag);
        }
    }
}
