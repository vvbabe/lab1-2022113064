package application.controller;
import java.nio.file.Paths;
import application.model.Graph;
import application.model.Vertex;
import application.util.FileLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.stream.Collectors;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class GraphController {
    @FXML private Button loadFileBtn;
    @FXML private TextField filePathField;
    @FXML private TextArea graphDisplayArea;
    @FXML private TextField word1Field;
    @FXML private TextField word2Field;
    @FXML private TextArea bridgeWordsResult;
    @FXML private TextArea newTextInput;
    @FXML private TextArea newTextOutput;
    @FXML private TextArea shortestPathResult;
    @FXML private TextArea pageRankResult;
    @FXML private TextArea randomWalkResult;
    @FXML private VBox mainContainer;

    @FXML
    private TextField bridgeWord1Field;

    @FXML
    private TextField bridgeWord2Field;



    private Graph graph;

    @FXML
    public void initialize() {
        // 初始化按钮图标
        loadFileBtn.setGraphic(new FontIcon("fas-file"));

        // 设置Bootstrap样式
        loadFileBtn.getStyleClass().addAll("btn", "btn-primary");
        filePathField.getStyleClass().add("form-control");
        graphDisplayArea.getStyleClass().add("form-control");
        bridgeWordsResult.getStyleClass().add("form-control");
        newTextInput.getStyleClass().add("form-control");
        newTextOutput.getStyleClass().add("form-control");
        shortestPathResult.getStyleClass().add("form-control");
        pageRankResult.getStyleClass().add("form-control");
        randomWalkResult.getStyleClass().add("form-control");
        mainContainer.getStyleClass().add("panel");
        mainContainer.getStyleClass().add("panel-primary");
//        graphImagePane.setVisible(false);
        graphDisplayArea.setPrefHeight(400);
//        graphImagePane.setPrefHeight(100);

    }

    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        File file = fileChooser.showOpenDialog(loadFileBtn.getScene().getWindow());

        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
            graph = FileLoader.loadGraphFromFile(file);
            showDirectedGraph();
        }
    }

    private void showDirectedGraph() {
        if (graph == null) {

            graphDisplayArea.setText("图未加载");

            return;
        }

        // 1. 生成并保存Graphviz图像（但不显示）
        try {
            String imagePath = "graph_" + System.currentTimeMillis() + ".png";
            graph.generateGraphvizImage(imagePath); // 只生成不显示
        } catch (IOException e) {
            // 可以记录日志或忽略，因为用户不需要看到这个错误
            System.err.println("生成图形时出错: " + e.getMessage());
        }

        // 2. 显示文本信息（保留原有功能）
        StringBuilder sb = new StringBuilder();
        sb.append("有向图结构:\n");
        sb.append("顶点数量: ").append(graph.getVertices().size()).append("\n");
        sb.append("边数量: ").append(
                graph.getVertices().stream()
                        .mapToInt(v -> graph.getEdgesFrom(v).size())
                        .sum()
        ).append("\n\n");

        sb.append("顶点列表:\n");
        graph.getVertices().forEach(v -> sb.append(v.getWord()).append(", "));
        sb.append("\n\n边列表:\n");

        graph.getVertices().forEach(v -> {
            graph.getEdgesFrom(v).forEach(e -> {
                sb.append(e.toString()).append("\n");
            });
        });

        graphDisplayArea.setText(sb.toString());
    }
    @FXML
    private void handleQueryBridgeWords() {
        if (graph == null) {
            bridgeWordsResult.setText("请先加载图数据");
            return;
        }

        String word1 = bridgeWord1Field.getText().trim().toLowerCase();
        String word2 = bridgeWord2Field.getText().trim().toLowerCase();
        System.out.println("word1: [" + word1 + "]");
        System.out.println("word2: [" + word2 + "]");
        if (word1.isEmpty() || word2.isEmpty()) {
            bridgeWordsResult.setText("请输入两个单词");
            return;
        }

        List<String> bridges = graph.findBridgeWords(word1, word2);

        if (bridges == null) {
            bridgeWordsResult.setText("No \"" + word1 + "\" or \"" + word2 + "\" in the graph!");
        } else if (bridges.isEmpty()) {
            bridgeWordsResult.setText("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
        } else {
            String result = "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ";
            if (bridges.size() == 1) {
                result += bridges.get(0);
            } else {
                result += String.join(", ", bridges.subList(0, bridges.size() - 1));
                result += " and " + bridges.get(bridges.size() - 1);
            }
            bridgeWordsResult.setText(result);
        }
    }

    @FXML
    private void handleGenerateNewText() {
        if (graph == null) {
            newTextOutput.setText("请先加载图数据");
            return;
        }

        String inputText = newTextInput.getText().trim();
        if (inputText.isEmpty()) {
            newTextOutput.setText("请输入文本");
            return;
        }

        String[] words = inputText.toLowerCase().split("\\s+");
        StringBuilder output = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            output.append(words[i]).append(" ");

            List<String> bridges = graph.findBridgeWords(words[i], words[i+1]);
            if (bridges != null && !bridges.isEmpty()) {
                String bridge = bridges.get(random.nextInt(bridges.size()));
                output.append(bridge).append(" ");
            }
        }
        output.append(words[words.length - 1]);

        newTextOutput.setText(output.toString());
    }

    @FXML
    private void handleCalcShortestPath() {
        if (graph == null) {
            shortestPathResult.setText("请先加载图数据");
            return;
        }

        String word1 = word1Field.getText().trim().toLowerCase();
        String word2 = word2Field.getText().trim().toLowerCase();

        // 如果输入的两个单词都为空，提示用户输入单词
        if (word1.isEmpty()) {
            shortestPathResult.setText("请输入一个单词");
            return;
        }

        // 如果用户只输入一个单词，计算该单词到图中其他单词的最短路径
        if (word2.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("最短路径计算结果：\n");

            // 遍历所有图中的单词，计算从word1到每个单词的最短路径
            for (Vertex vertex : graph.getVertices()) {
                if (!vertex.getWord().equals(word1)) {
                    List<Vertex> path = graph.shortestPath(word1, vertex.getWord());
                    if (path == null) {
                        sb.append("No path from \"" + word1 + "\" to \"" + vertex.getWord() + "\"!\n");
                    } else {
                        sb.append("从 \"" + word1 + "\" 到 \"" + vertex.getWord() + "\" 的最短路径: ");
                        sb.append(path.stream().map(Vertex::getWord).collect(Collectors.joining(" -> ")));
                        sb.append("\n路径长度: ").append(path.size() - 1).append("\n");
                    }
                }
            }

            shortestPathResult.setText(sb.toString());
            return;
        }

        // 如果两个单词都不为空，计算它们之间的最短路径
        if (word2.isEmpty()) {
            shortestPathResult.setText("请输入第二个单词");
            return;
        }

        // 计算从word1到word2的最短路径
        List<Vertex> path = graph.shortestPath(word1, word2);

        if (path == null) {
            shortestPathResult.setText("No path from \"" + word1 + "\" to \"" + word2 + "\"!");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("最短路径: ");
            sb.append(path.stream().map(Vertex::getWord).collect(Collectors.joining(" -> ")));
            sb.append("\n路径长度: ").append(path.size() - 1);
            shortestPathResult.setText(sb.toString());
        }
    }

    @FXML
    private void handleCalcPageRank() {
        if (graph == null) {
            pageRankResult.setText("请先加载图数据");
            return;
        }

        Map<Vertex, Double> pageRank = graph.calculatePageRank(0.85, 20);

        StringBuilder sb = new StringBuilder();
        sb.append("PageRank 结果 (d=0.85):\n");

        pageRank.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> {
                    sb.append(entry.getKey().getWord())
                            .append(": ")
                            .append(String.format("%.4f", entry.getValue()))
                            .append("\n");
                });

        pageRankResult.setText(sb.toString());
    }

    @FXML
    private void handleRandomWalk() {
        if (graph == null) {
            randomWalkResult.setText("请先加载图数据");
            return;
        }

        String walk = graph.randomWalk();
        randomWalkResult.setText("随机游走结果:\n" + walk);
    }
}