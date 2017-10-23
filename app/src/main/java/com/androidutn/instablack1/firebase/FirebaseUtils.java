package com.androidutn.instablack1.firebase;

import android.support.annotation.Nullable;

import com.androidutn.instablack1.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by andres on 10/23/17.
 */

public class FirebaseUtils {

    public static void insertarUsuario(@Nullable DatabaseReference.CompletionListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            final Usuario usuario = new Usuario();
            usuario.setId(user.getUid());
            usuario.setNombre(user.getDisplayName());
            usuario.setEmail(user.getEmail());
            if (user.getPhotoUrl() != null) {
                usuario.setImagenUrl(user.getPhotoUrl().toString());
            }

            FirebaseDatabase.getInstance().getReference("Usuarios").child(usuario.getId())
                    .setValue(usuario, listener);
        }
    }

    public static void insertarEmail(@Nullable DatabaseReference.CompletionListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();
            FirebaseDatabase.getInstance().getReference("Emails").child(email.replace('@', '_').replace('.', '_'))
                    .setValue(uid, listener);
        }
    }

    public static void seguirUsuario(final String uid, @Nullable final DatabaseReference.CompletionListener listener) {
        final String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid != null) {
            FirebaseDatabase.getInstance().getReference("Siguiendo")
                    .child(authUid).child(uid).setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        FirebaseDatabase.getInstance().getReference("Seguidores")
                                .child(uid).child(authUid).setValue(true, listener);
                    } else {
                        if (listener != null) {
                            listener.onComplete(databaseError, databaseReference);
                        }
                    }
                }
            });
        }
    }

}
