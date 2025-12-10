package com.example.goplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ENVIADA = 1;
    private static final int VIEW_TYPE_RECEBIDA = 2;

    private List<Mensagem> listaDeMensagens;

    public ChatAdapter(List<Mensagem> listaDeMensagens) {
        this.listaDeMensagens = listaDeMensagens;
    }

    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = listaDeMensagens.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser() != null && mensagem.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_ENVIADA;
        } else {
            return VIEW_TYPE_RECEBIDA;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ENVIADA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_enviada, parent, false);
            return new EnviadaViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_recebida, parent, false);
            return new RecebidaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensagem mensagem = listaDeMensagens.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ENVIADA) {
            ((EnviadaViewHolder) holder).bind(mensagem);
        } else {
            ((RecebidaViewHolder) holder).bind(mensagem);
        }
    }

    @Override
    public int getItemCount() {
        return listaDeMensagens.size();
    }

    public void setListaDeMensagens(List<Mensagem> novaLista) {
        this.listaDeMensagens = novaLista;
        notifyDataSetChanged();
    }

    // ViewHolder para mensagens enviadas
    private static class EnviadaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMensagem;

        EnviadaViewHolder(View itemView) {
            super(itemView);
            textViewMensagem = itemView.findViewById(R.id.text_view_mensagem);
        }

        void bind(Mensagem mensagem) {
            textViewMensagem.setText(mensagem.getTexto());
        }
    }

    // ViewHolder para mensagens recebidas
    private static class RecebidaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMensagem;
        TextView textViewNomeUsuario;

        RecebidaViewHolder(View itemView) {
            super(itemView);
            textViewMensagem = itemView.findViewById(R.id.text_view_mensagem);
            textViewNomeUsuario = itemView.findViewById(R.id.text_view_nome_usuario);
        }

        void bind(Mensagem mensagem) {
            textViewMensagem.setText(mensagem.getTexto());
            textViewNomeUsuario.setText(mensagem.getUserName());
        }
    }
}
