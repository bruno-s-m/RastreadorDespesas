package com.example.rastreadordespesas;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity {
    //adicionar categorias
    private EditText txtCategoriaNome;
    private EditText txtCategoriaLimite;
    private Button btnSalvarCategoria;
    //banco de dados
    private AppDatabase db;
    //lista categoria
    private ListView listviewCategorias;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayList<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_category), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCategoriaNome = findViewById(R.id.txtCategoriaNome);
        txtCategoriaLimite = findViewById(R.id.txtCategoriaLimite);
        btnSalvarCategoria = findViewById(R.id.btnSalvarCategoria);

        db = AppDatabase.getDatabase(getApplicationContext());

        listviewCategorias = findViewById(R.id.listViewCategorias);
        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        listviewCategorias.setAdapter(categoryAdapter);

        carregarCategoriasDoBanco();

        btnSalvarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarNovaCategoria();
            }
        });
    }

    private void salvarNovaCategoria() {
        String nomeCategoria = txtCategoriaNome.getText().toString();
        String limiteCategoriaStr = txtCategoriaLimite.getText().toString();

        if (nomeCategoria.isEmpty()) {
            Toast.makeText(this, "Preencha o nome da categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double limiteCategoria = 0.0;
        if (!limiteCategoriaStr.isEmpty()) {
            try {
                limiteCategoria = Double.parseDouble(limiteCategoriaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Limite Inválido. Use apenas números", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final CategoryEntity novaCategoria = new CategoryEntity();
        novaCategoria.setName(nomeCategoria);
        novaCategoria.setLimiteMensal(limiteCategoria);

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.categoryDao().insertCategory(novaCategoria);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CategoriasActivity.this, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();
                        txtCategoriaNome.setText("");
                        txtCategoriaLimite.setText("");

                        carregarCategoriasDoBanco();
                    }
                });
            }
        }).start();
    }

    private void carregarCategoriasDoBanco() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CategoryEntity> categoriasDoBanco = db.categoryDao().getAllCategories();
                categoryList.clear();


                for (CategoryEntity categoria : categoriasDoBanco) {
                    String limiteFormatado = String.format("%.2f", categoria.getLimiteMensal());
                    categoryList.add(categoria.getName() + "(Limite: R$ " + limiteFormatado + " )");

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        categoryAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }
}