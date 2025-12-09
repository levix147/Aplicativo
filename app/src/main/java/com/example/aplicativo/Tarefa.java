package com.example.aplicativo;

import com.google.firebase.firestore.DocumentId;

public class Tarefa {

    @DocumentId
    private String id;

    private String titulo;
    private String descricao;
    private String data;
    private String hora;
    private String local;
    private String status; // "A_FAZER", "FAZENDO", "FEITO"

    // Construtor vazio necessario para o Firestore
    public Tarefa() {}

    public Tarefa(String titulo, String descricao, String data, String hora, String local, String status) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getData() { return data; }
    public String getHora() { return hora; }
    public String getLocal() { return local; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setData(String data) { this.data = data; }
    public void setHora(String hora) { this.hora = hora; }
    public void setLocal(String local) { this.local = local; }
    public void setStatus(String status) { this.status = status; }
}