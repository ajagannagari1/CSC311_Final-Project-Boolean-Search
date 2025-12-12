package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiSearch {

    private Map<String, Integer> map;

    public WikiSearch(Map<String, Integer> map) {
        this.map = map;
    }

    public Integer getRelevance(String url) {
        return map.getOrDefault(url, 0);
    }

    public WikiSearch and(WikiSearch that) {
        Map<String, Integer> intersection = new HashMap<>();
        for (String url : this.map.keySet()) {
            if (that.map.containsKey(url)) {
                int score = this.getRelevance(url) + that.getRelevance(url);
                intersection.put(url, score);
            }
        }
        return new WikiSearch(intersection);
    }

    public WikiSearch or(WikiSearch that) {
        Map<String, Integer> union = new HashMap<>();

        for (String url : this.map.keySet()) {
            union.put(url, this.getRelevance(url));
        }

        for (String url : that.map.keySet()) {
            int score = union.getOrDefault(url, 0) + that.getRelevance(url);
            union.put(url, score);
        }

        return new WikiSearch(union);
    }

    public WikiSearch minus(WikiSearch that) {
        Map<String, Integer> diff = new HashMap<>();
        for (String url : this.map.keySet()) {
            if (!that.map.containsKey(url)) {
                diff.put(url, this.getRelevance(url));
            }
        }
        return new WikiSearch(diff);
    }

    public List<Map.Entry<String, Integer>> sort() {
        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(map.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a,
                               Map.Entry<String, Integer> b) {
                return a.getValue() - b.getValue();
            }
        });

        return entries;
    }

    public void print() {
        for (Map.Entry<String, Integer> entry : sort()) {
            System.out.println(entry);
        }
    }

    public static WikiSearch search(String term, JedisIndex index) throws IOException {
        Map<String, Integer> map = index.getCounts(term);
        return new WikiSearch(map);
    }

    // -----------------------------------------------------------------------
    // MAIN METHOD (needed for `ant WikiSearch`)
    // -----------------------------------------------------------------------
    public static void main(String[] args) throws Exception {

        System.out.println("Connecting to Redis...");
        JedisIndex index = new JedisIndex(JedisMaker.make());

        System.out.println("Running test search queries...");

        WikiSearch s1 = search("java", index);
        WikiSearch s2 = search("programming", index);

        System.out.println("\n=== Results for 'java' ===");
        s1.print();

        System.out.println("\n=== Results for 'programming' ===");
        s2.print();

        System.out.println("\n=== AND (java AND programming) ===");
        s1.and(s2).print();

        System.out.println("\n=== OR (java OR programming) ===");
        s1.or(s2).print();

        System.out.println("\n=== MINUS (java MINUS programming) ===");
        s1.minus(s2).print();
    }
}
