package stocksmonitor;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Symbol implements Runnable {
  private String symbol;

  public Symbol(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public void run() {
    var client = HttpClient.newHttpClient();
    String apiKey = "";

    var request = HttpRequest.newBuilder(
        URI.create(
            "https://brapi.dev/api/quote/" + symbol + "?token=" + apiKey))
        .header("accept", "application/json")
        .build();

    HttpResponse<String> response;
    try {
      System.out.println(symbol);

      response = client.send(request, HttpResponse.BodyHandlers.ofString());
      JSONObject jsonObject = new JSONObject(response.body());

      JSONArray stockData = jsonObject.getJSONArray("results");
      var a = stockData.getJSONObject(0).get("regularMarketPrice");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
