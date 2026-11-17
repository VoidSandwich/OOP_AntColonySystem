package antcolony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Полный неориентированный взвешенный граф. */
public final class Graph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final double initialPheromone;

    public Graph(double initialPheromone) {
        if (!Double.isFinite(initialPheromone) || initialPheromone <= 0) {
            throw new IllegalArgumentException("Начальное количество феромона должно быть положительным");
        }
        this.initialPheromone = initialPheromone;
    }

    public void addNode(Node node) {
        if (node == null) throw new IllegalArgumentException("Узел не может быть null");
        for (Node existing : nodes) {
            if (existing.getId() == node.getId()) {
                throw new IllegalArgumentException("Повторяется идентификатор узла: " + node.getId());
            }
        }
        nodes.add(node);
    }

    public void addEdge(Node a, Node b, double distance) {
        if (!nodes.contains(a) || !nodes.contains(b)) {
            throw new IllegalArgumentException("Оба конца дороги должны принадлежать графу");
        }
        if (getEdge(a, b) != null) throw new IllegalArgumentException("Такая дорога уже существует");
        edges.add(new Edge(a, b, distance, initialPheromone));
    }

    public List<Node> getNodes() { return Collections.unmodifiableList(nodes); }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    public List<Node> getNeighbors(Node node) {
        List<Node> result = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getFirst() == node) result.add(edge.getSecond());
            else if (edge.getSecond() == node) result.add(edge.getFirst());
        }
        return result;
    }

    public Edge getEdge(Node a, Node b) {
        for (Edge edge : edges) if (edge.connects(a, b)) return edge;
        return null;
    }

    public void resetPheromones() {
        for (Edge edge : edges) edge.resetPheromone(initialPheromone);
    }

    public double getMinimumPheromone() {
        return edges.stream().mapToDouble(Edge::getPheromone).min().orElse(initialPheromone);
    }

    public double getMaximumPheromone() {
        return edges.stream().mapToDouble(Edge::getPheromone).max().orElse(initialPheromone);
    }

    /**
     * Демонстрационный граф из шести городов и пятнадцати дорог.
     * Шесть дорог внешнего контура имеют длину 10, диагонали — 24.
     * Кратчайший замкнутый маршрут имеет длину 60.
     */
    public static Graph createDemoGraph(double initialPheromone) {
        Graph graph = new Graph(initialPheromone);
        Node[] cities = {
            new Node(0, "Город 1", 0.50, 0.10),
            new Node(1, "Город 2", 0.83, 0.27),
            new Node(2, "Город 3", 0.83, 0.70),
            new Node(3, "Город 4", 0.50, 0.88),
            new Node(4, "Город 5", 0.17, 0.70),
            new Node(5, "Город 6", 0.17, 0.27)
        };
        for (Node city : cities) graph.addNode(city);
        for (int i = 0; i < cities.length; i++) {
            for (int j = i + 1; j < cities.length; j++) {
                boolean outerRing = j == i + 1 || (i == 0 && j == cities.length - 1);
                graph.addEdge(cities[i], cities[j], outerRing ? 10.0 : 24.0);
            }
        }
        return graph;
    }
}
