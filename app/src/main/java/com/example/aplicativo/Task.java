package com.example.aplicativo;

public class Task {
    private String titulo;
    private String descricao;
    private String data;
    private String hora;
    private String local;
    private String status; // "A_FAZER", "FAZENDO", "FEITO"

    public Task(String titulo, String descricao, String data, String hora, String local, String status) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.status = status;
    }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getData() { return data; }
    public String getHora() { return hora; }
    public String getLocal() { return local; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}