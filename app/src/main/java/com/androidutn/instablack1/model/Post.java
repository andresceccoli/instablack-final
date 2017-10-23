package com.androidutn.instablack1.model;

/**
 * Created by andres on 10/23/17.
 */

public class Post {

    private String id;
    private long fecha;
    private long fechaRev;
    private String fotoUrl;
    private String texto;
    private int likes;
    private int comentarios;
    private String autorUid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public long getFechaRev() {
        return fechaRev;
    }

    public void setFechaRev(long fechaRev) {
        this.fechaRev = fechaRev;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComentarios() {
        return comentarios;
    }

    public void setComentarios(int comentarios) {
        this.comentarios = comentarios;
    }

    public String getAutorUid() {
        return autorUid;
    }

    public void setAutorUid(String autorUid) {
        this.autorUid = autorUid;
    }
}
