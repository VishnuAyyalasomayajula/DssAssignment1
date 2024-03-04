package org.example;

import org.junit.jupiter.api.Test;

import java.rmi.Naming;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvertedIndexClientTest {

    @Test
    public void testInvertedIndex() throws Exception {
        // Locate the registry and get the stub of the service
        String endpoint = "rmi://127.0.0.1:8000/InvertedIndexService";
        InvertedIndexService service = (InvertedIndexService) Naming.lookup(endpoint);
        

        // Specify the file path
        String filePath = "src/main/resources/sample_data.txt";


        // Invoke the service and get the inverted index
        Map<String, List<Integer>> invertedIndex = service.getInvertedIndex(filePath);
        System.out.println("Inverted Index:");
        for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        // Check the results for the top 5 tokens
        assertEquals(5, invertedIndex.size());
        assertEquals("rmi", getKeyAtIndex(invertedIndex, 0));
        assertEquals("is", getKeyAtIndex(invertedIndex, 1));
        assertEquals("good", getKeyAtIndex(invertedIndex, 2));
        assertEquals("always", getKeyAtIndex(invertedIndex, 3));
        assertEquals("java", getKeyAtIndex(invertedIndex, 4));
    }

    private String getKeyAtIndex(Map<String, List<Integer>> map, int index) {
        return map.entrySet().stream().skip(index).findFirst().orElseThrow().getKey();
    }
        
    }

