package application.model;

public class Edge {
    private Vertex source;
    private Vertex target;
    private int weight;

    public Edge(Vertex source, Vertex target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    public void incrementWeight() {
        weight++;
    }

    @Override
    public String toString() {
        return source + " -> " + target + " (" + weight + ")";
    }
}