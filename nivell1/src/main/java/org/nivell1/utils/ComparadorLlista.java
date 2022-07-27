package org.nivell1.utils;

import java.util.Comparator;
import java.util.List;

public class ComparadorLlista implements Comparator<List<String>> {
    @Override
    public int compare(List<String> list1, List<String> list2) {
        return list1.get(0).compareTo(list2.get(0));
    }
}
