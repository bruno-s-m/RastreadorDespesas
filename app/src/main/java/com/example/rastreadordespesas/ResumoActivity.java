package com.example.rastreadordespesas;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ResumoActivity extends AppCompatActivity {

    private TextView txtResumoOrcamento;
    private ListView listViewDespesas;
    private ArrayAdapter<String> expenseAdapter;
    private ArrayList<String> expenseList;
    private List<ExpenseEntity> listaDeDespesas;
    private Button btnVoltar;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumo);

        // --- Inicialização do Banco de Dados ---
        db = AppDatabase.getDatabase(getApplicationContext());

        // --- Inicialização UI ---
        txtResumoOrcamento = findViewById(R.id.txtResumoOrcamento);
        listViewDespesas = findViewById(R.id.listViewDespesas);
        btnVoltar = findViewById(R.id.btnVoltar);

        // --- Inicialização Lista/Adapter ---
        expenseList = new ArrayList<>();
        listaDeDespesas = new ArrayList<>();
        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseList);
        listViewDespesas.setAdapter(expenseAdapter);


        // --- Listener para Excluir Despesa (Requisito 3.1 - CRUD) ---
        listViewDespesas.setOnItemLongClickListener((parent, view, position, id) -> {
            ExpenseEntity despesaSelecionada = listaDeDespesas.get(position);
            mostrarDialogoExcluirDespesa(despesaSelecionada);
            return true; // Consumiu o clique longo
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Colocamos no onResume() para que os dados sejam atualizados
        // toda vez que o utilizador voltar para esta tela.
        carregarDespesasDoBanco();
        atualizarResumoOrcamento();
    }

    // =================================================================================
    // --- MÉTODOS MOVIDOS DA MAINACTIVITY ---
    // =================================================================================

    /**
     * Carrega e exibe o histórico de despesas (Requisito 3.1 - Read)
     */
    private void carregarDespesasDoBanco() {
        new Thread(() -> {
            listaDeDespesas = db.expenseDao().getAllExpenses(); // Atualiza a lista de entidades
            expenseList.clear(); // Limpa a lista de strings

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            for (ExpenseEntity despesa : listaDeDespesas) {
                // Para exibir a categoria, buscamos pelo ID
                CategoryEntity categoria = db.categoryDao().getCategoryById(despesa.getCategoriaId());
                String nomeCategoria = (categoria != null) ? categoria.getName() : "Sem Categoria";

                String valorFormatado = String.format("%.2f", despesa.getValor());
                String dataFormatada = sdf.format(despesa.getData());

                expenseList.add(dataFormatada + " - " + nomeCategoria + " - R$ " + valorFormatado
                        + "\n(" + despesa.getDescricao() + ")");
            }

            runOnUiThread(() -> {
                expenseAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    /**
     * Calcula o total gasto por categoria no mês corrente (Requisitos 5.2 e 5.3)
     */
    private void atualizarResumoOrcamento() {
        new Thread(() -> {
            // 1. Define o início e o fim do mês corrente
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1); // Primeiro dia do mês
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            long inicioMes = cal.getTimeInMillis();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Último dia
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
            long fimMes = cal.getTimeInMillis();

            // 2. Busca todas as categorias
            List<CategoryEntity> todasCategorias = db.categoryDao().getAllCategories();
            StringBuilder resumo = new StringBuilder();

            for (CategoryEntity categoria : todasCategorias) {
                // 3. Para cada categoria, busca o total de gastos no período
                double totalGasto = db.expenseDao().getTotalExpensesForCategory(categoria.getId(), inicioMes, fimMes);
                double limite = categoria.getLimiteMensal();

                String gastoFormatado = String.format("%.2f", totalGasto);
                String limiteFormatado = String.format("%.2f", limite);

                // 4. Constrói a string de resumo
                resumo.append(categoria.getName() + ": R$ " + gastoFormatado + " de R$ " + limiteFormatado);

                // 5. Adiciona o Alerta Visual (Requisito 5.3)
                if (limite > 0 && totalGasto > limite) {
                    resumo.append(" (ESTOURADO!)"); // 🚨 Alerta Visual
                }
                resumo.append("\n");
            }

            if (resumo.length() == 0) {
                resumo.append("Nenhuma categoria cadastrada.");
            }

            // 6. Atualiza o TextView na UI Thread
            runOnUiThread(() -> {
                txtResumoOrcamento.setText(resumo.toString());
            });

        }).start();
    }

    /**
     * NOVO: Mostra confirmação para excluir a despesa (Requisito 3.1 - Delete)
     */
    private void mostrarDialogoExcluirDespesa(ExpenseEntity despesa) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir esta despesa?")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> excluirDespesa(despesa))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirDespesa(ExpenseEntity despesa) {
        new Thread(() -> {
            db.expenseDao().deleteExpense(despesa);
            // Recarrega tudo na UI thread
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa excluída", Toast.LENGTH_SHORT).show();
                carregarDespesasDoBanco();   // Recarrega a lista
                atualizarResumoOrcamento(); // Atualiza o resumo
            });
        }).start();
    }
}