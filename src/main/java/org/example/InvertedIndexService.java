package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface InvertedIndexService extends Remote {
    Map<String, List<Integer>> getInvertedIndex(String filename) throws RemoteException;
}
