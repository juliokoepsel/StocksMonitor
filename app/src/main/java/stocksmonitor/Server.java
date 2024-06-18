package stocksmonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int PORT = 31415;

    public static void main(String[] args) {
        String fileName = "server_connection.cfg";
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line = br.readLine();
                if (line != null) {
                    PORT = Integer.parseInt(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de leitura: Erro ao ler " + fileName);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.err.println("Erro de formatação numérica: Erro ao ler " + fileName);
            }
        } else {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
                bw.write(Integer.toString(PORT));
                System.out.println("(" + fileName + "): Gravação concluída!");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de gravação: Erro ao gravar " + fileName);
            }
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("O servidor está ouvindo a porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova requisição recebida!");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro de socket: Erro ao abrir socket na porta " + PORT);
        } finally {
            threadPool.shutdown();
        }
    }
}
