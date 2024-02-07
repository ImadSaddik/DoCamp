package com.example.ragapplication;

import android.app.Activity;
import android.app.Notification;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntriesRetiever {
    private List<Double> query;
    private FunctionChoices similarityFunction;
    private int numberOfEntriesToRetrieve;

    public EntriesRetiever(List<Double> query, FunctionChoices similarityFunction, int numberOfEntriesToRetrieve) {
        this.query = query;
        this.similarityFunction = similarityFunction;
        this.numberOfEntriesToRetrieve = numberOfEntriesToRetrieve;
    }

    public List<String> retrieveEntries(Activity activity) {
        DatabaseHelper databaseHelper = new DatabaseHelper(activity);
        Cursor cursor = databaseHelper.getEmbeddingRows(MainActivity.ROOM_ID);

        List<String> chunks = new ArrayList<>();
        List<List<Double>> vectorRepresentations = new ArrayList<>();

        int chunkIndex = cursor.getColumnIndex("Chunk");
        int vectorRepresentationIndex = cursor.getColumnIndex("Vector_Representation");

        if (chunkIndex != -1 && vectorRepresentationIndex != -1 && cursor.moveToFirst()) {
            do {
                String chunk = cursor.getString(chunkIndex);
                String vectorRepresentation = cursor.getString(vectorRepresentationIndex);

                chunks.add(chunk);
                vectorRepresentations.add(convertStringToDoubleVector(vectorRepresentation));
            } while (cursor.moveToNext());
        }

        cursor.close();

        List<Double> similarities = calculateSimilarities(query, vectorRepresentations);
        List<Integer> sortedIndices = getSortedIndices(similarities);

        List<String> topChunks = new ArrayList<>();
        for (int i = 0; i < numberOfEntriesToRetrieve && i < sortedIndices.size(); i++) {
            int index = sortedIndices.get(i);
            topChunks.add(chunks.get(index));
        }

        return topChunks;
    }

    private List<Double> convertStringToDoubleVector(String stringVector) {
        List<Double> doubleVector = new ArrayList<>();
        String[] stringVectorArray = stringVector.split(",");

        for (String stringElement : stringVectorArray) {
            doubleVector.add(Double.parseDouble(stringElement));
        }

        return doubleVector;
    }

    private List<Double> calculateSimilarities(List<Double> query, List<List<Double>> vectorRepresentations) {
        List<Double> similarities = new ArrayList<>();

        for (List<Double> vectorRepresentation : vectorRepresentations) {
            similarities.add(calculateSimilarity(query, vectorRepresentation, similarityFunction));
        }

        return similarities;
    }

    private Double calculateSimilarity(List<Double> query, List<Double> vectorRepresentation, FunctionChoices similarityFunction) {
        switch (similarityFunction) {
            case COSINE:
                return SimilarityFunctions.cosineSimilarity(query, vectorRepresentation);
            case EUCLIDEAN_DISTANCE:
                return SimilarityFunctions.euclideanDistance(query, vectorRepresentation);
            case MANHATTAN_DISTANCE:
                return SimilarityFunctions.manhattanDistance(query, vectorRepresentation);
            default:
                return 0.0;
        }
    }

    private List<Integer> getSortedIndices(List<Double> values) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            indices.add(i);
        }

        Collections.sort(indices, (i1, i2) -> Double.compare(values.get(i2), values.get(i1)));

        return indices;
    }
}
