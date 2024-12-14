package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProcessor implements Runnable {

    private Socket socket;
    private String serverName;

    public ClientProcessor(Socket socket, String serverName) {
        this.socket = socket;
        this.serverName = serverName;
    }

    // Função para validar comandos SMTP e endereços de e-mail
    public String validarMensagem(String message) {
        String emailRegex = ("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]");
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {

            // Envia mensagem inicial de boas-vindas
            out.println("220 <" + this.serverName + "> Simple Dinner's Philosopers in SMTP protocol\r");

            String message;
            

            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message); // Debugando output no server
               
                if (message.startsWith("HELO")) {
                    out.println("250 " + this.serverName + " Hello ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}