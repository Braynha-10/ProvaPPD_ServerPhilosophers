package src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceServer {
    private static final int PORT = 5000;
    private Map<Integer, PhilosopherData> philosophers = new HashMap<>();
    private boolean[] forks;
    private ReentrantLock lock = new ReentrantLock();
    private int number_clients = 0;
    private static final int NUMBER_FORKS = 3;
    private final int NUMBER_PHILOSOPHERS = 3;

    public ResourceServer(int numForks) {
        forks = new boolean[numForks]; 
    }

    public static void main(String[] args) {
        ResourceServer server = new ResourceServer(NUMBER_FORKS); 
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT);
            int id =1;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, philosophers.get(id))).start();
                id +=1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int philosopherId;
        public PhilosopherData philosopher;

        public ClientHandler(Socket clientSocket, PhilosopherData philosopher) {
            this.clientSocket = clientSocket;
            this.philosopher = philosopher;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String message;
                while ((message = in.readLine()) != null) {
                    String response = handleRequest(message);
                    if (response != null) {
                        out.println(response);
                    }
                }
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }

        private String handleRequest(String message) {
            String[] parts = message.split(" ");
            String command = parts[0];

            String loginRegex = "LOGIN: [a-zA-Z0-9]+";
            Pattern pattern = Pattern.compile(loginRegex);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                String matchedString = matcher.group();
                if (matchedString.length() > 7) {
                    String nome = matchedString.substring(7); 
                    System.out.println(nome);

                    boolean exists = false;

                    System.out.println(philosophers.size());
                    for(int j = 1; j <= philosophers.size(); j++){
                        System.out.println(philosophers.get(j).Name);
                        if(philosophers.get(j).Name.equals(nome)){
                            System.out.println("Philosopher already exists"); 
                            this.philosopherId = j;
                            this.philosopher = philosophers.get(j);
                            exists = true;
                            break;
                        }
                    }    
                    if(exists == false){   
                        if(philosophers.size() < NUMBER_PHILOSOPHERS){
                            philosopherId = philosophers.size() + 1;
                            philosophers.put(philosopherId, new PhilosopherData(nome, "CONNECTED"));
                        }
                        else{
                            return "MAXIMUM NUMBER OF PHILOSOPHERS REACHED";
                        }
                    }
                }
            }
            switch (command) {
                case "HELLO":
                    return "HI " + philosopherId;

                case "THINK":
                                    
                    philosophers.get(philosopherId).incrementThinking(philosopherId);
                    return "ACK";

                case "REQUEST_FORKS":
                    if (tryAllocateForks(philosopherId)) {
                        return "FORKS_GRANTED";
                    } else {
                        return "FORKS_DENIED";
                    }

                case "RELEASE_FORKS":
                    releaseForks(philosopherId);
                    return "ACK"; 

                case "EAT":
                    philosophers.get(philosopherId).incrementEating(philosopherId);
                    return "EAT";    

                default:
                    return "UNKNOWN_COMMAND";
            }
        }

        private boolean tryAllocateForks(int id) {
            if(id > forks.length){
                return false;
            }
            lock.lock();
            try {
                int leftFork = id - 1;
                int rightFork = id % forks.length;

                if (!forks[leftFork] && !forks[rightFork]) {
                    forks[leftFork] = true;
                    forks[rightFork] = true;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        private void releaseForks(int id) {
            lock.lock();
            try {
                int leftFork = id - 1;
                int rightFork = id % forks.length;
                forks[leftFork] = false;
                forks[rightFork] = false;
            } finally {
                lock.unlock();
            }
        }
    }

    private static class PhilosopherData {
        private int thinkingCount;
        private int eatingCount;
        public String Name;
        public String Status;

        public PhilosopherData(){
            thinkingCount = 0;
            eatingCount = 0;
        }
        public PhilosopherData(String name, String status){
            thinkingCount = 0;
            eatingCount = 0;
            this.Name = name;
            this.Status = status;
        }

        public void incrementThinking( int id) {
            thinkingCount++;
            System.out.println("Philosopher " + id + "Number of times thinking: " + thinkingCount);
        }

        public void incrementEating(int id) {
            eatingCount++;
            System.out.println("Philosopher " + id +"Number of times eating: " + eatingCount);
        }

        private void verifyState() throws IOException {
            System.out.println("Filósofo " + Name + " está temporariamente desconectado,  internet instável");
        }
    }
}
