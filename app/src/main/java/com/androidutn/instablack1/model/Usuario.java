package com.androidutn.instablack1.model;

/**
 * Created by andres on 10/23/17.
 */

public class Usuario {

    private String id;
    private String email;
    private String imagenUrl;
    private String nombre;
    private int seguidores;
    private int siguiendo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public int getSiguiendo() {
        return siguiendo;
    }

    public void setSiguiendo(int siguiendo) {
        this.siguiendo = siguiendo;
    }
}
