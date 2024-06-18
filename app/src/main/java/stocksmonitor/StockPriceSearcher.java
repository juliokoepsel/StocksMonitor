package stocksmonitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StockPriceSearcher {
    private List<String> symbols;
    private int maxConcurrentThreads = 2;
    private Map<String, Float> prices;

    public StockPriceSearcher(List<String> symbols, Map<String, Float> prices) {
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
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Fim da atualização de valores");
    }
    
}
