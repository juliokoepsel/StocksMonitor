package stocksmonitor;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockPriceSearcher {
  private String[] symbols;
  private int maxConcurrentThreads = 2;
  private Map<String, Float> prices; 

  public StockPriceSearcher(String[] symbols, Map<String, Float> prices) {
    this.symbols = symbols;
    this.prices = prices;
  }

  public void updateStockPrices() {
    System.out.println("Início da atualização de valores");
    ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentThreads);

    for (int i = 0; i < symbols.length; i++) {
      Thread t = new Thread(new Symbol(symbols[i], prices));
      executorService.execute(t);
    }
    executorService.shutdown();
    System.out.println("Fim da atualização de valores");
  }
}
