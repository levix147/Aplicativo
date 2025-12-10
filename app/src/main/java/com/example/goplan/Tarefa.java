package com.example.goplan;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

public class Tarefa implements Parcelable {

    @DocumentId
    private String id;

    private String titulo;
    private String descricao;
    private String data;
    private String hora;
    private String local;
    private String status;

    public Tarefa() {}

    public Tarefa(String titulo, String descricao, String data, String hora, String local, String status) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.status = status;
    }

    protected Tarefa(Parcel in) {
        id = in.readString();
        titulo = in.readString();
        descricao = in.readString();
        data = in.readString();
        hora = in.readString();
        local = in.readString();
        status = in.readString();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(titulo);
        dest.writeString(descricao);
        dest.writeString(data);
        dest.writeString(hora);
        dest.writeString(local);
        dest.writeString(status);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
}
