package com.androidutn.instablack1;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidutn.instablack1.firebase.FirebaseUtils;
import com.androidutn.instablack1.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublicacionActivity extends BaseActivity {

    public static final String EXTRA_URI = "uri";

    @BindView(R.id.publicacion_imagen) ImageView mImagen;
    @BindView(R.id.publicacion_texto) TextView mTexto;

    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicacion);

        ButterKnife.bind(this);

        uri = getIntent().getParcelableExtra(EXTRA_URI);
        mImagen.setImageURI(uri);
    }

    @OnClick(R.id.publicacion_aceptar)
    public void onAceptar() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            StorageReference imagenesRef = FirebaseStorage.getInstance().getReference("imagenes");
            String imagenId = UUID.randomUUID().toString();

            blockUI();

            imagenesRef.child(imagenId).putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            publicarPost(downloadUrl);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            unblockUI();
                            mostrarMensaje(e);
                        }
                    });


        }
    }

    private void publicarPost(Uri downloadUrl) {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            Post post = new Post();
            post.setAutorUid(uid);
            post.setFecha(System.currentTimeMillis());
            post.setFechaRev(-post.getFecha());
            post.setTexto(mTexto.getText().toString());
            post.setFotoUrl(downloadUrl.toString());

            FirebaseUtils.guardarPost(post, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    unblockUI();

                    if (databaseError == null) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mostrarMensaje(databaseError.getMessage());
                    }
                }
            });
        }
    }


}
