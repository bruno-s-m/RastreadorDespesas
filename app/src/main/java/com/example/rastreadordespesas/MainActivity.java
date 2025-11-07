package com.example.rastreadordespesas;

import android.os.Bundle;
// Remova os imports antigos de Button, EditText, Spinner, etc.

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// Não precisamos mais de EdgeToEdge ou ViewCompat aqui,
// pois os fragmentos vão cuidar disso.

public class MainActivity extends AppCompatActivity {

    // A MainActivity agora só precisa saber sobre a navegação.
    // Todas as outras variáveis (EditTexts, botões, etc.)
    // foram movidas para suas respectivas Activities ou Fragments.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define o layout que tem a barra de navegação
        setContentView(R.layout.activity_main);

        // 1. Encontra a barra de navegação no layout
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

        // 2. Encontra o "Contêiner de Fragmentos" (NavHostFragment)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 3. Obtém o "Controlador de Navegação"
        NavController navController = navHostFragment.getNavController();

        // 4. Conecta a barra de navegação ao controlador
        // Isso faz com que clicar nos botões da barra (Dashboard, Histórico, Categorias)
        // automaticamente troque os fragmentos no contêiner.
        NavigationUI.setupWithNavController(navView, navController);

        // 5. (Opcional) Configura a barra de ação (título) para mudar com a navegação
        // NavigationUI.setupActionBarWithNavController(this, navController);
    }
}