package com.example.rastreadordespesas;

// Imports necessários
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

    // --- VARIÁVEIS DA CLASSE ---

    // Componentes Visuais (do seu activity_detalhe_categoria.xml)
    private EditText editTxtNomeCategoria;
    private EditText editTxtLimiteCategoria;
    private Button btnSalvarCategoriaAlteracoes;
    private Button btnExcluirCategoria;

    // Dados
    private AppDatabase db;
    private CategoryEntity categoriaAtual; // A categoria que estamos editando
    private int categoriaId;

    // --- CICLO DE VIDA DA ACTIVITY ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_categoria); // Carrega o layout que você criou

        // Ajuste do layout (use o ID do seu layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_detalhe_categoria), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Pega o ID enviado pelo CategoriasFragment
        categoriaId = getIntent().getIntExtra("CATEGORIA_ID", -1);
        if (categoriaId == -1) {
            // Se não recebeu um ID, mostra erro e fecha
            Toast.makeText(this, "Erro ao carregar categoria.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializa o banco de dados
        db = AppDatabase.getDatabase(getApplicationContext());

        // Conecta os componentes visuais
        editTxtNomeCategoria = findViewById(R.id.editTxtNomeCategoria);
        editTxtLimiteCategoria = findViewById(R.id.editTxtLimiteCategoria);
        btnSalvarCategoriaAlteracoes = findViewById(R.id.btnSalvarCategoriaAlteracoes);
        btnExcluirCategoria = findViewById(R.id.btnExcluirCategoria);

        // Carrega os dados da categoria e preenche os campos
        carregarDadosDaCategoria();

        // Configura os botões
        btnSalvarCategoriaAlteracoes.setOnClickListener(v -> salvarAlteracoes());
        btnExcluirCategoria.setOnClickListener(v -> excluirCategoria());
    }

    // --- MÉTODOS DE LÓGICA ---

    /**
     * Busca a Categoria no banco (em background) e preenche
     * os campos EditText com os dados atuais.
     */
    private void carregarDadosDaCategoria() {
        new Thread(() -> {
            // Busca a categoria no banco usando o ID
            categoriaAtual = db.categoryDao().getCategoryById(categoriaId);

            if (categoriaAtual == null) {
                // Segurança: se a categoria não for encontrada
                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoria não encontrada.", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Preenche os campos na UI Thread
            runOnUiThread(() -> {
                editTxtNomeCategoria.setText(categoriaAtual.getName());
                // Formata o limite como "123.45"
                editTxtLimiteCategoria.setText(String.format(Locale.US, "%.2f", categoriaAtual.getLimiteMensal()));
            });
        }).start();
    }

    /**
     * Pega os novos dados dos campos, valida, e dá UPDATE no banco.
     */
    private void salvarAlteracoes() {
        String nome = editTxtNomeCategoria.getText().toString();
        String limiteStr = editTxtLimiteCategoria.getText().toString();

        // Validação
        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = 0.0;
        if (!limiteStr.isEmpty()) {
            try {
                // Usar replace para garantir que vírgula também funcione
                limite = Double.parseDouble(limiteStr.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor de limite inválido.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Atualiza o objeto que já temos na memória
        categoriaAtual.setName(nome);
        categoriaAtual.setLimiteMensal(limite);

        // Salva (Update) no banco em uma thread
        new Thread(() -> {
            db.categoryDao().updateCategory(categoriaAtual);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria atualizada!", Toast.LENGTH_SHORT).show();
                finish(); // Fecha a tela e volta para a lista
            });
        }).start();
    }

    /**
     * Exclui a categoria atual do banco.
     */
    private void excluirCategoria() {
        // Opcional: Adicionar um AlertDialog de confirmação aqui
        // (como fizemos na AdicionarDespesaActivity)

        new Thread(() -> {
            db.categoryDao().deleteCategory(categoriaAtual);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria excluída.", Toast.LENGTH_SHORT).show();
                finish(); // Fecha a tela e volta para a lista
            });
        }).start();
    }
}