package com.example.rastreadordespesas;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "despesas",foreignKeys = @ForeignKey(entity = CategoryEntity.class, parentColumns = "id", childColumns = "categoriaId", onDelete = ForeignKey.CASCADE))
@TypeConverters ({Converters.class})
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double valor;
    public Date data;
    public String descricao;
    @ColumnInfo(index = true)
    public int categoriaId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
}
