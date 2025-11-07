package com.example.rastreadordespesas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Importante para Fragments
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Importante para Fragments
import androidx.annotation.Nullable; // Importante para Fragments
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment; // MUDANÇA IMPORTANTE

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import para String.format

public class CategoriasFragment extends Fragment { // MUDANÇA: Extends Fragment

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

    // Construtor vazio (necessário para Fragments)
    public CategoriasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla (carrega) o layout XML para este fragmento
        // MUDANÇA: Não usamos setContentView. Inflamos a view e a retornamos.
        return inflater.inflate(R.layout.fragment_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MUDANÇA: O código que estava no onCreate da Activity, agora vem aqui.
        // Precisamos do 'view' para encontrar os componentes.

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main_category), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // MUDANÇA: Usamos 'view.findViewById'
        txtCategoriaNome = view.findViewById(R.id.txtCategoriaNome);
        txtCategoriaLimite = view.findViewById(R.id.txtCategoriaLimite);
        btnSalvarCategoria = view.findViewById(R.id.btnSalvarCategoria);
        listviewCategorias = view.findViewById(R.id.listViewCategorias);

        // MUDANÇA: Usamos 'getContext()' em vez de 'this' para o Contexto
        db = AppDatabase.getDatabase(getContext());

        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categoryList);
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
            // MUDANÇA: Usamos 'getContext()'
            Toast.makeText(getContext(), "Preencha o nome da categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double limiteCategoria = 0.0;
        if (!limiteCategoriaStr.isEmpty()) {
            try {
                limiteCategoria = Double.parseDouble(limiteCategoriaStr);
            } catch (NumberFormatException e) {
                // MUDANÇA: Usamos 'getContext()'
                Toast.makeText(getContext(), "Limite Inválido. Use apenas números", Toast.LENGTH_SHORT).show();
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

                // MUDANÇA: Usamos 'getActivity().runOnUiThread'
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();
                            txtCategoriaNome.setText("");
                            txtCategoriaLimite.setText("");

                            carregarCategoriasDoBanco();
                        }
                    });
                }
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
                    // MUDANÇA: Usando Locale para String.format
                    String limiteFormatado = String.format(Locale.getDefault(), "%.2f", categoria.getLimiteMensal());
                    categoryList.add(categoria.getName() + " (Limite: R$ " + limiteFormatado + " )");
                }

                // MUDANÇA: Usamos 'getActivity().runOnUiThread'
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            categoryAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
}