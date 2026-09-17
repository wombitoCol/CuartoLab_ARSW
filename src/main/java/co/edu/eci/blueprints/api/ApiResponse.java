package co.edu.eci.blueprints.api;

public record ApiResponse<T>(int code, String message, T data) { }
