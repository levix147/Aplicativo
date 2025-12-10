package com.example.goplan;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class Tarefa implements Parcelable {

    @DocumentId
    private String id;
    private String userId;

    private String titulo;
    private String descricao;
    private String data;
    private String hora;
    private String local;
    private String status;
    
    private String codigoDeConvite;
    private List<String> membros;

    public Tarefa() {}

    public Tarefa(String userId, String titulo, String descricao, String data, String hora, String local, String status) {
        this.userId = userId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.status = status;
    }

    public Tarefa(String id, String userId, String titulo, String descricao, String data, String hora, String local, String status) {
        this.id = id;
        this.userId = userId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.status = status;
    }

    protected Tarefa(Parcel in) {
        id = in.readString();
        userId = in.readString();
        titulo = in.readString();
        descricao = in.readString();
        data = in.readString();
        hora = in.readString();
        local = in.readString();
        status = in.readString();
        codigoDeConvite = in.readString();
        membros = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(titulo);
        dest.writeString(descricao);
        dest.writeString(data);
        dest.writeString(hora);
        dest.writeString(local);
        dest.writeString(status);
        dest.writeString(codigoDeConvite);
        dest.writeStringList(membros);
    }

    public static final Creator<Tarefa> CREATOR = new Creator<Tarefa>() {
        @Override
        public Tarefa createFromParcel(Parcel in) {
            return new Tarefa(in);
        }

        @Override
        public Tarefa[] newArray(int size) {
            return new Tarefa[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCodigoDeConvite() { return codigoDeConvite; }
    public void setCodigoDeConvite(String codigoDeConvite) { this.codigoDeConvite = codigoDeConvite; }
    public List<String> getMembros() { return membros; }
    public void setMembros(List<String> membros) { this.membros = membros; }
}
