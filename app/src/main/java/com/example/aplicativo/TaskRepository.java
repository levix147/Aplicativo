package com.example.aplicativo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final String PREF_NAME = "KanbanPrefs";
    private static final String KEY_TASKS = "tasks_list";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TaskRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTask(Task task) {
        List<Task> tasks = getTasks();
        tasks.add(task);
        saveTasksList(tasks);
    }

    public List<Task> getTasks() {
        String json = sharedPreferences.getString(KEY_TASKS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    // Método para salvar a lista completa (usado internamente e se precisar atualizar status)
    public void saveTasksList(List<Task> tasks) {
        String json = gson.toJson(tasks);
        sharedPreferences.edit().putString(KEY_TASKS, json).apply();
    }
    
    // Método para limpar todas as tarefas (útil para testes ou logout)
    public void clearTasks() {
        sharedPreferences.edit().remove(KEY_TASKS).apply();
    }
}