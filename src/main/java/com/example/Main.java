package com.example;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/api/";
    public static void main(String[] args) throws Exception {
        GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI));
        System.out.println("Api server is starting on " + BASE_URI);
        Thread.currentThread().join();
    }
}
