package com.androidutn.instablack1;

import android.os.Bundle;

public class PerfilActivity extends BaseActivity {

    public static final String EXTRA_UID = "uid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        String uid = getIntent().getStringExtra(EXTRA_UID);

        if (uid == null) {
            finish();
            return;
        }

        PerfilFragment fragment = PerfilFragment.newInstance(uid);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.perfil_container, fragment)
                .commit();
    }
}
