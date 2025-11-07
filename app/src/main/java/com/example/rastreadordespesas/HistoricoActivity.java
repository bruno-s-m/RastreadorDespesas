package com.example.rastreadordespesas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoricoActivity extends AppCompatActivity {

    private ListView listViewHistorico;
    private ArrayAdapter<String> despesaAdapter;
    private ArrayList<String> despesaDisplayList;

    private List<ExpenseEntity> listaDeDespesas;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_historico), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getDatabase(getApplicationContext());

        listaDeDespesas = new ArrayList<>();

        listViewHistorico = findViewById(R.id.listViewHistorico);
        despesaDisplayList = new ArrayList<>();
        despesaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, despesaDisplayList);
        listViewHistorico.setAdapter(despesaAdapter);



        listViewHistorico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExpenseEntity despesaClicada = listaDeDespesas.get(position);

                Intent intent = new Intent(HistoricoActivity.this, DetalheDespesaActivity.class);
                intent.putExtra("DESPESA_ID", despesaClicada.getId());

                startActivity(intent);
            }

        });
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
                    String valorFormatado = String.format("%.2f", despesa.getValor());

                    String itemLista = dataFormatada + ": R$ " + valorFormatado + " ('" + nomeCategoria + "')";

                    if (despesa.getDescricao() != null && !despesa.getDescricao().isEmpty()) {
                        itemLista += "\n " + despesa.getDescricao();
                    }

                    despesaDisplayList.add(itemLista);

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        despesaAdapter.notifyDataSetChanged();
                    }
                });


            }
        }).start();

    }


}