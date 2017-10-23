package com.androidutn.instablack1.model;

/**
 * Created by andres on 10/23/17.
 */

public class Comentario {

    private String id;
    private String texto;
    private String autorUid;
    private long fecha;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutorUid() {
        return autorUid;
    }

    public void setAutorUid(String autorUid) {
        this.autorUid = autorUid;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}
