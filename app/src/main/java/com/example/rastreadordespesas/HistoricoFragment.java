package com.example.rastreadordespesas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment; // Extends Fragment

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoricoFragment extends Fragment { // Extends Fragment

    // Componentes visuais e de dados
    private ListView listViewHistorico;
    private ArrayAdapter<String> despesaAdapter;
    private ArrayList<String> despesaDisplayList;
    private List<ExpenseEntity> listaDeDespesas; // Para saber o ID
    private AppDatabase db;

    // Construtor vazio (necessário para Fragments)
    public HistoricoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla (carrega) o layout XML para este fragmento
        return inflater.inflate(R.layout.fragment_historico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MUDANÇA: O código que estava no onCreate da Activity, agora vem aqui.

        // Ajuste do EdgeToEdge (usa 'view.findViewById')
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main_historico), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // MUDANÇA: Usamos 'getContext()' para o banco de dados
        db = AppDatabase.getDatabase(getContext());

        // MUDANÇA: Usamos 'view.findViewById' para os componentes
        listViewHistorico = view.findViewById(R.id.listViewHistorico);

        // Inicialização das listas
        listaDeDespesas = new ArrayList<>();
        despesaDisplayList = new ArrayList<>();

        // MUDANÇA: Usamos 'getContext()' para o Adapter
        despesaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, despesaDisplayList);
        listViewHistorico.setAdapter(despesaAdapter);

        // Configuração do clique na lista
        listViewHistorico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExpenseEntity despesaClicada = listaDeDespesas.get(position);

                // MUDANÇA: Usamos 'getContext()' para criar o Intent
                Intent intent = new Intent(getContext(), DetalheDespesaActivity.class);
                intent.putExtra("DESPESA_ID", despesaClicada.getId());

                startActivity(intent);
            }
        });

        // O método carregarHistoricoDoBanco() será chamado no onResume()
    }

    @Override
    public void onResume() {
        super.onResume();
        // MUDANÇA IMPORTANTE: Carregamos os dados no onResume().
        // Isso garante que a lista seja atualizada toda vez que o usuário
        // voltar para esta tela (ex: após deletar ou editar um item).
        carregarHistoricoDoBanco();
    }

    private void carregarHistoricoDoBanco() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CategoryEntity> todasCategorias = db.categoryDao().getAllCategories();
                Map<Integer, String> mapaCategorias = new HashMap<>();

                for (CategoryEntity cat : todasCategorias) {
                    mapaCategorias.put(cat.getId(), cat.getName());
                }

                listaDeDespesas = db.expenseDao().getAllExpenses(); // Preenche a lista de objetos
                despesaDisplayList.clear(); // Limpa a lista de exibição

                for (ExpenseEntity despesa : listaDeDespesas) {
                    String nomeCategoria = mapaCategorias.get(despesa.getCategoriaId());
                    if (nomeCategoria == null) {
                        nomeCategoria = "Sem categoria";
                    }

                    String dataFormatada = sdf.format(despesa.getData());
                    // MUDANÇA: Usando Locale para String.format
                    String valorFormatado = String.format(Locale.getDefault(), "%.2f", despesa.getValor());

                    String itemLista = dataFormatada + ": R$ " + valorFormatado + " (" + nomeCategoria + ")";

                    if (despesa.getDescricao() != null && !despesa.getDescricao().isEmpty()) {
                        itemLista += "\n  " + despesa.getDescricao(); // O recuo com 2 espaços ajuda
                    }

                    despesaDisplayList.add(itemLista);
                }

                // MUDANÇA: Usamos 'getActivity().runOnUiThread'
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            despesaAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
}