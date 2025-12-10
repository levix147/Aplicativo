package com.example.goplan;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Mensagem {
    private String texto;
    private String userId;
    private String userName; 
    private @ServerTimestamp Date timestamp;

    public Mensagem() {}

    public Mensagem(String texto, String userId, String userName) {
        this.texto = texto;
        this.userId = userId;
        this.userName = userName;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
