package stocksmonitor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockPriceSearcher {
    private Set<String> symbols;
    private int maxConcurrentThreads = 2;
    private Map<String, Float> prices;

    public StockPriceSearcher(Set<String> symbols, Map<String, Float> prices) {
        this.symbols = symbols;
        this.prices = prices;
    }

    public void updateStockPrices() {
    System.out.println("Início da atualização de valores");
    ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentThreads);

    for (String symbol : symbols) {
        Thread t = new Thread(new Symbol(symbol, prices));
        executorService.execute(t);
    }
    executorService.shutdown();
    System.out.println("Fim da atualização de valores");
  }
}
