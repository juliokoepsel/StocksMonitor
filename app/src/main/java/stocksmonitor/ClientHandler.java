package stocksmonitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());) {
            @SuppressWarnings("unchecked")
            List<String> elements = (List<String>) in.readObject();
            System.out.println("Elementos recebidos!");
            System.out.println("Processando elementos!");

            List<Float> result = new ArrayList<>();
            Map<String, Float> prices = new HashMap<>();
            StockPriceSearcher searcher = new StockPriceSearcher(elements, prices);
            searcher.updateStockPrices();

            for (String string : elements) {
                for (Map.Entry<String, Float> set : prices.entrySet()) {
                    if (string.equals(set.getKey())) {
                        result.add(set.getValue());
                        break;
                    }
                }
            }

            System.out.println("Elementos processados!");
            out.writeObject(result);
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Erro de conexão: Erro ao enviar dados ao cliente no socket na porta "
                    + clientSocket.getLocalPort());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(
                        "Erro de socket: Erro ao fechar o socket do cliente na porta " + clientSocket.getLocalPort());
            }
        }
    }
}
