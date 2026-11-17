package antcolony;

/** Город (вершина) графа. Координаты задаются в диапазоне от 0 до 1. */
public final class Node {
    private final int id;
    private final String name;
    private final double x;
    private final double y;

    public Node(int id, String name, double x, double y) {
        if (id < 0) {
            throw new IllegalArgumentException("Идентификатор узла не может быть отрицательным");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("У узла должно быть имя");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || x > 1 || y < 0 || y > 1) {
            throw new IllegalArgumentException("Координаты должны находиться в диапазоне [0; 1]");
        }
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() { return name; }
}
