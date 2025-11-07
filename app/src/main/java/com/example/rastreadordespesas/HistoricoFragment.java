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

public class HistoricoFragment extends Fragment {

    private ListView listViewHistorico;
    private ArrayAdapter<String> despesaAdapter;
    private ArrayList<String> despesaDisplayList;
    private List<ExpenseEntity> listaDeDespesas;
    private AppDatabase db;


    public HistoricoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main_historico), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getDatabase(getContext());

        listViewHistorico = view.findViewById(R.id.listViewHistorico);

        listaDeDespesas = new ArrayList<>();
        despesaDisplayList = new ArrayList<>();

        despesaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, despesaDisplayList);
        listViewHistorico.setAdapter(despesaAdapter);

        listViewHistorico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExpenseEntity despesaClicada = listaDeDespesas.get(position);

                Intent intent = new Intent(getContext(), DetalheDespesaActivity.class);
                intent.putExtra("DESPESA_ID", despesaClicada.getId());

                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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

                listaDeDespesas = db.expenseDao().getAllExpenses();
                despesaDisplayList.clear();

                for (ExpenseEntity despesa : listaDeDespesas) {
                    String nomeCategoria = mapaCategorias.get(despesa.getCategoriaId());
                    if (nomeCategoria == null) {
                        nomeCategoria = "Sem categoria";
                    }

                    String dataFormatada = sdf.format(despesa.getData());
                    String valorFormatado = String.format(Locale.getDefault(), "%.2f", despesa.getValor());

                    String itemLista = dataFormatada + ": R$ " + valorFormatado + " (" + nomeCategoria + ")";

                    if (despesa.getDescricao() != null && !despesa.getDescricao().isEmpty()) {
                        itemLista += "\n  " + despesa.getDescricao();
                    }

                    despesaDisplayList.add(itemLista);
                }

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