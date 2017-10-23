package com.androidutn.instablack1;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by andres on 10/23/17.
 */

public class BaseActivity extends AppCompatActivity {

    private DelayedProgressDialog progress;

    protected void mostrarMensaje(int resId) {
        mostrarMensaje(getString(resId));
    }

    protected void mostrarMensaje(Throwable t) {
        t.printStackTrace();

        mostrarMensaje(t.getLocalizedMessage());
    }

    protected void mostrarMensaje(String texto) {
        new AlertDialog.Builder(this)
                .setMessage(texto)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    protected void blockUI() {
        if (progress == null) {
            progress = new DelayedProgressDialog();
        }
        progress.show(getSupportFragmentManager(), "progress");
    }

    protected void unblockUI() {
        if (progress != null) {
            progress.cancel();
        }
    }

}
