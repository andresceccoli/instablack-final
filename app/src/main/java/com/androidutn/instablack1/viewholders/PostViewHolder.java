package com.androidutn.instablack1.viewholders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidutn.instablack1.R;
import com.androidutn.instablack1.firebase.FirebaseUtils;
import com.androidutn.instablack1.model.Post;
import com.androidutn.instablack1.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by andres on 10/24/17.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.post_autor_imagen) ImageView mAutorImagen;
    @BindView(R.id.post_autor_nombre) TextView mAutorNombre;
    @BindView(R.id.post_imagen) ImageView mImagen;
    @BindView(R.id.post_like_count) TextView mLikeCount;
    @BindView(R.id.post_texto) TextView mTexto;
    @BindView(R.id.post_fecha) TextView mFecha;
    @BindView(R.id.post_comentarios_count) TextView mComentariosCount;

    public PostViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    public void setModel(Post p) {
        FirebaseUtils.buscarUsuario(p.getAutorUid(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                if (usuario != null) {
                    mAutorNombre.setText(usuario.getNombre());
                    if (usuario.getImagenUrl() != null)
                        Picasso.with(itemView.getContext()).load(Uri.parse(usuario.getImagenUrl())).into(mAutorImagen);
                    else
                        mAutorImagen.setImageResource(R.drawable.account);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Picasso.with(itemView.getContext()).load(Uri.parse(p.getFotoUrl())).into(mImagen);
        mTexto.setText(p.getTexto());

        mFecha.setText(DateUtils.getRelativeTimeSpanString(p.getFecha(), System.currentTimeMillis(), 0));

        if (p.getLikes() == 1) {
            mLikeCount.setText(itemView.getContext().getString(R.string.like_count_1));
            mLikeCount.setVisibility(View.VISIBLE);
        } else if (p.getLikes() > 1) {
            mLikeCount.setText(itemView.getContext().getString(R.string.like_count, p.getLikes()));
            mLikeCount.setVisibility(View.VISIBLE);
        } else {
            mLikeCount.setVisibility(View.GONE);
        }

        if (p.getComentarios() == 1) {
            mComentariosCount.setText(itemView.getContext().getString(R.string.comentarios_count_1));
            mComentariosCount.setVisibility(View.VISIBLE);
        } else  if (p.getComentarios() > 1) {
            mComentariosCount.setText(itemView.getContext().getString(R.string.comentarios_count, p.getComentarios()));
            mComentariosCount.setVisibility(View.VISIBLE);
        } else {
            mComentariosCount.setVisibility(View.GONE);
        }
    }

}
