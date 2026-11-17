package antcolony;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Алгоритм Ant System для поиска кратчайшего замкнутого маршрута. */
public final class AntColony {
    private final Graph graph;
    private final AntColonyConfig config;
    private final Random random;
    private final List<Ant> ants = new ArrayList<>();

    private int completedIterations;
    private int bestFoundAtIteration;
    private double bestLength = Double.POSITIVE_INFINITY;
    private List<Node> bestTour = List.of();
    private boolean learningComplete;
    private double moveDurationMs = 450.0;

    public AntColony(Graph graph, AntColonyConfig config) {
        if (graph == null || config == null) throw new IllegalArgumentException("Граф и настройки обязательны");
        if (graph.getNodes().size() < 3 || graph.getEdges().isEmpty()) {
            throw new IllegalArgumentException("Граф должен содержать не менее трёх связанных городов");
        }
        this.graph = graph;
        this.config = config;
        this.random = new Random(config.getRandomSeed());
        reset();
    }

    public Graph getGraph() { return graph; }
    public AntColonyConfig getConfig() { return config; }
    public List<Ant> getAnts() { return List.copyOf(ants); }
    public int getCompletedIterations() { return completedIterations; }
    public int getBestFoundAtIteration() { return bestFoundAtIteration; }
    public double getBestLength() { return bestLength; }
    public List<Node> getBestTour() { return bestTour; }
    public boolean hasBestTour() { return !bestTour.isEmpty(); }
    public boolean isLearningComplete() { return learningComplete; }
    public double getMoveDurationMs() { return moveDurationMs; }

    public void setMoveDurationMs(double moveDurationMs) {
        if (!Double.isFinite(moveDurationMs) || moveDurationMs < 30) {
            throw new IllegalArgumentException("Длительность перемещения должна быть не менее 30 мс");
        }
        this.moveDurationMs = moveDurationMs;
    }

    public void reset() {
        graph.resetPheromones();
        random.setSeed(config.getRandomSeed());
        completedIterations = 0;
        bestFoundAtIteration = 0;
        bestLength = Double.POSITIVE_INFINITY;
        bestTour = List.of();
        learningComplete = false;
        createAntsForNextIteration();
    }

    /** Один кадр анимации. Муравьи перемещаются одновременно. */
    public void tick(double elapsedMs) {
        if (learningComplete) {
            for (Ant ant : ants) ant.advanceShowcase(elapsedMs, moveDurationMs);
            return;
        }
        for (Ant ant : ants) {
            if (ant.isFinished()) continue;
            if (ant.isMoving()) ant.advance(elapsedMs, graph, moveDurationMs);
            else ant.beginMove(chooseNextNode(ant));
        }
        if (allAntsFinished()) completeIteration();
    }

    /** Перемещает каждого муравья на одну дорогу; используется кнопкой «Шаг». */
    public void stepOneEdge() {
        if (learningComplete) {
            for (Ant ant : ants) ant.advanceShowcase(moveDurationMs, moveDurationMs);
            return;
        }
        for (Ant ant : ants) {
            if (ant.isFinished()) continue;
            if (!ant.isMoving()) ant.beginMove(chooseNextNode(ant));
            ant.completeCurrentMove(graph);
        }
        if (allAntsFinished()) completeIteration();
    }

    private Node chooseNextNode(Ant ant) {
        // После посещения всех городов муравей возвращается в исходный город.
        if (ant.getVisitedNodeIds().size() == graph.getNodes().size()) return ant.getStartNode();

        List<Node> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;

        // В первом цикле beta=0: непосещённые города выбираются равновероятно.
        // Затем влияние расстояния и феромона постепенно усиливается за несколько циклов.
        double progress = Math.min(1.0, completedIterations / (double) config.getExplorationIterations());
        double effectiveAlpha = config.isRandomFirstIteration()
                ? config.getAlpha() * progress : config.getAlpha();
        double effectiveBeta = config.isRandomFirstIteration()
                ? config.getBeta() * progress : config.getBeta();

        for (Node candidate : graph.getNeighbors(ant.getCurrentNode())) {
            if (ant.getVisitedNodeIds().contains(candidate.getId())) continue;
            Edge edge = graph.getEdge(ant.getCurrentNode(), candidate);
            if (edge == null) continue;
            double tau = Math.max(edge.getPheromone(), 1.0e-12);
            double eta = 1.0 / edge.getDistance();
            double weight = Math.pow(tau, effectiveAlpha) * Math.pow(eta, effectiveBeta);
            if (!Double.isFinite(weight) || weight < 0) weight = 0;
            candidates.add(candidate);
            weights.add(weight);
            totalWeight += weight;
        }

        if (candidates.isEmpty()) return ant.getStartNode();
        if (totalWeight <= 0 || !Double.isFinite(totalWeight)) {
            return candidates.get(random.nextInt(candidates.size()));
        }

        // Рулетка: вероятность пропорциональна tau^alpha * eta^beta.
        double threshold = random.nextDouble() * totalWeight;
        double accumulated = 0;
        for (int i = 0; i < candidates.size(); i++) {
            accumulated += weights.get(i);
            if (threshold <= accumulated) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    private boolean allAntsFinished() {
        return !ants.isEmpty() && ants.stream().allMatch(Ant::isFinished);
    }

    private void completeIteration() {
        // Сначала запоминаем лучший из завершённых замкнутых маршрутов.
        for (Ant ant : ants) {
            if (ant.getPathLength() < bestLength) {
                bestLength = ant.getPathLength();
                bestTour = ant.getTour();
                bestFoundAtIteration = completedIterations + 1;
            }
        }

        // Испарение феромона на всех дорогах.
        for (Edge edge : graph.getEdges()) edge.evaporate(config.getEvaporationRate());

        // Добавление Q / Lk на каждую дорогу маршрута каждого муравья.
        for (Ant ant : ants) {
            double deposit = config.getPheromoneDeposit() / ant.getPathLength();
            List<Node> route = ant.getTour();
            for (int i = 0; i < route.size() - 1; i++) {
                Edge edge = graph.getEdge(route.get(i), route.get(i + 1));
                if (edge != null) edge.addPheromone(deposit);
            }
        }

        completedIterations++;
        if (completedIterations >= config.getMaxIterations()) finishLearningAndShowBestTour();
        else createAntsForNextIteration();
    }

    private void createAntsForNextIteration() {
        ants.clear();
        List<Node> nodes = graph.getNodes();
        for (int i = 0; i < config.getAntCount(); i++) {
            ants.add(new Ant(i, nodes.get(i % nodes.size())));
        }
    }

    private void finishLearningAndShowBestTour() {
        learningComplete = true;
        if (bestTour.isEmpty()) return;
        // После заданного числа итераций силуэты демонстрируют найденный лучший цикл.
        // Стартовые позиции разнесены по маршруту, поэтому муравьи не накладываются друг на друга.
        for (int i = 0; i < ants.size(); i++) {
            ants.get(i).configureShowcase(bestTour, i % (bestTour.size() - 1));
        }
    }
}
