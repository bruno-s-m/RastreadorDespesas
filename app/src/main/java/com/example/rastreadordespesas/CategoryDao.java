package com.example.rastreadordespesas;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insertCategory (CategoryEntity category);
    @Query("SELECT * FROM categorias ORDER BY name ASC")
    List<CategoryEntity> getAllCategories();
    @Update
    void updateCategory(CategoryEntity category);
    @Delete
    void deleteCategory(CategoryEntity category);
    @Query("SELECT*FROM categorias WHERE id = :categoryId LIMIT 1")
    CategoryEntity getCategoryById(int categoryId);

}
