import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StockTradingPlatform {

    static final String FILE_NAME = "portfolio.txt";

    static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    static class Stock {
        private final String symbol;
        private final String name;
        private double price;

        Stock(String symbol, String name, double price) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }

        String getSymbol() { return symbol; }
        String getName() { return name; }
        double getPrice() { return price; }
        void setPrice(double price) { this.price = price; }
    }

    static class Transaction {
        private final String time;
        private final String type;
        private final String symbol;
        private final int qty;
        private final double price;

        // used when a new trade happens
        Transaction(String type, String symbol, int qty, double price) {
            this(now(), type, symbol, qty, price);
        }

        // NEW in Step 5: used when loading from the file (time already known)
        Transaction(String time, String type, String symbol, int qty, double price) {
            this.time = time;
            this.type = type;
            this.symbol = symbol;
            this.qty = qty;
            this.price = price;
        }

        String getType() { return type; }
        double total() { return qty * price; }

        String toFileLine() {
            return "T," + time + "," + type + "," + symbol + "," + qty + "," + price;
        }

        void print() {
            System.out.printf("%-20s %-5s %-6s %5d %10.2f %12.2f%n",
                    time, type, symbol, qty, price, total());
        }
    }

    static class Snapshot {
        private final String time;
        private final double value;

        Snapshot(double value) {
            this(now(), value);
        }

        // NEW in Step 5
        Snapshot(String time, double value) {
            this.time = time;
            this.value = value;
        }

        String getTime() { return time; }
        double getValue() { return value; }

        String toFileLine() {
            return "S," + time + "," + value;
        }
    }

    static class Market {
        private final List<Stock> stocks = new ArrayList<>();
        private final Random random = new Random();

        Market() {
            stocks.add(new Stock("AAPL", "Apple Inc.", 190.00));
            stocks.add(new Stock("GOOG", "Alphabet Inc.", 140.00));
            stocks.add(new Stock("MSFT", "Microsoft Corp.", 370.00));
            stocks.add(new Stock("TSLA", "Tesla Inc.", 240.00));
            stocks.add(new Stock("AMZN", "Amazon.com Inc.", 150.00));
        }

        List<Stock> getStocks() { return stocks; }

        void display() {
            System.out.println("\n--- MARKET ---");
            System.out.printf("%-8s %-18s %10s%n", "Symbol", "Company", "Price");
            for (Stock s : stocks) {
                System.out.printf("%-8s %-18s %10.2f%n",
                        s.getSymbol(), s.getName(), s.getPrice());
            }
        }

        Stock getStock(String symbol) {
            for (Stock s : stocks) {
                if (s.getSymbol().equalsIgnoreCase(symbol)) return s;
            }
            return null;
        }

        void advance() {
            for (Stock s : stocks) {
                double change = (random.nextDouble() * 6 - 3) / 100.0;
                double newPrice = s.getPrice() * (1 + change);
                newPrice = Math.round(newPrice * 100) / 100.0;
                s.setPrice(Math.max(1.00, newPrice));
            }
        }
    }

    static class Portfolio {
        private final double startingCash = 10000.00;
        private double cash = startingCash;
        private final Map<String, Integer> holdings = new LinkedHashMap<>();
        private final List<Transaction> history = new ArrayList<>();
        private final List<Snapshot> snapshots = new ArrayList<>();

        int getShares(String symbol) {
            return holdings.getOrDefault(symbol, 0);
        }

        int transactionCount() { return history.size(); }
        boolean hasSnapshots() { return !snapshots.isEmpty(); }

        double totalValue(Market market) {
            double total = cash;
            for (String symbol : holdings.keySet()) {
                total = total + market.getStock(symbol).getPrice() * holdings.get(symbol);
            }
            return total;
        }

        void recordSnapshot(Market market) {
            snapshots.add(new Snapshot(totalValue(market)));
        }

        boolean buy(Stock stock, int qty) {
            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                return false;
            }
            double cost = stock.getPrice() * qty;
            if (cost > cash) {
                System.out.printf("Not enough cash. You need $%.2f but have $%.2f.%n", cost, cash);
                return false;
            }
            cash = cash - cost;
            holdings.put(stock.getSymbol(), getShares(stock.getSymbol()) + qty);
            history.add(new Transaction("BUY", stock.getSymbol(), qty, stock.getPrice()));
            return true;
        }

        boolean sell(Stock stock, int qty) {
            int owned = getShares(stock.getSymbol());

            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                return false;
            }
            if (qty > owned) {
                System.out.println("You only own " + owned + " shares of " + stock.getSymbol() + ".");
                return false;
            }

            cash = cash + stock.getPrice() * qty;

            if (qty == owned) {
                holdings.remove(stock.getSymbol());
            } else {
                holdings.put(stock.getSymbol(), owned - qty);
            }
            history.add(new Transaction("SELL", stock.getSymbol(), qty, stock.getPrice()));
            return true;
        }

        void display(Market market) {
            System.out.println("\n--- PORTFOLIO ---");
            if (holdings.isEmpty()) {
                System.out.println("You don't own any shares yet.");
            } else {
                System.out.printf("%-8s %6s %10s %12s%n", "Symbol", "Shares", "Price", "Value");
                for (String symbol : holdings.keySet()) {
                    Stock s = market.getStock(symbol);
                    int qty = holdings.get(symbol);
                    System.out.printf("%-8s %6d %10.2f %12.2f%n",
                            symbol, qty, s.getPrice(), s.getPrice() * qty);
                }
            }
            double total = totalValue(market);
            System.out.printf("%nCash: $%.2f%n", cash);
            System.out.printf("Holdings value: $%.2f%n", total - cash);
            System.out.printf("Total: $%.2f%n", total);
        }

        void showHistory() {
            System.out.println("\n--- TRANSACTION HISTORY ---");
            if (history.isEmpty()) {
                System.out.println("No transactions yet.");
                return;
            }
            System.out.printf("%-20s %-5s %-6s %5s %10s %12s%n",
                    "Time", "Type", "Symbol", "Qty", "Price", "Total");
            int buys = 0;
            int sells = 0;
            for (Transaction t : history) {
                t.print();
                if (t.getType().equals("BUY")) buys++;
                else sells++;
            }
            System.out.println("\nBuys: " + buys + " | Sells: " + sells);
        }

        void showPerformance() {
            System.out.println("\n--- PORTFOLIO PERFORMANCE OVER TIME ---");
            if (snapshots.isEmpty()) {
                System.out.println("No data yet. Trade or advance the market first.");
                return;
            }
            System.out.printf("%-20s %12s %10s%n", "Time", "Value", "Change");
            double previous = startingCash;
            double highest = snapshots.get(0).getValue();
            double lowest = snapshots.get(0).getValue();
            for (Snapshot s : snapshots) {
                System.out.printf("%-20s %12.2f %+10.2f%n",
                        s.getTime(), s.getValue(), s.getValue() - previous);
                previous = s.getValue();
                if (s.getValue() > highest) highest = s.getValue();
                if (s.getValue() < lowest) lowest = s.getValue();
            }
            double latest = snapshots.get(snapshots.size() - 1).getValue();
            System.out.printf("%nStarted with: $%.2f%n", startingCash);
            System.out.printf("Latest value: $%.2f%n", latest);
            System.out.printf("Highest value: $%.2f%n", highest);
            System.out.printf("Lowest value: $%.2f%n", lowest);
            System.out.printf("Total return: %+.2f%%%n", (latest - startingCash) / startingCash * 100);
        }

        // ---- NEW in Step 5: save everything to a text file ----
        void save(Market market) {
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
                out.println("CASH," + cash);
                for (Stock s : market.getStocks()) {
                    out.println("P," + s.getSymbol() + "," + s.getPrice());
                }
                for (String symbol : holdings.keySet()) {
                    out.println("H," + symbol + "," + holdings.get(symbol));
                }
                for (Transaction t : history) {
                    out.println(t.toFileLine());
                }
                for (Snapshot s : snapshots) {
                    out.println(s.toFileLine());
                }
            } catch (IOException e) {
                System.out.println("Could not save data: " + e.getMessage());
            }
        }

        // ---- NEW in Step 5: read it back when the program starts ----
        boolean load(Market market) {
            File file = new File(FILE_NAME);
            if (!file.exists()) return false;        // first run: nothing to load

            try (Scanner reader = new Scanner(file)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split(",");

                    switch (p[0]) {
                        case "CASH":
                            cash = Double.parseDouble(p[1]);
                            break;
                        case "P": {
                            Stock s = market.getStock(p[1]);
                            if (s != null) s.setPrice(Double.parseDouble(p[2]));
                            break;
                        }
                        case "H":
                            if (market.getStock(p[1]) == null) {
                                throw new IllegalArgumentException("unknown stock " + p[1]);
                            }
                            holdings.put(p[1], Integer.parseInt(p[2]));
                            break;
                        case "T":
                            history.add(new Transaction(p[1], p[2], p[3],
                                    Integer.parseInt(p[4]), Double.parseDouble(p[5])));
                            break;
                        case "S":
                            snapshots.add(new Snapshot(p[1], Double.parseDouble(p[2])));
                            break;
                    }
                }
                return true;
            } catch (Exception e) {
                System.out.println("Save file could not be read (" + e.getMessage() + "). Starting fresh.");
                cash = startingCash;
                holdings.clear();
                history.clear();
                snapshots.clear();
                return false;
            }
        }
    }

    static Scanner sc = new Scanner(System.in);

    static void trade(Market market, Portfolio portfolio, boolean buying) {
        System.out.print("Stock symbol: ");
        Stock stock = market.getStock(sc.nextLine().trim());
        if (stock == null) {
            System.out.println("Unknown symbol.");
            return;
        }
        System.out.print("Quantity: ");
        try {
            int qty = Integer.parseInt(sc.nextLine().trim());
            boolean ok = buying ? portfolio.buy(stock, qty) : portfolio.sell(stock, qty);
            if (ok) {
                System.out.println((buying ? "Bought " : "Sold ") + qty + " x " + stock.getSymbol());
                portfolio.recordSnapshot(market);
                portfolio.save(market);                 // NEW: save after every trade
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a whole number.");
        }
    }

    public static void main(String[] args) {
        Market market = new Market();
        Portfolio portfolio = new Portfolio();

        if (portfolio.load(market)) {                   // NEW
            System.out.println("Welcome back! Loaded " + portfolio.transactionCount()
                    + " transactions from " + FILE_NAME + ".");
        }
        if (!portfolio.hasSnapshots()) {
            portfolio.recordSnapshot(market);           // starting value on a fresh start
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== STOCK TRADING PLATFORM ===");
            System.out.println("1. View market");
            System.out.println("2. Buy shares");
            System.out.println("3. Sell shares");
            System.out.println("4. View portfolio");
            System.out.println("5. Transaction history");
            System.out.println("6. Advance market (new prices)");
            System.out.println("7. Performance over time");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            switch (sc.nextLine().trim()) {
                case "1": market.display(); break;
                case "2": market.display(); trade(market, portfolio, true); break;
                case "3": trade(market, portfolio, false); break;
                case "4": portfolio.display(market); break;
                case "5": portfolio.showHistory(); break;
                case "6":
                    market.advance();
                    portfolio.recordSnapshot(market);
                    portfolio.save(market);             // NEW
                    System.out.println("Market updated.");
                    market.display();
                    break;
                case "7": portfolio.showPerformance(); break;
                case "8": running = false; break;
                default: System.out.println("Invalid option, try again.");
            }
        }
        portfolio.save(market);                         // NEW: save on exit
        System.out.println("Portfolio saved. Goodbye!");
    }
}