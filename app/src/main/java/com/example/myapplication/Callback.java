package com.example.myapplication;

public interface Callback<T> {
    void onSuccess(T result);

    void onError(Exception e);
}