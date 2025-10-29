package com.example.rastreadordespesas;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(ExpenseEntity expense);

    @Query("SELECT*FROM despesas ORDER BY data DESC")
    List<ExpenseEntity> getAllExpenses();
    @Update
    void updateExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT*FROM despesas WHERE id= :expenseId LIMIT 1")
    ExpenseEntity getExpenseById(int expenseId);

    @Query("SELECT*FROM despesas WHERE data >= :startDate AND data <= :endDate ORDER BY data DESC")
    List<ExpenseEntity> getExpensesBetweenDates(long startDate, long endDate);

    @Query("SELECT SUM(valor) FROM despesas WHERE categoriaId = :categoryId AND data >= :startDate AND data <= :endDate")
    double getTotalExpensesForCategory(int categoryId, long startDate, long endDate);

}
