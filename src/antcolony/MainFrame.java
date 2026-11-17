package antcolony;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/** Главное окно приложения. */
public final class MainFrame extends JFrame {
    private static final int[] SPEED_LEVELS = {0, 2, 4, 6, 8, 10};

    private final Graph graph;
    private final AntColonyConfig baseConfig;
    private AntColony colony;
    private final GraphPanel graphPanel;
    private final Timer animationTimer;

    private final JLabel iterationValue = new JLabel();
    private final JLabel bestValue = new JLabel();
    private final JLabel statusValue = new JLabel("Ожидание запуска");
    private final JLabel speedValue = new JLabel("Выбрано: 6");
    private final JSpinner iterationSpinner = new JSpinner(new SpinnerNumberModel(40, 5, 1000, 5));
    private int selectedSpeed = 6;

    public MainFrame() {
        super("Оптимизация муравьиной колонии");
        baseConfig = AntColonyConfig.defaultConfig();
        graph = Graph.createDemoGraph(baseConfig.getInitialPheromone());
        colony = new AntColony(graph, baseConfig);
        graphPanel = new GraphPanel(graph, colony);
        animationTimer = new Timer(20, event -> onAnimationTick());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(graphPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1280, 830);
        setLocationRelativeTo(null);
        setSelectedSpeed(6);
        updateStatistics();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(350, 650));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        JLabel title = new JLabel("Управление", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setAlignmentX(LEFT_ALIGNMENT);

        content.add(sectionTitle("Состояние алгоритма"));
        JPanel stats = new JPanel(new GridLayout(2, 2, 5, 8));
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));
        stats.add(new JLabel("Итерация:"));
        stats.add(iterationValue);
        stats.add(new JLabel("Лучший маршрут:"));
        stats.add(bestValue);
        content.add(stats);

        content.add(sectionTitle("Параметры запуска"));
        JPanel settings = new JPanel();
        settings.setLayout(new javax.swing.BoxLayout(settings, javax.swing.BoxLayout.Y_AXIS));
        settings.setAlignmentX(LEFT_ALIGNMENT);
        settings.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));
        settings.add(new JLabel("Количество итераций:"));
        iterationSpinner.setToolTipText("Число полных циклов движения всех муравьёв");
        iterationSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        iterationSpinner.setPreferredSize(new Dimension(180, 30));
        settings.add(iterationSpinner);
        settings.add(new JLabel("Скорость анимации:"));

        JPanel speedPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        speedPanel.setAlignmentX(LEFT_ALIGNMENT);
        speedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        speedPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        ButtonGroup speedGroup = new ButtonGroup();
        for (int speed : SPEED_LEVELS) {
            JToggleButton speedButton = new JToggleButton(Integer.toString(speed));
            speedButton.setFocusPainted(false);
            speedButton.setToolTipText(speedDescription(speed));
            speedButton.addActionListener(event -> setSelectedSpeed(speed));
            speedGroup.add(speedButton);
            speedPanel.add(speedButton);
            if (speed == 6) speedButton.setSelected(true);
        }
        settings.add(speedPanel);
        speedValue.setFont(speedValue.getFont().deriveFont(Font.PLAIN, 11f));
        speedValue.setForeground(new Color(90, 100, 112));
        speedValue.setAlignmentX(LEFT_ALIGNMENT);
        settings.add(speedValue);
        content.add(settings);

        content.add(sectionTitle("Параметры алгоритма муравьиной колонии"));
        JPanel parameters = new JPanel(new GridLayout(0, 1, 3, 4));
        parameters.setAlignmentX(LEFT_ALIGNMENT);
        parameters.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));
        parameters.add(new JLabel("Количество муравьёв: " + baseConfig.getAntCount()));
        parameters.add(new JLabel("α — влияние феромона: " + baseConfig.getAlpha()));
        parameters.add(new JLabel("β — влияние расстояния: " + baseConfig.getBeta()));
        parameters.add(new JLabel("ρ — испарение феромона: " + baseConfig.getEvaporationRate()));
        parameters.add(new JLabel("Q — добавление феромона: " + baseConfig.getPheromoneDeposit()));
        parameters.add(new JLabel("Начальная разведка: " + baseConfig.getExplorationIterations() + " итераций"));
        content.add(parameters);

        content.add(sectionTitle("Обозначения"));
        JPanel legend = new JPanel(new GridLayout(0, 1, 3, 5));
        legend.setAlignmentX(LEFT_ALIGNMENT);
        legend.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        legend.add(new JLabel("●  Города (узлы графа)"));
        legend.add(new JLabel("Цветные силуэты — муравьи"));
        legend.add(new JLabel("━  Толщина дороги — сила феромона"));
        legend.add(new JLabel("━  Зелёный — лучший найденный маршрут"));
        content.add(legend);

        JTextArea hint = new JTextArea("Подсказка: наведите курсор на дорогу, чтобы увидеть её длину и количество феромона.", 2, 34);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(new Color(90, 100, 112));
        hint.setOpaque(false);
        hint.setEditable(false);
        hint.setFocusable(false);
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setRows(2);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        hint.setAlignmentX(LEFT_ALIGNMENT);
        content.add(hint);
        Dimension contentSize = content.getPreferredSize();
        content.setPreferredSize(new Dimension(305, contentSize.height));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 7, 7));
        JButton startButton = new JButton("Старт");
        JButton pauseButton = new JButton("Пауза");
        JButton stepButton = new JButton("Шаг");
        JButton resetButton = new JButton("Сброс");
        startButton.addActionListener(event -> animationTimer.start());
        pauseButton.addActionListener(event -> animationTimer.stop());
        stepButton.addActionListener(event -> {
            animationTimer.stop();
            colony.stepOneEdge();
            updateStatistics();
            graphPanel.repaint();
        });
        resetButton.addActionListener(event -> resetSimulation());
        buttons.add(startButton);
        buttons.add(pauseButton);
        buttons.add(stepButton);
        buttons.add(resetButton);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 232)));
        statusValue.setFont(statusValue.getFont().deriveFont(Font.PLAIN, 12f));
        panel.add(statusValue);
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label;
        if ("Параметры алгоритма муравьиной колонии".equals(text)) {
            label = new JLabel("<html>Параметры алгоритма<br>муравьиной колонии</html>");
        } else {
            label = new JLabel(text);
        }
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void setSelectedSpeed(int speed) {
        selectedSpeed = speed;
        double duration = switch (speed) {
            case 0 -> 1200.0;
            case 2 -> 900.0;
            case 4 -> 650.0;
            case 6 -> 450.0;
            case 8 -> 280.0;
            case 10 -> 150.0;
            default -> throw new IllegalArgumentException("Неизвестная скорость: " + speed);
        };
        speedValue.setText("Выбрана скорость: " + speed);
        if (colony != null) colony.setMoveDurationMs(duration);
    }

    private String speedDescription(int speed) {
        return switch (speed) {
            case 0 -> "Очень медленное перемещение";
            case 2 -> "Медленное перемещение";
            case 4 -> "Ниже средней скорости";
            case 6 -> "Средняя скорость";
            case 8 -> "Быстрое перемещение";
            case 10 -> "Максимальная скорость";
            default -> "Скорость анимации";
        };
    }

    private void onAnimationTick() {
        colony.tick(20.0);
        updateStatistics();
        graphPanel.repaint();
    }

    private void updateStatistics() {
        iterationValue.setText(colony.getCompletedIterations() + " / " + colony.getConfig().getMaxIterations());
        if (colony.hasBestTour()) {
            String foundAt = colony.getBestFoundAtIteration() > 0
                    ? " (итерация " + colony.getBestFoundAtIteration() + ")" : "";
            bestValue.setText(String.format("%.1f%s", colony.getBestLength(), foundAt));
        } else {
            bestValue.setText("—");
        }

        if (colony.isLearningComplete()) {
            statusValue.setText("Обучение завершено. Муравьи демонстрируют лучший найденный замкнутый маршрут.");
        } else if (colony.getCompletedIterations() == 0) {
            statusValue.setText("Начальная разведка: муравьи случайно выбирают непосещённые города.");
        } else if (colony.getCompletedIterations() < colony.getConfig().getExplorationIterations()) {
            statusValue.setText("Разведка продолжается: влияние феромона и расстояния постепенно усиливается.");
        } else {
            statusValue.setText("Поиск маршрутов: после каждого цикла испаряется и обновляется феромон.");
        }
    }

    private void resetSimulation() {
        animationTimer.stop();
        int iterations = (Integer) iterationSpinner.getValue();
        AntColonyConfig config = new AntColonyConfig(
                baseConfig.getAntCount(), iterations,
                baseConfig.getAlpha(), baseConfig.getBeta(),
                baseConfig.getEvaporationRate(), baseConfig.getPheromoneDeposit(),
                baseConfig.getInitialPheromone(), baseConfig.isRandomFirstIteration(),
                baseConfig.getRandomSeed(), baseConfig.getExplorationIterations());
        colony = new AntColony(graph, config);
        graphPanel.setColony(colony);
        setSelectedSpeed(selectedSpeed);
        updateStatistics();
    }
}
