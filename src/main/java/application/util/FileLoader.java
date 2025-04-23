package application.util;

import application.model.Graph;
import application.model.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileLoader {
    public static Graph loadGraphFromFile(File file) {
        Graph graph = new Graph();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String prevWord = null;

            while ((line = reader.readLine()) != null) {
                // 替换所有非字母字符为空格，并转换为小写
                String cleanedLine = line.replaceAll("[^a-zA-Z]", " ").toLowerCase();
                String[] words = cleanedLine.split("\\s+");

                for (String word : words) {
                    if (word.isEmpty()) continue;

                    if (prevWord != null) {
                        graph.addEdge(new Vertex(prevWord), new Vertex(word));
                    }
                    prevWord = word;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return graph;
    }
}