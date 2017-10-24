package com.androidutn.instablack1;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublicacionActivity extends BaseActivity {

    public static final String EXTRA_URI = "uri";

    @BindView(R.id.publicacion_imagen) ImageView mImagen;
    @BindView(R.id.publicacion_texto) TextView mTexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicacion);

        ButterKnife.bind(this);

        Uri uri = getIntent().getParcelableExtra(EXTRA_URI);
        mImagen.setImageURI(uri);
    }

    @OnClick(R.id.publicacion_aceptar)
    public void onAceptar() {
//        String uid = FirebaseAuth.getInstance().getUid();
//
//        if (uid != null) {
//            Post post = new Post();
//            post.setAutorUid(uid);
//            post.setFecha(System.currentTimeMillis());
//            post.setFechaRev(-post.getFecha());
//            post.setTexto(mTexto.getText().toString());
//        }
    }
}
