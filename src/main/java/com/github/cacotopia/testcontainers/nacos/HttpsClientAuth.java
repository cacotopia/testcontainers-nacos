package com.github.cacotopia.testcontainers.nacos;

public enum HttpsClientAuth {

    NONE,
    REQUEST,
    REQUIRED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
