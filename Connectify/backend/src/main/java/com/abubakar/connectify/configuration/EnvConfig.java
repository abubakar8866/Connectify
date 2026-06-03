package com.abubakar.connectify.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {

    private final Dotenv dotenv = Dotenv.load();

    @PostConstruct
    public void init() {

        System.setProperty("db_url", dotenv.get("DB_URL"));

        System.setProperty("db_username", dotenv.get("DB_USERNAME"));

        System.setProperty("db_password", dotenv.get("DB_PASSWORD"));

        System.setProperty("jwt_initial_secret_key", dotenv.get("JWT_INITIAL_SECRET_KEY"));

        System.setProperty("jwt_expiration", dotenv.get("JWT_EXPIRATION"));

        System.setProperty("host", dotenv.get("HOST"));

        System.setProperty("port", dotenv.get("PORT"));

        System.setProperty("email_username", dotenv.get("EMAIL_USERNAME"));

        System.setProperty("app_password", dotenv.get("APP_PASSWORD"));

        System.setProperty("smtp_auth", dotenv.get("SMTP_AUTH"));

        System.setProperty("smtp_starttls_enable", dotenv.get("SMTP_STARTTLS_ENABLE"));

        System.setProperty("smtp_starttls_required", dotenv.get("SMTP_STARTTLS_REQUIRED"));

        System.setProperty("google_client_id", dotenv.get("GOOGLE_CLIENT_ID"));

        System.setProperty("google_client_secret", dotenv.get("GOOGLE_CLIENT_SECRET"));

        System.setProperty("github_client_id", dotenv.get("GITHUB_CLIENT_ID"));

        System.setProperty("github_client_secret", dotenv.get("GITHUB_CLIENT_SECRET"));

        System.out.println("ENV LOADING COMPLETED SUCCESSFULLY");
    }

}

