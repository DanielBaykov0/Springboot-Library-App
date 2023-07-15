package baykov.daniel.springbootlibraryapp.service;

public interface EmailService {

    void send(String receiver, String email);

    String buildEmail(String name, String link);

    String buildEmailForgotPassword(String name, String link);

    String buildEmailChangePassword(String name);
}
