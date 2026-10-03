# CodeAlpha Stock Trading Platform

A console-based Java application that simulates a basic stock trading environment.

## Features
- Market data display for 5 stocks
- Buy and sell shares with validation (cash and shares owned)
- Portfolio view: holdings, cash, current value
- Simulated market: prices move randomly (up to 3% up or down) each time you advance the market
- Portfolio performance over time: value recorded after every trade and market update, with change, highest, lowest and total return
- Transaction history with timestamps
- Data saved to a file (`portfolio.txt`) and reloaded on the next run

## OOP design
- `Stock`: symbol, name and price
- `Market`: the list of stocks and price simulation
- `Portfolio`: cash, holdings, transactions and performance snapshots
- `Transaction`: one buy or sell record
- `Snapshot`: portfolio value at a point in time

## How to run
```
javac StockTradingPlatform.java
java StockTradingPlatform
```

## Built with
Java, OOP, collections (List, Map), file I/O, exception handling

Internship task for CodeAlpha.