package com.androidutn.instablack1;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidutn.instablack1.firebase.FirebaseUtils;
import com.androidutn.instablack1.model.Comentario;
import com.androidutn.instablack1.model.Post;
import com.androidutn.instablack1.model.Usuario;
import com.androidutn.instablack1.viewholders.ComentarioViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostActivity extends BaseActivity {

    public static final String EXTRA_POST_ID = "postId";
    public static final String EXTRA_SCROLL_COMENTARIOS = "scrollComentarios";

    @BindView(R.id.post_autor_imagen) ImageView mAutorImagen;
    @BindView(R.id.post_autor_nombre) TextView mAutorNombre;
    @BindView(R.id.post_imagen) ImageView mImagen;
    @BindView(R.id.post_like) ImageView mLike;
    @BindView(R.id.post_like_count) TextView mLikeCount;
    @BindView(R.id.post_texto) TextView mTexto;
    @BindView(R.id.post_fecha) TextView mFecha;
    @BindView(R.id.post_comentarios_count) TextView mComentariosCount;
    @BindView(R.id.post_comentarios) RecyclerView mComentarios;
    @BindView(R.id.comentario_texto) EditText mComentarioTexto;

    private Post post;
    private boolean scrollComentarios;
    private FirebaseRecyclerAdapter<Comentario, ComentarioViewHolder> adapterComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ButterKnife.bind(this);

        String postId = getIntent().getStringExtra(EXTRA_POST_ID);

        if (postId == null)
            return;

        scrollComentarios = getIntent().getBooleanExtra(EXTRA_SCROLL_COMENTARIOS, false);

        blockUI();

        FirebaseUtils.buscarPost(postId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unblockUI();
                post = dataSnapshot.getValue(Post.class);
                cargarDatos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                unblockUI();
                mostrarMensaje(databaseError.getMessage());
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("Comentarios").child(postId);
        FirebaseRecyclerOptions<Comentario> options = new FirebaseRecyclerOptions.Builder<Comentario>()
                .setQuery(query, Comentario.class)
                .build();
        adapterComentarios = new FirebaseRecyclerAdapter<Comentario, ComentarioViewHolder>(options) {
            @Override
            public ComentarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(PostActivity.this).inflate(R.layout.item_comentario, parent, false);
                return new ComentarioViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(ComentarioViewHolder holder, int position, Comentario model) {
                holder.setModel(model);
            }
        };
        mComentarios.setAdapter(adapterComentarios);
    }

    private void cargarDatos() {
        FirebaseUtils.buscarUsuario(post.getAutorUid(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                if (usuario != null) {
                    mAutorNombre.setText(usuario.getNombre());
                    if (usuario.getImagenUrl() != null)
                        Picasso.with(PostActivity.this).load(Uri.parse(usuario.getImagenUrl())).into(mAutorImagen);
                    else
                        mAutorImagen.setImageResource(R.drawable.account);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Picasso.with(this).load(Uri.parse(post.getFotoUrl())).into(mImagen);
        mTexto.setText(post.getTexto());

        mFecha.setText(DateUtils.getRelativeTimeSpanString(post.getFecha(), System.currentTimeMillis(), 0));

        if (post.getLikes() == 1) {
            mLikeCount.setText(getString(R.string.like_count_1));
            mLikeCount.setVisibility(View.VISIBLE);
        } else if (post.getLikes() > 1) {
            mLikeCount.setText(getString(R.string.like_count, post.getLikes()));
            mLikeCount.setVisibility(View.VISIBLE);
        } else {
            mLikeCount.setVisibility(View.GONE);
        }

        mComentariosCount.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("Likes")
                .child(post.getId())
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean userLike = (Boolean) dataSnapshot.getValue();
                        if (userLike != null && userLike) {
                            mLike.setColorFilter(ContextCompat.getColor(PostActivity.this, R.color.colorAccent));
                        } else {
                            mLike.setColorFilter(ContextCompat.getColor(PostActivity.this, android.R.color.primary_text_dark));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    @OnClick(R.id.post_like)
    public void onLike() {
        FirebaseUtils.like(post.getId(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    mostrarMensaje(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterComentarios.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterComentarios.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: scroll a comentarios
    }

    @OnClick(R.id.comentario_enviar)
    public void onComentarioEnviar() {
        if (!TextUtils.isEmpty(mComentarioTexto.getText())) {
            Comentario comentario = new Comentario();
            comentario.setAutorUid(FirebaseAuth.getInstance().getUid());
            comentario.setFecha(System.currentTimeMillis());
            comentario.setTexto(mComentarioTexto.getText().toString());

            FirebaseUtils.guardarComentario(post.getId(), comentario, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    // aumentar contador comentarios

                    FirebaseDatabase.getInstance().getReference("Posts").child(post.getId()).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Post post = mutableData.getValue(Post.class);

                            if (post == null) {
                                return Transaction.success(mutableData);
                            }

                            post.setComentarios(post.getComentarios() + 1);

                            mutableData.setValue(post);

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                mostrarMensaje(databaseError.getMessage());
                            } else {
                                mComentarioTexto.setText(null);
                            }
                        }
                    });
                }
            });


        }
    }
}
