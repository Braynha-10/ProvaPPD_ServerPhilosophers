package src;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class PhilosopherClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    public int id;
    private String Name;

    public static void main(String[] args) throws InterruptedException {
        PhilosopherClient client = new PhilosopherClient();
        client.start();
    }

    public void start() throws InterruptedException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("HELLO");
            String response = in.readLine();
            id = Integer.parseInt(response.split(" ")[1]);
            System.out.println("Conectado como filósofo ID: " + id);

            Random random = new Random();
           
            Scanner scanner = new Scanner(System.in);
            Name = scanner.nextLine();

            scanner.close(); 

            out.println("LOGIN: " + Name);
            while (true) {
                think(out, in, random);
                requestForks(out, in);
                eat(out, in, random);
                releaseForks(out);
            }

        } catch (IOException e) {
            System.err.println("Erro ao conectar com o servidor.");
            e.printStackTrace();
        }
    }

    private void think(PrintWriter out, BufferedReader in, Random random) throws InterruptedException {
        out.println("THINK");
        // int thinkingTime = 1000; Teste com tempo fixo para pensar
        int thinkingTime = Math.max(0, (int) (random.nextGaussian() * 2 + 5) * 1000);
        System.out.println("Filósofo " + Name + " pensando por " + thinkingTime + "ms");
        Thread.sleep(thinkingTime);
    }

    private void requestForks(PrintWriter out, BufferedReader in) throws IOException {
        out.println("REQUEST_FORKS");
        String response = in.readLine();
        if (response.equals("FORKS_DENIED")) {
            System.out.println("Filósofo " + Name + " esperando pelos garfos.");
        } else {
            System.out.println("Filósofo " + Name + " pegou os garfos.");
        }
    }

    private void eat(PrintWriter out, BufferedReader in, Random random) throws InterruptedException {
        out.println("EAT");
        int eatingTime = 2000; // Tempo fixo para comer
        System.out.println("Filósofo " + Name + " comendo por " + eatingTime + "ms");
        Thread.sleep(eatingTime);
    }

    private void releaseForks(PrintWriter out) {
        out.println("RELEASE_FORKS");
        System.out.println("Filósofo " + Name + " liberou os garfos.");
    }
}
