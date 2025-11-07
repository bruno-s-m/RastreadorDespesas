package com.example.rastreadordespesas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //categorias
    private Button btnIrParaCategorias;

    //despesas
    private Button btnAdicionarDespesa;
    //historico
    private Button btnIrParaHistorico;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnIrParaCategorias = findViewById(R.id.btnIrParaCategorias);
        btnIrParaCategorias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria a intenção de abrir a tela de categorias
                android.content.Intent intent = new android.content.Intent(MainActivity.this, CategoriasActivity.class);
                startActivity(intent);
            }
        });

        btnAdicionarDespesa = findViewById(R.id.btnAdicionarDespesa);
        btnAdicionarDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria a intenção de abrir a tela de adicionar despesa
                Intent intent = new Intent(MainActivity.this, AdicionarDespesaActivity.class);
                startActivity(intent);
            }
        });

        btnIrParaHistorico = findViewById(R.id.btnIrParaHistorico);
        btnIrParaHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria a intenção de abrir a tela de histórico
                Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
                startActivity(intent);
            }
        });
    }
}
