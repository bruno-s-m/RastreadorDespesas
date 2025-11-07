package com.example.rastreadordespesas;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class DetalheCategoriaActivity extends AppCompatActivity {

    private EditText editTxtNomeCategoria;
    private EditText editTxtLimiteCategoria;
    private Button btnSalvarCategoriaAlteracoes;
    private Button btnExcluirCategoria;
    private AppDatabase db;
    private CategoryEntity categoriaAtual;
    private int categoriaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_categoria);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_detalhe_categoria), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoriaId = getIntent().getIntExtra("CATEGORIA_ID", -1);
        if (categoriaId == -1) {
            Toast.makeText(this, "Erro ao carregar categoria.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        editTxtNomeCategoria = findViewById(R.id.editTxtNomeCategoria);
        editTxtLimiteCategoria = findViewById(R.id.editTxtLimiteCategoria);
        btnSalvarCategoriaAlteracoes = findViewById(R.id.btnSalvarCategoriaAlteracoes);
        btnExcluirCategoria = findViewById(R.id.btnExcluirCategoria);

        carregarDadosDaCategoria();

        btnSalvarCategoriaAlteracoes.setOnClickListener(v -> salvarAlteracoes());
        btnExcluirCategoria.setOnClickListener(v -> excluirCategoria());
    }

    private void carregarDadosDaCategoria() {
        new Thread(() -> {
            categoriaAtual = db.categoryDao().getCategoryById(categoriaId);

            if (categoriaAtual == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoria não encontrada.", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> {
                editTxtNomeCategoria.setText(categoriaAtual.getName());
                editTxtLimiteCategoria.setText(String.format(Locale.US, "%.2f", categoriaAtual.getLimiteMensal()));
            });
        }).start();
    }

    private void salvarAlteracoes() {
        String nome = editTxtNomeCategoria.getText().toString();
        String limiteStr = editTxtLimiteCategoria.getText().toString();

        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = 0.0;
        if (!limiteStr.isEmpty()) {
            try {

                limite = Double.parseDouble(limiteStr.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor de limite inválido.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        categoriaAtual.setName(nome);
        categoriaAtual.setLimiteMensal(limite);

        new Thread(() -> {
            db.categoryDao().updateCategory(categoriaAtual);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria atualizada!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void excluirCategoria() {

        new Thread(() -> {
            db.categoryDao().deleteCategory(categoriaAtual);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria excluída.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}