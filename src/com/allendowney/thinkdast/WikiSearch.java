package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The WikiSearch class represents search results for a single term.
 * It supports boolean operations (AND, OR, MINUS) and relevance scoring.
 *
 * Project: Think Data Structures – Exercise 13
 * Author: Akshitha Jagannagari
 */
public class WikiSearch {

    /**
     * Map from URL (String) to relevance score (Integer).
     */
    private Map<String, Integer> map;

    /**
     * Construct a WikiSearch object from a map of URL -> relevance.
     *
     * @param map map of URL to relevance score
     */
    public WikiSearch(Map<String, Integer> map) {
        this.map = map;
    }

    /**
     * Get the relevance score for a given URL.
     *
     * @param url the URL to query
     * @return relevance score (0 if not present)
     */
    public Integer getRelevance(String url) {
        return map.getOrDefault(url, 0);
    }

    /**
     * Compute the intersection (AND) of this WikiSearch and another.
     * Relevance for a URL present in both is the sum of the two relevances.
     *
     * @param that another WikiSearch
     * @return new WikiSearch containing URLs present in both inputs
     */
    public WikiSearch and(WikiSearch that) {
        Map<String, Integer> intersection = new HashMap<String, Integer>();
        for (String url : this.map.keySet()) {
            if (that.map.containsKey(url)) {
                int score = this.getRelevance(url) + that.getRelevance(url);
                intersection.put(url, score);
            }
        }
        return new WikiSearch(intersection);
    }

    /**
     * Compute the union (OR) of this WikiSearch and another.
     * Relevance for a URL present in one or both inputs is the sum of relevances.
     *
     * @param that another WikiSearch
     * @return new WikiSearch containing the union of URLs
     */
    public WikiSearch or(WikiSearch that) {
        Map<String, Integer> union = new HashMap<String, Integer>();

        for (String url : this.map.keySet()) {
            union.put(url, this.getRelevance(url));
        }

        for (String url : that.map.keySet()) {
            int score = union.getOrDefault(url, 0) + that.getRelevance(url);
            union.put(url, score);
        }

        return new WikiSearch(union);
    }

    /**
     * Compute the difference (MINUS) of this WikiSearch and another:
     * URLs that are in this result but not in the other.
     *
     * @param that another WikiSearch
     * @return new WikiSearch containing URLs present only in this
     */
    public WikiSearch minus(WikiSearch that) {
        Map<String, Integer> diff = new HashMap<String, Integer>();
        for (String url : this.map.keySet()) {
            if (!that.map.containsKey(url)) {
                diff.put(url, this.getRelevance(url));
            }
        }
        return new WikiSearch(diff);
    }

    /**
     * Return a list of map entries sorted by relevance (ascending).
     *
     * @return sorted list of entries (URL -> relevance)
     */
    public List<Map.Entry<String, Integer>> sort() {
        List<Map.Entry<String, Integer>> entries =
            new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a,
                               Map.Entry<String, Integer> b) {
                // sort by value (relevance) ascending
                return a.getValue() - b.getValue();
            }
        });

        return entries;
    }

    /**
     * Print the sorted results to standard output in the form:
     * URL = relevance
     */
    public void print() {
        for (Map.Entry<String, Integer> entry : sort()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    /**
     * Search for a single term using the given JedisIndex.
     *
     * @param term  the search term
     * @param index the JedisIndex (Redis-backed) to query
     * @return WikiSearch object containing the results
     * @throws IOException when index lookup fails
     */
    public static WikiSearch search(String term, JedisIndex index) throws IOException {
        Map<String, Integer> map = index.getCounts(term);
        return new WikiSearch(map);
    }

    // -----------------------------------------------------------------------
    // MAIN METHOD (needed for `ant WikiSearch`)
    // -----------------------------------------------------------------------

    /**
     * Main method used by the ant task to run a few test queries.
     *
     * @param args command-line arguments (unused)
     * @throws Exception on Redis or IO errors
     */
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
