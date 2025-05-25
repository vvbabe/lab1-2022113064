package application.controller;

import application.model.Graph;
import application.model.Vertex;
import application.util.FileLoader;
import java.nio.file.Paths;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.util.stream.Collectors;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
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
    private static final Random rand = new Random(); // 类级别声明

    @FXML
    private TextField bridgeWord1Field;

    @FXML
    private TextField bridgeWord2Field;



    private Graph graph;
//写一些仅供测试用的 setter 方法
    public void setBridgeWord1Field(TextField field) {
        this.bridgeWord1Field = field;
    }

    public void setBridgeWord2Field(TextField field) {
        this.bridgeWord2Field = field;
    }

    public void setBridgeWordsResult(TextArea area) {
        this.bridgeWordsResult = area;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }


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
    public void handleQueryBridgeWords() {
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

        for (int i = 0; i < words.length - 1; i++) {
            output.append(words[i]).append(" ");

            List<String> bridges = graph.findBridgeWords(words[i], words[i+1]);
            if (bridges != null && !bridges.isEmpty()) {
                String bridge = bridges.get(rand.nextInt(bridges.size()));
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
// Step 1: 计算总节点数
        int totalVertices = graph.getVertices().size();

// Step 2: 计算TF-IDF权重作为初始PR值
// 2.1 计算TF（词频，即节点的出边数量）
        Map<Vertex, Integer> tfMap = graph.getVertices().stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> graph.getEdgesFrom(v).size()
                ));

// 2.2 计算IDF（逆文档频率）
        Map<Vertex, Double> idfMap = graph.getVertices().stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> {
                            int inDegree = (int) graph.getVertices().stream()
                                    .filter(u -> graph.getEdgesFrom(u).stream()
                                            .anyMatch(e -> e.getTarget().equals(v)))
                                    .count();
                            return Math.log((double) totalVertices / (inDegree + 1));
                        }
                ));

// 2.3 计算TF-IDF并归一化为初始PR值
        Map<Vertex, Double> tfidfMap = graph.getVertices().stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> tfMap.get(v) * idfMap.get(v)
                ));

// 归一化TF-IDF值作为初始PR
        double tfidfSum = tfidfMap.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<Vertex, Double> initialPR = tfidfMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> tfidfSum == 0 ? 1.0 / totalVertices : e.getValue() / tfidfSum
                ));

// Step 3: 调用PageRank计算方法
        Map<Vertex, Double> pageRank = graph.calculatePageRank(0.85, 10, initialPR);

// Step 4: 处理悬挂节点（出度为0的节点）
        List<Vertex> zeroOutDegreeNodes = graph.getVertices().stream()
                .filter(v -> graph.getEdgesFrom(v).isEmpty())
                .collect(Collectors.toList());

        if (!zeroOutDegreeNodes.isEmpty()) {
            // 计算需要重新分配的PR值（阻尼因子部分）
            double danglingPR = zeroOutDegreeNodes.stream()
                    .mapToDouble(v -> pageRank.get(v) * 0.85)
                    .sum();

            // 均分给所有节点
            double prShare = danglingPR / totalVertices;

            // 更新悬挂节点的PR值（保留(1-d)/N的部分）
            for (Vertex v : zeroOutDegreeNodes) {
                pageRank.put(v, (1 - 0.85) / totalVertices + prShare);
            }

            // 更新其他节点的PR值
            for (Vertex v : graph.getVertices()) {
                if (!zeroOutDegreeNodes.contains(v)) {
                    pageRank.put(v, pageRank.get(v) + prShare);
                }
            }
        }
        /*
        // Step 1: 计算总节点数
        int totalVertices = graph.getVertices().size();

// Step 2: 设置每个节点的初始 PageRank 为均匀值
// 将每个节点的初始 PR 值设置为 1 / 总节点数
        Map<Vertex, Double> initialPR = graph.getVertices().stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> 1.0 / totalVertices  // 每个节点的初始 PR 值都相等
                ));

// Step 3: 调用 PageRank 计算方法，并传入均匀初始化的 PR 值
        Map<Vertex, Double> pageRank = graph.calculatePageRank(0.85, 20, initialPR);

// Step 4: 处理出度为0的节点，将它们的 PR 值均分给其他节点
// 获取出度为0的节点列表
        List<Vertex> zeroOutDegreeNodes = graph.getVertices().stream()
                .filter(v -> graph.getOutDegree(v) == 0)
                .collect(Collectors.toList());

// 总 PageRank 值
        double totalPR = pageRank.values().stream().mapToDouble(Double::doubleValue).sum();

// 如果有出度为0的节点，均分其 PR 值
        if (!zeroOutDegreeNodes.isEmpty()) {
            double prShare = totalPR / graph.getVertices().size();
            for (Vertex v : zeroOutDegreeNodes) {
                pageRank.put(v, prShare);
            }
        }
*/
// Step 5: 展示所有节点 PageRank（按照得分从高到低排序）
        StringBuilder sb = new StringBuilder();
        sb.append("PageRank 结果 (d=0.85)：\n");

// 按照 PageRank 值排序节点
        pageRank.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> sb.append(entry.getKey().toString())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n"));

        System.out.println(sb.toString());
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