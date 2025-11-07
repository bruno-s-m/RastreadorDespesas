package com.example.rastreadordespesas;

import android.content.Intent; // Importe o Intent
import android.os.Bundle;
import android.view.View; // Importe o View

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Importe o FAB

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdicionarDespesa; // Variável para o FAB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Configuração da Barra de Navegação (como antes) ---
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        // --- ADICIONE O CÓDIGO DO FAB ---
        fabAdicionarDespesa = findViewById(R.id.fabAdicionarDespesa);
        fabAdicionarDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abre a tela de Adicionar Despesa
                Intent intent = new Intent(MainActivity.this, AdicionarDespesaActivity.class);
                startActivity(intent);
            }
        });
    }
}