package com.tutu.mobileoperacional;

import android.os.Bundle;

public class ScannerNotFoundActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_not_found);

        // Esta tela nao tem botao de voltar proprio: a volta e pelo gesto/botao
        // do sistema ou pela barra inferior.
        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_products);
    }
}
