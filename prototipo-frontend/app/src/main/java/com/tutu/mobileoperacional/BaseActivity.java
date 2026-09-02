package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base de todas as telas.
 *
 * Resolve em um lugar só os dois problemas que apareciam espalhados pelo app:
 *
 * 1. Insets. Com targetSdk 36 o Android desenha o app de ponta a ponta e não
 *    existe mais opt-out. Sem tratar isso, o cabeçalho fica embaixo da barra de
 *    status e a barra inferior fica embaixo da barra de gestos do sistema.
 * 2. Barra inferior. Antes cada Activity repetia (ou esquecia) o mesmo bloco de
 *    navegação, então em 14 telas a barra aparecia mas não respondia ao toque.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Jornadas do app; definem para onde cada aba da barra inferior leva. */
    public enum Journey {
        ESTOQUISTA(HomeActivity.class, ProductsActivity.class,
                IntelligenceActivity.class, ProfileActivity.class),
        GERENTE(ManagerHomeActivity.class, ManagerProductsActivity.class,
                ManagerIntelligenceActivity.class, ProfileActivity.class),
        DONO(OwnerHomeActivity.class, ManagerProductsActivity.class,
                ManagerIntelligenceActivity.class, ConfigActivity.class);

        final Class<?> home;
        final Class<?> products;
        final Class<?> ai;
        /** Última aba: "Perfil" nas jornadas Estoquista/Gerente, "Config" na do Dono. */
        final Class<?> last;

        Journey(Class<?> home, Class<?> products, Class<?> ai, Class<?> last) {
            this.home = home;
            this.products = products;
            this.ai = ai;
            this.last = last;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // O app é claro em qualquer situação, então os ícones das barras do
        // sistema precisam ser escuros para não sumirem no fundo bege.
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyWindowInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyWindowInsets();
    }

    /**
     * Distribui os insets do sistema entre a raiz e a barra inferior.
     *
     * A raiz recebe topo e laterais. O rodapé vai para a própria barra, como
     * padding: assim o fundo branco continua desenhado atrás da barra de gestos
     * em vez de sobrar uma faixa vazia, e a barra cresce sozinha conforme o
     * aparelho (gestos, 3 botões, recorte de tela).
     */
    protected void applyWindowInsets() {
        final View root = findViewById(android.R.id.content);
        if (root == null) {
            return;
        }
        final View bottomNav = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int imeBottom = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            // Só o que o teclado ocupa além da barra do sistema é espaço novo.
            int keyboard = Math.max(imeBottom - bars.bottom, 0);

            if (bottomNav == null) {
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom + keyboard);
            } else {
                v.setPadding(bars.left, bars.top, bars.right, keyboard);
                bottomNav.setPadding(
                        bottomNav.getPaddingLeft(),
                        bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(),
                        keyboard > 0 ? 0 : bars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.requestApplyInsets(root);
    }

    /**
     * Liga a barra inferior da tela. Marca a aba atual e faz as outras abas
     * navegarem sem empilhar telas repetidas.
     *
     * @param journey        jornada da tela (estoquista, gerente ou dono)
     * @param selectedItemId aba que representa a tela atual, ou 0 quando a tela
     *                       é secundária (detalhe, formulário) e nenhuma aba
     *                       deve aparecer marcada
     */
    protected void setupBottomNav(Journey journey, @IdRes int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            return;
        }

        if (selectedItemId != 0 && bottomNav.getMenu().findItem(selectedItemId) != null) {
            bottomNav.setSelectedItemId(selectedItemId);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true;
            }

            Class<?> target = null;
            if (id == R.id.nav_home) {
                target = journey.home;
            } else if (id == R.id.nav_products) {
                target = journey.products;
            } else if (id == R.id.nav_ai) {
                target = journey.ai;
            } else if (id == R.id.nav_profile || id == R.id.nav_config) {
                target = journey.last;
            }

            if (target == null || target.equals(getClass())) {
                return target != null;
            }

            Intent intent = new Intent(this, target);
            // Reaproveita a tela se ela já estiver na pilha, em vez de abrir uma
            // cópia nova a cada toque na aba.
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            disableActivityTransition();
            return true;
        });
    }

    /** Troca de aba não deve parecer "abrir outra tela", então sem animação. */
    @SuppressWarnings("deprecation")
    private void disableActivityTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
        } else {
            overridePendingTransition(0, 0);
        }
    }
}
