package com.esprit.Services;

import java.util.*;

public class RouteOptimizationService {

    private Map<String, Map<String, Double>> graph;

    public RouteOptimizationService() {
        this.graph = new HashMap<>();
    }

    public void addEdge(String source, String destination, double weight) {
        graph.computeIfAbsent(source, k -> new HashMap<>()).put(destination, weight);
        graph.computeIfAbsent(destination, k -> new HashMap<>()).put(source, weight);
    }

    public List<String> findShortestPath(String start, String end, boolean favorGreenTrucks) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (String node : graph.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                break;
            }

            for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet()) {
                String next = neighbor.getKey();
                double weight = neighbor.getValue();
                if (favorGreenTrucks && isGreenTruckRoute(current, next)) {
                    weight *= 0.8; // Reduce weight for green truck routes
                }
                double newDistance = distances.get(current) + weight;

                if (newDistance < distances.get(next)) {
                    distances.put(next, newDistance);
                    previousNodes.put(next, current);
                    queue.add(next);
                }
            }
        }

        List<String> path = new ArrayList<>();
        for (String node = end; node != null; node = previousNodes.get(node)) {
            path.add(0, node);
        }
        return path;
    }

    private boolean isGreenTruckRoute(String source, String destination) {
        // Logic to determine if the route is suitable for green trucks
        // This could be based on charging station availability, road conditions, etc.
        return false; // Placeholder logic
    }

    public double calculateCO2Savings(List<String> path) {
        // Example calculation: 1 km = 0.1 kg CO2
        return path.size() * 0.1;
    }
} 