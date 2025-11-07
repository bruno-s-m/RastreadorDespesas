package com.example.rastreadordespesas;

import android.content.Intent; // Import para o Intent
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // Import para o OnItemClickListener
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoriasFragment extends Fragment {

    //adicionar categorias
    private EditText txtCategoriaNome;
    private EditText txtCategoriaLimite;
    private Button btnSalvarCategoria;
    //banco de dados
    private AppDatabase db;
    //lista categoria
    private ListView listviewCategorias;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayList<String> categoryList; // Lista para exibição (Strings)
    private List<CategoryEntity> loadedCategories; // Lista de objetos (para IDs)

    // Construtor vazio (necessário para Fragments)
    public CategoriasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla (carrega) o layout XML para este fragmento
        return inflater.inflate(R.layout.fragment_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ajuste do layout (usando o ID 'main_category' do seu XML)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main_category), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Conectando os componentes visuais
        txtCategoriaNome = view.findViewById(R.id.txtCategoriaNome);
        txtCategoriaLimite = view.findViewById(R.id.txtCategoriaLimite);
        btnSalvarCategoria = view.findViewById(R.id.btnSalvarCategoria);
        listviewCategorias = view.findViewById(R.id.listViewCategorias);

        // Inicializando o banco de dados
        db = AppDatabase.getDatabase(getContext());

        // Configurando a ListView e o Adapter
        categoryList = new ArrayList<>();
        loadedCategories = new ArrayList<>(); // Inicializa a lista de objetos
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categoryList);
        listviewCategorias.setAdapter(categoryAdapter);

        // Configurando o clique para Salvar Categoria
        btnSalvarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarNovaCategoria();
            }
        });

        // Configurando o clique nos itens da lista (para Editar/Excluir)
        listviewCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pega a categoria clicada da nossa lista de objetos
                CategoryEntity categoriaClicada = loadedCategories.get(position);

                // Cria o Intent para a tela de DetalheCategoria
                Intent intent = new Intent(getContext(), DetalheCategoriaActivity.class);

                // Coloca o ID da categoria no Intent
                intent.putExtra("CATEGORIA_ID", categoriaClicada.getId());

                // Abre a tela de detalhes
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Carrega (ou recarrega) a lista toda vez que esta tela fica visível
        // Isso atualiza a lista após sairmos da tela de detalhes
        carregarCategoriasDoBanco();
    }

    private void salvarNovaCategoria() {
        String nomeCategoria = txtCategoriaNome.getText().toString();
        String limiteCategoriaStr = txtCategoriaLimite.getText().toString();

        if (nomeCategoria.isEmpty()) {
            Toast.makeText(getContext(), "Preencha o nome da categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double limiteCategoria = 0.0;
        if (!limiteCategoriaStr.isEmpty()) {
            try {
                limiteCategoria = Double.parseDouble(limiteCategoriaStr.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Limite Inválido. Use apenas números", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final CategoryEntity novaCategoria = new CategoryEntity();
        novaCategoria.setName(nomeCategoria); // Verifique se o método é setName ou setNome
        novaCategoria.setLimiteMensal(limiteCategoria);

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.categoryDao().insertCategory(novaCategoria);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();
                            txtCategoriaNome.setText("");
                            txtCategoriaLimite.setText("");

                            // Recarrega a lista após salvar
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
                // Busca os objetos completos do banco
                loadedCategories = db.categoryDao().getAllCategories();

                // Prepara a lista de Strings para exibição
                categoryList.clear();
                for (CategoryEntity categoria : loadedCategories) {
                    String limiteFormatado = String.format(Locale.getDefault(), "%.2f", categoria.getLimiteMensal());
                    categoryList.add(categoria.getName() + " (Limite: R$ " + limiteFormatado + ")");
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Notifica o adapter que os dados mudaram
                            categoryAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
}