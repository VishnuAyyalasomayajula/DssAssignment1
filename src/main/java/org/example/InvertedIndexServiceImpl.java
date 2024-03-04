package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvertedIndexServiceImpl extends UnicastRemoteObject implements InvertedIndexService {

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Adjust pool size as needed
    private ExecutorService executorService;

    public InvertedIndexServiceImpl() throws RemoteException {
        super();
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String filename) throws RemoteException {
        Map<String, Set<Integer>> index = new ConcurrentHashMap<>();
        Map<String, Integer> tokenFrequency = new HashMap<>(); // To store token frequencies

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                executorService.execute(new ProcessLineTask(line, index, tokenFrequency, lineNumber));
                lineNumber++;
            }
            reader.close();

            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // Wait for all tasks to complete
            }

            // Sort the tokens by frequency
            List<Map.Entry<String, Integer>> sortedTokens = new ArrayList<>(tokenFrequency.entrySet());
            sortedTokens.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // Create a map to store only the top 5 tokens and their locations
            Map<String, List<Integer>> top5Tokens = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(5, sortedTokens.size()); i++) {
                String token = sortedTokens.get(i).getKey();
                List<Integer> locations = new ArrayList<>(index.get(token));
                top5Tokens.put(token, locations);
            }

            // Return only the top 5 tokens and their locations
            return top5Tokens;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Handle error case appropriately
    }

    private class ProcessLineTask implements Runnable {
        private final String line;
        private final Map<String, Set<Integer>> index;
        private final Map<String, Integer> tokenFrequency;
        private final int lineNumber;

        public ProcessLineTask(String line, Map<String, Set<Integer>> index, Map<String, Integer> tokenFrequency, int lineNumber) {
            this.line = line;
            this.index = index;
            this.tokenFrequency = tokenFrequency;
            this.lineNumber = lineNumber;
        }

        @Override
        public void run() {
            String[] words = line.split("\\s+");
            for (String word : words) {
                index.computeIfAbsent(word.toLowerCase(), k -> new TreeSet<>()).add(lineNumber);
                tokenFrequency.put(word.toLowerCase(), tokenFrequency.getOrDefault(word.toLowerCase(), 0) + 1);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Create an instance of the service implementation
            InvertedIndexService invertedIndexService = new InvertedIndexServiceImpl();

            // Create the RMI registry on port 8000
            LocateRegistry.createRegistry(8000);

            // Bind the service to the registry
            Naming.rebind("rmi://127.0.0.1:8000/InvertedIndexService", invertedIndexService);

            System.out.println("InvertedIndexService is running...");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
