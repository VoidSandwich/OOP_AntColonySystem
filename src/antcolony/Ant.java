package antcolony;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Муравей, строящий замкнутый маршрут. Перемещение анимируется интерполяцией. */
public final class Ant {
    private final int id;
    private Node startNode;
    private Node currentNode;
    private Node targetNode;
    private final List<Node> tour = new ArrayList<>();
    private final Set<Integer> visitedNodeIds = new HashSet<>();
    private double pathLength;
    private double progress;
    private boolean finished;

    private boolean showcaseMode;
    private List<Node> showcaseTour = List.of();
    private int showcaseSegment;

    public Ant(int id, Node startNode) {
        if (id < 0 || startNode == null) throw new IllegalArgumentException("Некорректный муравей");
        this.id = id;
        resetForTour(startNode);
    }

    public int getId() { return id; }
    public Node getStartNode() { return startNode; }
    public Node getCurrentNode() { return currentNode; }
    public Node getTargetNode() { return targetNode; }
    public List<Node> getTour() { return List.copyOf(tour); }
    public Set<Integer> getVisitedNodeIds() { return Set.copyOf(visitedNodeIds); }
    public double getPathLength() { return pathLength; }
    public double getProgress() { return progress; }
    public boolean isMoving() { return targetNode != null; }
    public boolean isFinished() { return finished; }
    public boolean isShowcaseMode() { return showcaseMode; }

    public void resetForTour(Node start) {
        startNode = start;
        currentNode = start;
        targetNode = null;
        tour.clear();
        tour.add(start);
        visitedNodeIds.clear();
        visitedNodeIds.add(start.getId());
        pathLength = 0;
        progress = 0;
        finished = false;
        showcaseMode = false;
    }

    public void beginMove(Node nextNode) {
        if (finished || showcaseMode || targetNode != null || nextNode == null) return;
        targetNode = nextNode;
        progress = 0;
    }

    public boolean advance(double elapsedMs, Graph graph, double moveDurationMs) {
        if (showcaseMode || targetNode == null || elapsedMs <= 0) return false;
        progress += elapsedMs / moveDurationMs;
        if (progress < 1.0) return false;
        completeCurrentMove(graph);
        return true;
    }

    public void completeCurrentMove(Graph graph) {
        if (targetNode == null || showcaseMode) return;
        Edge edge = graph.getEdge(currentNode, targetNode);
        if (edge == null) throw new IllegalStateException("Между выбранными городами нет дороги");
        pathLength += edge.getDistance();
        currentNode = targetNode;
        tour.add(currentNode);
        targetNode = null;
        progress = 0;

        if (currentNode == startNode && visitedNodeIds.size() == graph.getNodes().size()) {
            finished = true;
        } else {
            visitedNodeIds.add(currentNode.getId());
        }
    }

    /** Переводит муравья на лучший цикл после завершения обучения. */
    public void configureShowcase(List<Node> bestTour, int startOffset) {
        if (bestTour == null || bestTour.size() < 4
                || bestTour.get(0) != bestTour.get(bestTour.size() - 1)) {
            throw new IllegalArgumentException("Требуется корректный замкнутый маршрут");
        }
        showcaseMode = true;
        showcaseTour = List.copyOf(bestTour);
        int cityCount = showcaseTour.size() - 1;
        showcaseSegment = Math.floorMod(startOffset, cityCount);
        currentNode = showcaseTour.get(showcaseSegment);
        targetNode = showcaseTour.get((showcaseSegment + 1) % cityCount);
        progress = 0;
        finished = false;
    }

    public void advanceShowcase(double elapsedMs, double moveDurationMs) {
        if (!showcaseMode || elapsedMs <= 0) return;
        progress += elapsedMs / moveDurationMs;
        int cityCount = showcaseTour.size() - 1;
        while (progress >= 1.0) {
            progress -= 1.0;
            showcaseSegment = (showcaseSegment + 1) % cityCount;
            currentNode = showcaseTour.get(showcaseSegment);
            targetNode = showcaseTour.get((showcaseSegment + 1) % cityCount);
        }
    }

    public double getDisplayX() {
        if (targetNode == null) return currentNode.getX();
        return currentNode.getX() + (targetNode.getX() - currentNode.getX()) * progress;
    }

    public double getDisplayY() {
        if (targetNode == null) return currentNode.getY();
        return currentNode.getY() + (targetNode.getY() - currentNode.getY()) * progress;
    }
}
