package com.example.ragapplication;

public class CharacterTextSplitter {
    private int chunkSize;
    private int overlapSize;

    public CharacterTextSplitter(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    public String[] getChunksFromText(String text) {
        int numChunks = (int) Math.ceil((double) text.length() / (chunkSize - overlapSize));
        String[] chunks = new String[numChunks];

        int chunkIndex = 0;
        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = Math.min(startIndex + chunkSize, text.length());
            chunks[chunkIndex] = text.substring(startIndex, endIndex);
            startIndex += chunkSize - overlapSize;
            chunkIndex++;
        }

        return chunks;
    }
}
