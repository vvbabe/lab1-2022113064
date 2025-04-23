package application.model;

public class Vertex {
    private String word;

    public Vertex(String word) {
        this.word = word.toLowerCase();
    }

    public String getWord() {
        return word;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vertex)) return false;
        Vertex other = (Vertex) obj;
        return word.equals(other.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return word;
    }
}