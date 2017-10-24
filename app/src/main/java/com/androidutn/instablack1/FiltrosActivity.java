package com.androidutn.instablack1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidutn.instablack1.model.Thumbnail;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FiltrosActivity extends BaseActivity {

    private static final int REQUEST_PUBLICAR = 150;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private static final int MAX_SIZE = 1280;
    public static final String EXTRA_URI = "uri";
    private Uri uriOriginal;

    @BindView(R.id.filtros_preview) ImageView mPreview;
    @BindViews({R.id.filtros_original, R.id.filtros_1, R.id.filtros_2, R.id.filtros_3, R.id.filtros_4, R.id.filtros_5}) List<ImageView> mThumbs;
    @BindView(R.id.filtros_nombre) TextView mNombre;
    private Bitmap bitmapBase;
    private List<Thumbnail> thumbnails;
    private Filter filtroSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);

        ButterKnife.bind(this);

        uriOriginal = getIntent().getParcelableExtra("uri");

        if (uriOriginal == null) {
            finish();
        }

        cargarFiltros();
    }

    @OnClick({R.id.filtros_original, R.id.filtros_1, R.id.filtros_2, R.id.filtros_3, R.id.filtros_4, R.id.filtros_5})
    public void onImagenSeleccionada(View v) {
        int imagen = mThumbs.indexOf(v);
        if (imagen >= 0 && imagen < thumbnails.size()) {
            seleccionarImagen(imagen);
        }
    }

    private void cargarFiltros() {
        Bitmap bm = ImageUtils.cargarBitmap(FiltrosActivity.this, uriOriginal, MAX_SIZE);
        bitmapBase = ImageUtils.generarThumbnail(bm, 600);
        bitmapBase = ImageUtils.convertirGrayscale(bitmapBase);

        thumbnails = new ArrayList<>();

        Bitmap thumb = ImageUtils.generarThumbnail(this, bitmapBase);
        thumbnails.add(Thumbnail.getBase(this, thumb));
        thumbnails.add(Thumbnail.getVintage(this, thumb));
        thumbnails.add(Thumbnail.getContraste(this, thumb));
        thumbnails.add(Thumbnail.getBrillo(this, thumb));
        thumbnails.add(Thumbnail.getTinta(this, thumb));
        thumbnails.add(Thumbnail.getHighlight(this, thumb));

        for (int i = 0; i < thumbnails.size(); i++) {
            mThumbs.get(i).setImageBitmap(thumbnails.get(i).bitmap);
        }

        seleccionarImagen(0);
    }

    private void seleccionarImagen(int imagen) {
        Thumbnail thumbnail = thumbnails.get(imagen);
        Bitmap bitmap = bitmapBase;
        if (thumbnail.filtro != null) {
            bitmap = thumbnail.filtro.processFilter(Bitmap.createBitmap(bitmap));
        }
        mPreview.setImageBitmap(bitmap);
        mNombre.setText(thumbnail.nombre);
        filtroSeleccionado = thumbnail.filtro;
    }

    @OnClick(R.id.filtros_aceptar)
    public void onAceptar() {
        try {
            File archivo = new File(getExternalCacheDir(), UUID.randomUUID().toString());
            Uri uri = Uri.fromFile(archivo);

            // aplicar filtro a imagen original
            Bitmap bitmap = ImageUtils.cargarBitmap(FiltrosActivity.this, uriOriginal, MAX_SIZE);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap = ImageUtils.convertirGrayscale(bitmap);
            if (filtroSeleccionado != null) {
                bitmap = filtroSeleccionado.processFilter(bitmap);
            }

            OutputStream out = new FileOutputStream(archivo);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();

            Intent publicacion = new Intent(this, PublicacionActivity.class);
            publicacion.putExtra(PublicacionActivity.EXTRA_URI, uri);
            startActivityForResult(publicacion, REQUEST_PUBLICAR);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje(e);
        }
    }
}
