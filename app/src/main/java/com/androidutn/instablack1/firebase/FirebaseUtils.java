package com.androidutn.instablack1.firebase;

import android.support.annotation.Nullable;

import com.androidutn.instablack1.model.Comentario;
import com.androidutn.instablack1.model.Post;
import com.androidutn.instablack1.model.PostRef;
import com.androidutn.instablack1.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

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

    public static void guardarPost(final Post post, @Nullable final DatabaseReference.CompletionListener listener) {
        final String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid != null) {
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
            final String key = postsRef.push().getKey();
            post.setId(key);

            postsRef.child(key).setValue(post, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        guardarUserPost(post, listener);
                    } else {
                        if (listener != null) {
                            listener.onComplete(databaseError, databaseReference);
                        }
                    }
                }
            });
        }
    }

    private static void guardarUserPost(final Post post, final DatabaseReference.CompletionListener listener) {
        final PostRef ref = new PostRef();
        ref.setFechaRev(post.getFechaRev());

        FirebaseDatabase.getInstance().getReference("UserPosts")
                .child(post.getAutorUid()).child(post.getId()).setValue(ref, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // actualizar feed del autor
                    FirebaseDatabase.getInstance().getReference("Feed")
                            .child(post.getAutorUid()).child(post.getId()).setValue(ref);

                    actualizarFeedSeguidores(post, ref, listener);
                } else {
                    if (listener != null) {
                        listener.onComplete(databaseError, databaseReference);
                    }
                }
            }
        });
    }

    private static void actualizarFeedSeguidores(final Post post, final PostRef ref, final DatabaseReference.CompletionListener listener) {
        FirebaseDatabase.getInstance().getReference("Seguidores")
                .child(post.getAutorUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getKey();

                    FirebaseDatabase.getInstance().getReference("Feed")
                            .child(uid).child(post.getId()).setValue(ref);
                }

                if (listener != null) {
                    listener.onComplete(null, null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (listener != null)
                    listener.onComplete(databaseError, null);
            }
        });
    }

    public static void buscarUsuario(String uid, ValueEventListener listener) {
        FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).addListenerForSingleValueEvent(listener);
    }

    public static void buscarPost(String id, ValueEventListener listener) {
        FirebaseDatabase.getInstance().getReference("Posts").child(id).addValueEventListener(listener);
    }

    public static void guardarComentario(String postId, Comentario comentario, DatabaseReference.CompletionListener listener) {
        final String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid != null) {
            DatabaseReference comentariosRef = FirebaseDatabase.getInstance().getReference("Comentarios").child(postId);
            final String key = comentariosRef.push().getKey();
            comentario.setId(key);

            comentariosRef.child(key).setValue(comentario, listener);
        }
    }

    public static void like(final String postId, final DatabaseReference.CompletionListener listener) {
        final String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid != null) {
            FirebaseDatabase.getInstance().getReference("Likes")
                    .child(postId).child(authUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean userLike = (Boolean) dataSnapshot.getValue();

                    final int valor = (userLike == null || !userLike) ? 1 : -1;

                    DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null)
                                actualizarLikeCount(postId, valor, listener);
                            else if (listener != null)
                                listener.onComplete(databaseError, databaseReference);
                        }
                    };

                    if (userLike == null || !userLike) {
                        dataSnapshot.getRef().setValue(true, completionListener);
                    } else {
                        dataSnapshot.getRef().removeValue(completionListener);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (listener != null)
                        listener.onComplete(databaseError, null);
                }
            });
        }
    }

    private static void actualizarLikeCount(String postId, final int valor, final DatabaseReference.CompletionListener listener) {
        FirebaseDatabase.getInstance().getReference("Posts").child(postId)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Post p = mutableData.getValue(Post.class);

                        if (p == null) {
                            return Transaction.success(mutableData);
                        }

                        p.setLikes(p.getLikes() + valor);
                        mutableData.setValue(p);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (listener != null)
                            listener.onComplete(databaseError, null);
                    }
                });
    }


    public static void dejarDeSeguir(final String uid) {
        final String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid != null) {
            FirebaseDatabase.getInstance().getReference("Siguiendo").child(authUid).child(uid).removeValue();
            FirebaseDatabase.getInstance().getReference("Seguidores").child(uid).child(authUid).removeValue();
        }
    }
}
