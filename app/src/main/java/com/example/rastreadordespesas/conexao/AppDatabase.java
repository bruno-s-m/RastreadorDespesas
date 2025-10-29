package com.example.rastreadordespesas.conexao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.rastreadordespesas.Converters;
import com.example.rastreadordespesas.DAO.CategoryDao;
import com.example.rastreadordespesas.DAO.ExpenseDao;
import com.example.rastreadordespesas.Entity.CategoryEntity;
import com.example.rastreadordespesas.Entity.ExpenseEntity;

@Database(entities = {CategoryEntity.class, ExpenseEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryDao categoryDao();
    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "rastreador_despesas_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
