package com.androidutn.instablack1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidutn.instablack1.firebase.FirebaseUtils;
import com.androidutn.instablack1.model.Post;
import com.androidutn.instablack1.model.Usuario;
import com.androidutn.instablack1.viewholders.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PerfilFragment extends Fragment {

    @BindView(R.id.perfil_foto) ImageView mFoto;
    @BindView(R.id.perfil_editar_foto) View mEditarFoto;
    @BindView(R.id.perfil_nombre) TextView mNombre;
    @BindView(R.id.perfil_email) TextView mEmail;
    @BindView(R.id.perfil_editar_nombre) View mEditarNombre;
    @BindView(R.id.perfil_posts) TextView mPosts;
    @BindView(R.id.perfil_seguidores) TextView mSeguidores;
    @BindView(R.id.perfil_siguiendo) TextView mSiguiendo;
    @BindView(R.id.perfil_posts_list) RecyclerView mPostsList;
    @BindView(R.id.perfil_follow) View mFollow;
    @BindView(R.id.perfil_unfollow) View mUnfollow;
    @BindView(R.id.perfil_salir) View mSalir;
    @BindView(R.id.perfil_nombre_edit) EditText mNombreEdit;
    @BindView(R.id.perfil_editar_ok) View mEditarOk;

    private String uid;
    private Usuario usuario;
    private boolean perfilPropio;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> postsAdapter;

    public static PerfilFragment newInstance(String uid) {
        PerfilFragment f = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            uid = getArguments().getString("uid");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        ButterKnife.bind(this, view);

        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        mNombreEdit.setVisibility(View.GONE);
        mEditarOk.setVisibility(View.GONE);

        perfilPropio = FirebaseAuth.getInstance().getUid().equals(uid);
        if (perfilPropio) {
            mSalir.setVisibility(View.VISIBLE);
            mEditarFoto.setVisibility(View.VISIBLE);
            mEditarNombre.setVisibility(View.VISIBLE);
        } else {
            mSalir.setVisibility(View.GONE);
            mEditarFoto.setVisibility(View.GONE);
            mEditarNombre.setVisibility(View.GONE);
            mNombre.setPadding(0, 0, 0, 0);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        cargarDatos();

        postsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        postsAdapter.stopListening();
    }
    @OnClick(R.id.perfil_editar_nombre)
    public void onEditarNombre() {
        mNombreEdit.setText(mNombre.getText());
        mNombreEdit.setVisibility(View.VISIBLE);
        mNombre.setVisibility(View.GONE);
        mEditarNombre.setVisibility(View.GONE);
        mEditarOk.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.perfil_editar_ok)
    public void onEditarOk() {
        final String nombre = mNombreEdit.getText().toString();
        FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("nombre").setValue(nombre, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mNombreEdit.setVisibility(View.GONE);
                mNombre.setText(nombre);
                mEditarNombre.setVisibility(View.VISIBLE);
                mEditarOk.setVisibility(View.GONE);
                mNombre.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cargarDatos() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // datos del usuario
        FirebaseUtils.buscarUsuario(uid, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuario = dataSnapshot.getValue(Usuario.class);
                if (usuario != null) {
                    mNombre.setText(usuario.getNombre());
                    mEmail.setText(usuario.getEmail());
                    if (usuario.getImagenUrl() != null) {
                        Picasso.with(getActivity()).load(Uri.parse(usuario.getImagenUrl())).into(mFoto);
                    } else {
                        mFoto.setImageResource(R.drawable.account);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ((BaseActivity) getActivity()).mostrarMensaje(databaseError.getMessage());
            }
        });

        // revisar si esta siguiendo o no
        if (!perfilPropio) {
            db.getReference("Siguiendo")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                mUnfollow.setVisibility(View.VISIBLE);
                                mFollow.setVisibility(View.GONE);
                            } else {
                                mUnfollow.setVisibility(View.GONE);
                                mFollow.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }

        // contadores
        db.getReference("UserPosts").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPosts.setText(String.format(Locale.getDefault(), "%d", dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mPosts.setText("0");
                    }
                });
        db.getReference("Seguidores").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSeguidores.setText(String.format(Locale.getDefault(), "%d", dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mSeguidores.setText("0");
                    }
                });
        db.getReference("Siguiendo").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSiguiendo.setText(String.format(Locale.getDefault(), "%d", dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mSiguiendo.setText("0");
                    }
                });

        // posts
        Query query = FirebaseDatabase.getInstance().getReference("UserPosts").child(uid).orderByChild("fechaRev");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setIndexedQuery(query, postsRef, Post.class)
                .build();
        postsAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(PostViewHolder holder, int position, Post model) {
                holder.setModel(model);
            }

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.item_post, parent, false);
                return new PostViewHolder(itemView);
            }
        };
        mPostsList.setAdapter(postsAdapter);
    }

    @OnClick(R.id.perfil_unfollow)
    public void onUnfollow() {
        FirebaseUtils.dejarDeSeguir(uid);
    }

    @OnClick(R.id.perfil_follow)
    public void onFollow() {
        FirebaseUtils.seguirUsuario(uid, null);
    }

    @OnClick(R.id.perfil_salir)
    public void onSalir() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(i);
    }
}
