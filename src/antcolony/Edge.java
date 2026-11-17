package antcolony;

/** Двунаправленная дорога между двумя городами. */
public final class Edge {
    private final Node first;
    private final Node second;
    private final double distance;
    private double pheromone;

    public Edge(Node first, Node second, double distance, double initialPheromone) {
        if (first == null || second == null || first == second) {
            throw new IllegalArgumentException("Дорога должна соединять два разных города");
        }
        if (!Double.isFinite(distance) || distance <= 0) {
            throw new IllegalArgumentException("Расстояние должно быть положительным");
        }
        if (!Double.isFinite(initialPheromone) || initialPheromone <= 0) {
            throw new IllegalArgumentException("Начальное количество феромона должно быть положительным");
        }
        this.first = first;
        this.second = second;
        this.distance = distance;
        this.pheromone = initialPheromone;
    }

    public Node getFirst() { return first; }
    public Node getSecond() { return second; }
    public double getDistance() { return distance; }
    public double getPheromone() { return pheromone; }

    public boolean connects(Node a, Node b) {
        return (first == a && second == b) || (first == b && second == a);
    }

    public Node getOther(Node node) {
        if (node == first) return second;
        if (node == second) return first;
        throw new IllegalArgumentException("Указанный город не принадлежит дороге");
    }

    /** Испарение: tau = tau * (1 - rho). */
    public void evaporate(double rho) {
        if (rho < 0 || rho >= 1) {
            throw new IllegalArgumentException("Коэффициент испарения должен быть в диапазоне [0; 1)");
        }
        pheromone = Math.max(1.0e-9, pheromone * (1.0 - rho));
    }

    public void addPheromone(double amount) {
        if (Double.isFinite(amount) && amount > 0) {
            pheromone += amount;
        }
    }

    public void resetPheromone(double initialPheromone) {
        if (!Double.isFinite(initialPheromone) || initialPheromone <= 0) {
            throw new IllegalArgumentException("Начальное количество феромона должно быть положительным");
        }
        pheromone = initialPheromone;
    }
}
