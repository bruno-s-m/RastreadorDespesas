package com.example.rastreadordespesas;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double limiteMensal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLimiteMensal() {
        return limiteMensal;
    }

    public void setLimiteMensal(double limiteMensal) {
        this.limiteMensal = limiteMensal;
    }
}
