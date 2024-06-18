package stocksmonitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Symbol implements Runnable {
    private String symbol;
    private Map<String, Float> prices;

    public Symbol(String symbol, Map<String, Float> prices) {
        this.symbol = symbol;
        this.prices = prices;
    }

    @Override
    public void run() {
        HttpClient client = HttpClient.newHttpClient();
        String apiKey = "";
        String fileName = "apikey.txt";
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line = br.readLine();
                if (line != null) {
                    apiKey = line;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de leitura: Erro ao ler " + fileName);
            }
        } else {
            System.err.println("API KEY não foi encontrada!");
        }

        HttpRequest request = HttpRequest.newBuilder(
                URI.create(
                        "https://brapi.dev/api/quote/" + symbol + "?token=" + apiKey))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            System.out.println("Início da busca: " + symbol);

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());

            JSONArray stockData = jsonObject.getJSONArray("results");
            float price = stockData.getJSONObject(0).getFloat("regularMarketPrice");
            prices.put(symbol, price);
            System.out.println("Fim da busca: " + symbol);

        } catch (JSONException e) {
            System.out.println("Erro: json inválido - " + symbol);
            prices.put(symbol, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
