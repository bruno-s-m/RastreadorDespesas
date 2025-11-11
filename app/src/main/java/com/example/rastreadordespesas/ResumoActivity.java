package com.example.rastreadordespesas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumoActivity extends AppCompatActivity {

    // --- Variáveis de UI ---
    private TextView txtResumoOrcamento;
    private ListView listViewDespesas;
    private Button btnVoltar;

    // --- Variáveis de Dados ---
    private ArrayAdapter<String> expenseAdapter;
    private ArrayList<String> expenseList;
    private List<ExpenseEntity> listaDeDespesas;
    private List<CategoryEntity> listaDeCategoriasGlobal; // NOVO: Para o Spinner de edição
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumo);

        db = AppDatabase.getDatabase(getApplicationContext());

        // --- Inicialização UI ---
        txtResumoOrcamento = findViewById(R.id.txtResumoOrcamento);
        listViewDespesas = findViewById(R.id.listViewDespesas);
        btnVoltar = findViewById(R.id.btnVoltar);

        // --- Inicialização Lista/Adapter ---
        expenseList = new ArrayList<>();
        listaDeDespesas = new ArrayList<>();
        listaDeCategoriasGlobal = new ArrayList<>(); // INICIALIZA
        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseList);
        listViewDespesas.setAdapter(expenseAdapter);


        // --- Listener para Excluir/Editar Despesa (MODIFICADO) ---
        listViewDespesas.setOnItemLongClickListener((parent, view, position, id) -> {
            ExpenseEntity despesaSelecionada = listaDeDespesas.get(position);
            // Chama o novo diálogo de opções
            mostrarDialogoOpcoesDespesa(despesaSelecionada);
            return true;
        });

        // --- Listener Botão Voltar ---
        btnVoltar.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega tudo ao entrar na tela
        carregarTodasCategorias(); // NOVO: Carrega categorias para o spinner
        carregarDespesasDoBanco();
        atualizarResumoOrcamento();
    }

    /**
     * NOVO: Carrega as categorias para usar no Spinner de edição
     */
    private void carregarTodasCategorias() {
        new Thread(() -> {
            listaDeCategoriasGlobal = db.categoryDao().getAllCategories();
        }).start();
    }

    // =================================================================================
    // --- MÉTODOS DE DIÁLOGO (CRUD) ---
    // =================================================================================

    /**
     * NOVO: Mostra as opções "Excluir" ou "Editar" para uma despesa
     */
    private void mostrarDialogoOpcoesDespesa(ExpenseEntity despesa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opções para a Despesa");
        builder.setItems(new CharSequence[]{"Excluir", "Editar"}, (dialog, which) -> {
            switch (which) {
                case 0: // Excluir
                    mostrarDialogoExcluirDespesa(despesa);
                    break;
                case 1: // Editar
                    mostrarDialogoEditarDespesa(despesa);
                    break;
            }
        });
        builder.show();
    }

    /**
     * NOVO: Diálogo para editar a despesa (Requisito 3.1 - Update)
     */
    private void mostrarDialogoEditarDespesa(ExpenseEntity despesa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Despesa");

        // Infla o layout customizado (form_edit_despesa.xml)
        View dialogView = getLayoutInflater().inflate(R.layout.form_edit_despesa, null);
        builder.setView(dialogView);

        // Pega as referências dos componentes do diálogo
        EditText editValor = dialogView.findViewById(R.id.editValorDespesa);
        EditText editDescricao = dialogView.findViewById(R.id.editDescricaoDespesa);
        TextView editData = dialogView.findViewById(R.id.editDataDespesa);
        Spinner editSpinner = dialogView.findViewById(R.id.editSpinnerCategorias);

        // --- Preenche os campos com os dados existentes ---
        editValor.setText(String.format(Locale.US, "%.2f", despesa.getValor()));
        editDescricao.setText(despesa.getDescricao());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editData.setText(sdf.format(despesa.getData()));

        // 'final' array para ser acessível dentro do lambda
        final Date[] novaData = { despesa.getData() };

        editData.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(novaData[0]); // Começa com a data atual da despesa
            new DatePickerDialog(ResumoActivity.this, (view, year, month, day) -> {
                cal.set(year, month, day);
                novaData[0] = cal.getTime();
                editData.setText(sdf.format(novaData[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // --- Popula o Spinner ---
        ArrayList<String> nomesSpinner = new ArrayList<>();
        int selectionIndex = 0;
        // Espera-se que listaDeCategoriasGlobal já tenha sido carregada no onResume()
        for (int i = 0; i < listaDeCategoriasGlobal.size(); i++) {
            nomesSpinner.add(listaDeCategoriasGlobal.get(i).getName());
            // Encontra qual categoria deve vir pré-selecionada
            if (listaDeCategoriasGlobal.get(i).getId() == despesa.getCategoriaId()) {
                selectionIndex = i;
            }
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomesSpinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSpinner.setAdapter(spinnerAdapter);
        editSpinner.setSelection(selectionIndex);

        // --- Configura botões do Diálogo ---
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            // Pega os novos valores
            String novoValorStr = editValor.getText().toString();
            String novaDesc = editDescricao.getText().toString();

            double novoValor;
            try {
                novoValor = Double.parseDouble(novoValorStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            int novaCatPos = editSpinner.getSelectedItemPosition();
            int novaCatId = listaDeCategoriasGlobal.get(novaCatPos).getId();

            // Atualiza o objeto 'despesa' original
            despesa.setValor(novoValor);
            despesa.setDescricao(novaDesc);
            despesa.setData(novaData[0]); // Pega a data (potencialmente nova)
            despesa.setCategoriaId(novaCatId); // Pega a categoria (potencialmente nova)

            // Salva no banco
            atualizarDespesa(despesa);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void atualizarDespesa(ExpenseEntity despesa) {
        new Thread(() -> {
            db.expenseDao().updateExpense(despesa);
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show();
                carregarDespesasDoBanco();
                atualizarResumoOrcamento();
            });
        }).start();
    }

    // =================================================================================
    // --- MÉTODOS DE CARREGAMENTO (Sem alterações) ---
    // =================================================================================

    private void carregarDespesasDoBanco() {
        new Thread(() -> {
            listaDeDespesas = db.expenseDao().getAllExpenses();
            expenseList.clear();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            for (ExpenseEntity despesa : listaDeDespesas) {
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

    private void atualizarResumoOrcamento() {
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            long inicioMes = cal.getTimeInMillis();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
            long fimMes = cal.getTimeInMillis();
            List<CategoryEntity> todasCategorias = db.categoryDao().getAllCategories();
            StringBuilder resumo = new StringBuilder();
            for (CategoryEntity categoria : todasCategorias) {
                double totalGasto = db.expenseDao().getTotalExpensesForCategory(categoria.getId(), inicioMes, fimMes);
                double limite = categoria.getLimiteMensal();
                String gastoFormatado = String.format("%.2f", totalGasto);
                String limiteFormatado = String.format("%.2f", limite);
                resumo.append(categoria.getName() + ": R$ " + gastoFormatado + " de R$ " + limiteFormatado);
                if (limite > 0 && totalGasto > limite) {
                    resumo.append(" (ESTOURADO!)");
                }
                resumo.append("\n");
            }
            if (resumo.length() == 0) {
                resumo.append("Nenhuma categoria cadastrada.");
            }
            runOnUiThread(() -> {
                txtResumoOrcamento.setText(resumo.toString());
            });
        }).start();
    }

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
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa excluída", Toast.LENGTH_SHORT).show();
                carregarDespesasDoBanco();
                atualizarResumoOrcamento();
            });
        }).start();
    }
}