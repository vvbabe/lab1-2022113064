package application.model;

import java.util.*;
import java.util.stream.Collectors;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import java.io.File;
import java.io.IOException;
import static guru.nidi.graphviz.model.Factory.*;

public class Graph {
    private Map<Vertex, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addEdge(Vertex source, Vertex target) {
        if (!adjacencyList.containsKey(source)) {
            adjacencyList.put(source, new ArrayList<>());
        }

        List<Edge> edges = adjacencyList.get(source);
        Optional<Edge> existingEdge = edges.stream()
                .filter(e -> e.getTarget().equals(target))
                .findFirst();

        if (existingEdge.isPresent()) {
            existingEdge.get().incrementWeight();
        } else {
            edges.add(new Edge(source, target, 1));
        }

        // 确保目标顶点也在图中
        if (!adjacencyList.containsKey(target)) {
            adjacencyList.put(target, new ArrayList<>());
        }
    }

    public Set<Vertex> getVertices() {
        return adjacencyList.keySet();
    }

    public List<Edge> getEdgesFrom(Vertex vertex) {
        return adjacencyList.getOrDefault(vertex, Collections.emptyList());
    }

    public boolean containsVertex(String word) {
        return adjacencyList.containsKey(new Vertex(word));
    }

    public Vertex getVertex(String word) {
        return adjacencyList.keySet().stream()
                .filter(v -> v.getWord().equals(word.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    // 桥接词查询
    public List<String> findBridgeWords(String word1, String word2) {
        Vertex v1 = getVertex(word1);
        Vertex v2 = getVertex(word2);

        if (v1 == null || v2 == null) {
            return null;
        }

        List<String> bridges = new ArrayList<>();

        // 获取word1的所有出边目标
        List<Vertex> word1Targets = getEdgesFrom(v1).stream()
                .map(Edge::getTarget)
                .collect(Collectors.toList());

        // 检查这些目标是否有指向word2的边
        for (Vertex bridge : word1Targets) {
            boolean hasEdgeToWord2 = getEdgesFrom(bridge).stream()
                    .anyMatch(e -> e.getTarget().equals(v2));

            if (hasEdgeToWord2) {
                bridges.add(bridge.getWord());
            }
        }

        return bridges;
    }

    // 最短路径算法
    public List<Vertex> shortestPath(String word1, String word2) {
        // 实现Dijkstra算法
        Vertex start = getVertex(word1);
        Vertex end = getVertex(word2);

        if (start == null || end == null) {
            return null;
        }

        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();
        PriorityQueue<Vertex> queue = new PriorityQueue<>(
                Comparator.comparingInt(v -> distances.getOrDefault(v, Integer.MAX_VALUE)));

        for (Vertex vertex : adjacencyList.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();

            if (current.equals(end)) {
                break;
            }

            for (Edge edge : getEdgesFrom(current)) {
                Vertex neighbor = edge.getTarget();
                int newDist = distances.get(current) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (distances.get(end) == Integer.MAX_VALUE) {
            return null; // 不可达
        }

        // 构建路径
        List<Vertex> path = new ArrayList<>();
        for (Vertex at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        return path;
    }

    // 随机游走
    public String randomWalk() {
        if (adjacencyList.isEmpty()) {
            return "";
        }

        List<Vertex> visitedVertices = new ArrayList<>();
        Set<Edge> visitedEdges = new HashSet<>();

        Random random = new Random();
        List<Vertex> vertices = new ArrayList<>(adjacencyList.keySet());
        Vertex current = vertices.get(random.nextInt(vertices.size()));
        visitedVertices.add(current);

        while (true) {
            List<Edge> edges = getEdgesFrom(current);
            if (edges.isEmpty()) {
                break;
            }

            Edge nextEdge = edges.get(random.nextInt(edges.size()));
            if (visitedEdges.contains(nextEdge)) {
                break;
            }

            visitedEdges.add(nextEdge);
            current = nextEdge.getTarget();
            visitedVertices.add(current);
        }

        return visitedVertices.stream()
                .map(Vertex::getWord)
                .collect(Collectors.joining(" "));
    }
    public String generateGraphvizImage(String filename) throws IOException {
        MutableGraph g = mutGraph("directed-graph").setDirected(true);

        // 添加所有节点
        Map<Vertex, MutableNode> nodeMap = new HashMap<>();
        for (Vertex vertex : adjacencyList.keySet()) {
            nodeMap.put(vertex, mutNode(vertex.getWord()));
        }

        // 添加所有边
        for (Map.Entry<Vertex, List<Edge>> entry : adjacencyList.entrySet()) {
            MutableNode from = nodeMap.get(entry.getKey());
            for (Edge edge : entry.getValue()) {
                MutableNode to = nodeMap.get(edge.getTarget());
                from.addLink(to(to).with("label", String.valueOf(edge.getWeight())));
            }
        }

        // 添加所有节点到图中
        nodeMap.values().forEach(g::add);

        // 生成图片文件
        File output = new File(filename);
        Graphviz.fromGraph(g)
                .width(800)     // 当然可以保留
                .height(1000)   // 保留
                .render(Format.PNG) // 关键，设置成300dpi（默认是96）
                .toFile(output);


        return output.getAbsolutePath();
    }
    // PageRank计算
// 支持自定义初始PageRank
    public Map<Vertex, Double> calculatePageRank(double dampingFactor, int iterations, Map<Vertex, Double> initialPageRank) {
        Map<Vertex, Double> pageRank = new HashMap<>();

        // 初始化
        if (initialPageRank != null && !initialPageRank.isEmpty()) {
            // 使用外部提供的初始值
            double sum = initialPageRank.values().stream().mapToDouble(Double::doubleValue).sum();
            for (Vertex vertex : adjacencyList.keySet()) {
                pageRank.put(vertex, initialPageRank.getOrDefault(vertex, 0.0) / sum); // 归一化
            }
        } else {
            // 均匀分布
            double initialValue = 1.0 / adjacencyList.size();
            for (Vertex vertex : adjacencyList.keySet()) {
                pageRank.put(vertex, initialValue);
            }
        }

        // 迭代计算
        for (int i = 0; i < iterations; i++) {
            Map<Vertex, Double> newPageRank = new HashMap<>();
            double danglingValue = 0.0;

            // 计算悬挂节点贡献（没有出边的节点）
            for (Vertex vertex : adjacencyList.keySet()) {
                if (getEdgesFrom(vertex).isEmpty()) {
                    danglingValue += pageRank.get(vertex) / adjacencyList.size();
                }
            }

            // 计算每个节点的新PageRank值
            for (Vertex vertex : adjacencyList.keySet()) {
                double sum = 0.0;

                for (Vertex other : adjacencyList.keySet()) {
                    if (!other.equals(vertex)) {
                        List<Edge> edges = getEdgesFrom(other);
                        if (!edges.isEmpty()) {
                            long outLinks = edges.size();
                            boolean linksToCurrent = edges.stream()
                                    .anyMatch(e -> e.getTarget().equals(vertex));
                            if (linksToCurrent) {
                                sum += pageRank.get(other) / outLinks;
                            }
                        }
                    }
                }

                // 计算新的PageRank值
                double newValue = (1 - dampingFactor) / adjacencyList.size()
                        + dampingFactor * (sum + danglingValue);
                newPageRank.put(vertex, newValue);
            }

            pageRank = newPageRank;
        }

        return pageRank;
    }

    // 获取指定节点的出度（即指向其他节点的边的数量）
    public int getOutDegree(Vertex vertex) {
        List<Edge> edges = getEdgesFrom(vertex);
        return edges.size(); // 返回出度，即边的数量
    }


}