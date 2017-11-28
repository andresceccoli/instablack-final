package com.androidutn.instablack1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeActivity extends BaseActivity {

    private static final int REQUEST_PERMISSIONS = 200;
    private static final int REQUEST_IMAGEN = 201;
    private static final int REQUEST_FILTROS = 202;
    private TextView mTextMessage;
    private Uri mArchivoUri;

    private BottomNavigationViewEx mNavigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_feed:
                    mostrarFeed();
                    return true;
                case R.id.navigation_usuarios:
                    mostrarUsuarios();
                    return true;
                case R.id.navigation_nuevo_post:
                    seleccionarImagen();
                    return true;
                case R.id.navigation_cuenta:
                    mostrarPerfil();
                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigation = findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mNavigation.enableAnimation(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.enableShiftingMode(false);
        mNavigation.setTextVisibility(false);

        if (savedInstanceState != null) {
            mArchivoUri = savedInstanceState.getParcelable("archivoUri");
        }

        mostrarFeed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("archivoUri", mArchivoUri);
    }

    private void mostrarFeed() {
        FeedFragment fragment = (FeedFragment) getSupportFragmentManager().findFragmentByTag("feed");
        if (fragment == null) {
            fragment = FeedFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, "feed").commit();
    }

    private void mostrarUsuarios() {
        UsuariosFragment fragment = (UsuariosFragment) getSupportFragmentManager().findFragmentByTag("usuarios");
        if (fragment == null) {
            fragment = UsuariosFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, "usuarios").commit();
    }

    private void mostrarPerfil() {
        PerfilFragment fragment = (PerfilFragment) getSupportFragmentManager().findFragmentByTag("perfil");
        if (fragment == null) {
            fragment = PerfilFragment.newInstance(FirebaseAuth.getInstance().getUid());
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, "perfil").commit();
    }

    private void seleccionarImagen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_PERMISSIONS);

            return;
        }

        File archivoTemp = new File(getExternalCacheDir(), UUID.randomUUID().toString());
        mArchivoUri = Uri.fromFile(archivoTemp);

        // camara
        List<Intent> camaraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> camaraApps = getPackageManager().queryIntentActivities(captureIntent, 0);
        for (ResolveInfo info : camaraApps) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            intent.setPackage(info.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mArchivoUri);
            camaraIntents.add(intent);
        }

        // galeria
        Intent picker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooser = Intent.createChooser(picker, getString(R.string.title_picker));
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, camaraIntents.toArray(new Parcelable[camaraIntents.size()]));
        startActivityForResult(chooser, REQUEST_IMAGEN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        if (granted) {
            seleccionarImagen();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGEN) {
            if (resultCode == RESULT_OK) {
                boolean camara = false;
                if (data == null || data.getData() == null) {
                    camara = true;
                } else if (data.getAction() != null) {
                    camara = data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                if (!camara) {
                    mArchivoUri = data.getData();
                }

                if (mArchivoUri != null) {
                    Intent filtros = new Intent(this, FiltrosActivity.class);
                    filtros.putExtra(FiltrosActivity.EXTRA_URI, mArchivoUri);
                    startActivityForResult(filtros, REQUEST_FILTROS);
                }
            }
        } else if (requestCode == REQUEST_FILTROS) {
            mNavigation.setCurrentItem(0);
        }
    }
}
