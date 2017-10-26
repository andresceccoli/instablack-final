package com.androidutn.instablack1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.androidutn.instablack1.firebase.FirebaseUtils;
import com.androidutn.instablack1.model.Usuario;
import com.androidutn.instablack1.viewholders.UsuarioViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UsuariosFragment extends Fragment {

    @BindView(R.id.list) RecyclerView mList;
    @BindView(R.id.busqueda) EditText mBusqueda;
    private FirebaseRecyclerAdapter<Usuario, UsuarioViewHolder> adapter;

    public UsuariosFragment() {
    }

    public static UsuariosFragment newInstance() {
        UsuariosFragment fragment = new UsuariosFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);
        ButterKnife.bind(this, view);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            Query query = FirebaseDatabase.getInstance().getReference("Siguiendo").child(uid);
            DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios");
            FirebaseRecyclerOptions<Usuario> options = new FirebaseRecyclerOptions.Builder<Usuario>()
                    .setIndexedQuery(query, usuariosRef, Usuario.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<Usuario, UsuarioViewHolder>(options) {

                @Override
                public UsuarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
                    return new UsuarioViewHolder(itemView);
                }

                @Override
                protected void onBindViewHolder(UsuarioViewHolder holder, int position, Usuario model) {
                    holder.setModel(model);
                }
            };

            mList.setAdapter(adapter);
        }

        return view;
    }

    @OnClick(R.id.seguir)
    public void onSeguir() {
        if (!TextUtils.isEmpty(mBusqueda.getText())) {
            String email = mBusqueda.getText().toString();
            email = email.replace('@', '_').replace('.', '_');

            FirebaseDatabase.getInstance().getReference("Emails").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String uid = dataSnapshot.getValue().toString();
                        FirebaseUtils.seguirUsuario(uid, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    ((BaseActivity) getActivity()).mostrarMensaje(databaseError.getMessage());
                                } else {
                                    mBusqueda.setText(null);
                                    ((BaseActivity) getActivity()).mostrarMensaje(R.string.seguir_ok);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ((BaseActivity) getActivity()).mostrarMensaje(databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
