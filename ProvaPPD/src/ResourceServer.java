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
    private static final int NUMBER_FORKS = 5;
    private final int NUMBER_PHILOSOPHERS = 5;

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
            // // Iniciar o monitor de filósofos em uma thread separada
            // new Thread(() -> monitorPhilosophers()).start();

            int id = 1;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PhilosopherData philosopher = philosophers.get(id);
                new Thread(new ClientHandler(clientSocket, id, philosopher)).start();
                id++;
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //tentativa de monitoramnto de filósofos desconectados com outra thread
    // private void monitorPhilosophers() {
    //     while (true) {
    //         try {
    //             Thread.sleep(1000); // Verifica a cada 1000ms (1 segundo)
    //             lock.lock();
    //             try {
    //                 for (Map.Entry<Integer, PhilosopherData> entry : philosophers.entrySet()) {
    //                     PhilosopherData philosopher = entry.getValue();
    //                     if ("DISCONNECTED".equals(philosopher.Status)) {
    //                         System.out.println("Filósofo " + philosopher.Name + " está temporariamente desconectado.");
    //                     }
    //                 }
    //             } finally {
    //                 lock.unlock();
    //             }
    //         } catch (InterruptedException e) {
    //             System.err.println("Monitoramento interrompido: " + e.getMessage());
    //         }
    //     }
    // }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int philosopherId;
        public PhilosopherData philosopher;

        public ClientHandler(Socket clientSocket, int philosopherId, PhilosopherData philosopher) {
            this.clientSocket = clientSocket;
            this.philosopherId = philosopherId;
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
                philosophers.get(philosopherId).Status = "DISCONNECTED";
                
                //tentativa de definir status na instancia entretanto impossibilidtado por ser uma variável local e se perder quando a conexão é perdida
                // if (philosopher != null) {
                //     philosopher.Status = "DISCONNECTED";
                //     try {
                //         philosopher.verifyState();
                //     } catch (IOException ex) {
                //         // Erro tratado de conexão perdida e reatada
                //         ex.printStackTrace();
                //     }
                // }
                System.err.println("Conexão perdida com o filósofo ID " + philosopherId);
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
            }
            if (philosopher != null) {
                philosopher.Status = "DISCONNECTED";
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
                    // System.out.println(nome);

                    boolean exists = false;
                    
                    
                    // System.out.println(philosophers.size());
                    for (int j = 1; j <= philosophers.size(); j++) {
                        // System.out.println(philosophers.get(j).Name);
                        if (philosophers.get(j).Name.equals(nome) && philosophers.get(j).Status.equals("CONNECTED")) {
                            return "ERROR: Philosopher " + nome + " is already at the table.";
                        }
 
                        if (philosophers.get(j).Name.equals(nome)) {
                        System.out.println("Philosopher already exists; Reconnecting Philosopher id = " + j + " Name = " + nome); 
                            this.philosopherId = j;
                            this.philosopher = philosophers.get(j);
                            exists = true;
                            break;
                        }
                    }    
                    if (!exists) {   
                        if (philosophers.size() < NUMBER_PHILOSOPHERS) {
                            philosopherId = philosophers.size() + 1;
                            philosophers.put(philosopherId, new PhilosopherData(nome, "CONNECTED"));
                        } else {
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
                                if (id > forks.length) {
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

        public PhilosopherData() {
            thinkingCount = 0;
            eatingCount = 0;
        }

        public PhilosopherData(String name, String status) {
            thinkingCount = 0;
            eatingCount = 0;
            this.Name = name;
            this.Status = status;
        }

        public void incrementThinking(int id) {
            thinkingCount++;
            System.out.println("Philosopher " + id + " ( name = "+ Name + " )." + " Number of times thinking: " + thinkingCount);
        }

        public void incrementEating(int id) {
            eatingCount++;
            System.out.println("Philosopher " + id + " ( name = "+ Name + " )." + " Number of times eating: " + eatingCount);
        }

        // funcao que utiliza a variável Status para verificar o estado do filósofo mas nao estou mais utilizando pois nao consego resgatar a variavel local da instancia após seu desligamento portanto fiz sua lógica diretamente no if sobre as listas percoridda dos filsofos adicionados
        // private void verifyState() throws IOException {
        //     System.out.println("Filósofo " + Name + " está temporariamente "+ Status +", internet instável");
        // }
    }
}
