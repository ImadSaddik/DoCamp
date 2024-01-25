package com.example.ragapplication;

import java.util.List;

public class SimilarityFunctions {
    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = dotProduct(vectorA, vectorB);
        double magnitudeA = magnitude(vectorA);
        double magnitudeB = magnitude(vectorB);
        return dotProduct / (magnitudeA * magnitudeB);
    }

    public static double euclideanDistance(List<Double> vectorA, List<Double> vectorB) {
        double sum = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            sum += Math.pow(vectorA.get(i) - vectorB.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    public static double manhattanDistance(List<Double> vectorA, List<Double> vectorB) {
        double sum = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            sum += Math.abs(vectorA.get(i) - vectorB.get(i));
        }
        return sum;
    }

    private static double dotProduct(List<Double> vectorA, List<Double> vectorB) {
        double product = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            product += vectorA.get(i) * vectorB.get(i);
        }
        return product;
    }

    private static double magnitude(List<Double> vector) {
        double sum = 0.0;
        for (double value : vector) {
            sum += Math.pow(value, 2);
        }
        return Math.sqrt(sum);
    }
}
