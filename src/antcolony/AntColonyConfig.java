package antcolony;

/** Настройки алгоритма муравьиной колонии. */
public final class AntColonyConfig {
    private final int antCount;
    private final int maxIterations;
    private final double alpha;
    private final double beta;
    private final double evaporationRate;
    private final double pheromoneDeposit;
    private final double initialPheromone;
    private final boolean randomFirstIteration;
    private final long randomSeed;
    private final int explorationIterations;

    public AntColonyConfig(int antCount, int maxIterations, double alpha, double beta,
                           double evaporationRate, double pheromoneDeposit,
                           double initialPheromone, boolean randomFirstIteration,
                           long randomSeed, int explorationIterations) {
        if (antCount < 1 || maxIterations < 1) throw new IllegalArgumentException("Число муравьёв и итераций должно быть положительным");
        if (alpha < 0 || beta < 0) throw new IllegalArgumentException("Альфа и бета не могут быть отрицательными");
        if (evaporationRate < 0 || evaporationRate >= 1) throw new IllegalArgumentException("Коэффициент испарения должен быть в диапазоне [0; 1)");
        if (pheromoneDeposit <= 0 || initialPheromone <= 0) throw new IllegalArgumentException("Значения феромона должны быть положительными");
        if (explorationIterations < 1) throw new IllegalArgumentException("Длительность начальной разведки должна быть положительной");
        this.antCount = antCount;
        this.maxIterations = maxIterations;
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = evaporationRate;
        this.pheromoneDeposit = pheromoneDeposit;
        this.initialPheromone = initialPheromone;
        this.randomFirstIteration = randomFirstIteration;
        this.randomSeed = randomSeed;
        this.explorationIterations = explorationIterations;
    }

    public static AntColonyConfig defaultConfig() {
        return new AntColonyConfig(
                6,      // один муравей стартует из каждого города
                40,     // количество полных циклов
                1.0,    // влияние феромона (альфа)
                2.0,    // влияние расстояния (бета), раньше было 4.0
                0.45,   // испарение феромона
                30.0,   // количество добавляемого феромона Q
                1.0,    // начальный феромон на каждой дороге
                true,   // первый цикл — равновероятная случайная разведка
                42L,
                6       // плавное нарастание влияния феромона и расстояния за 6 циклов
        );
    }

    public int getAntCount() { return antCount; }
    public int getMaxIterations() { return maxIterations; }
    public double getAlpha() { return alpha; }
    public double getBeta() { return beta; }
    public double getEvaporationRate() { return evaporationRate; }
    public double getPheromoneDeposit() { return pheromoneDeposit; }
    public double getInitialPheromone() { return initialPheromone; }
    public boolean isRandomFirstIteration() { return randomFirstIteration; }
    public long getRandomSeed() { return randomSeed; }
    public int getExplorationIterations() { return explorationIterations; }
}
