package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicia o app diretamente na tela de login para simular a jornada
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}