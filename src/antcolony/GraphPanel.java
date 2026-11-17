package antcolony;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

/** Панель визуализации графа, дорог, феромона, муравьёв и лучшего маршрута. */
public final class GraphPanel extends JPanel {
    private static final Color[] ANT_COLORS = {
            new Color(226, 82, 75), new Color(45, 132, 210), new Color(237, 157, 48),
            new Color(150, 99, 194), new Color(36, 158, 128), new Color(211, 91, 153),
            new Color(93, 112, 129), new Color(171, 132, 48)
    };
    private static final int NODE_RADIUS = 22;

    private final Graph graph;
    private AntColony colony;

    public GraphPanel(Graph graph, AntColony colony) {
        this.graph = graph;
        this.colony = colony;
        setOpaque(true);
        setBackground(new Color(249, 251, 253));
        setPreferredSize(new java.awt.Dimension(790, 660));
        setToolTipText("");

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                Edge nearest = findNearestEdge(event.getPoint());
                if (nearest == null) {
                    setToolTipText("Наведите курсор на дорогу, чтобы увидеть её параметры");
                } else {
                    setToolTipText(String.format("%s ↔ %s | расстояние: %.1f | феромон: %.4f",
                            nearest.getFirst().getName(), nearest.getSecond().getName(),
                            nearest.getDistance(), nearest.getPheromone()));
                }
            }
        });
    }

    public void setColony(AntColony colony) {
        this.colony = colony;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawHeader(g);
            drawRoads(g);
            drawBestTour(g);
            drawCities(g);
            drawAnts(g);
            drawFooter(g);
        } finally {
            g.dispose();
        }
    }

    private void drawHeader(Graphics2D g) {
        g.setColor(new Color(43, 54, 69));
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));
        g.drawString("Сеть городов", 24, 30);
        g.setColor(new Color(105, 115, 128));
        g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        g.drawString("Полный неориентированный граф · 6 городов · 15 дорог", 24, 50);
    }

    private void drawRoads(Graphics2D g) {
        double min = graph.getMinimumPheromone();
        double max = graph.getMaximumPheromone();
        for (Edge edge : graph.getEdges()) {
            Point2D a = pointFor(edge.getFirst());
            Point2D b = pointFor(edge.getSecond());
            double intensity = max - min < 1.0e-9 ? 0.12
                    : (edge.getPheromone() - min) / (max - min);
            intensity = Math.max(0, Math.min(1, intensity));
            float width = (float) (1.1 + intensity * 3.2);
            g.setColor(blend(new Color(194, 202, 212), new Color(79, 105, 139), intensity));
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(a, b));
        }
    }

    private void drawBestTour(Graphics2D g) {
        if (!colony.hasBestTour()) return;
        List<Node> route = colony.getBestTour();
        g.setColor(new Color(27, 158, 108, 225));
        g.setStroke(new BasicStroke(4.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < route.size() - 1; i++) {
            g.draw(new Line2D.Double(pointFor(route.get(i)), pointFor(route.get(i + 1))));
        }
    }

    private void drawCities(Graphics2D g) {
        for (Node node : graph.getNodes()) {
            Point2D point = pointFor(node);
            int x = (int) Math.round(point.getX());
            int y = (int) Math.round(point.getY());
            g.setColor(new Color(35, 49, 66));
            g.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            String label = Integer.toString(node.getId() + 1);
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(label, x - metrics.stringWidth(label) / 2, y + metrics.getAscent() / 3);

            g.setColor(new Color(60, 72, 88));
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            String cityName = node.getName();
            g.drawString(cityName, x - g.getFontMetrics().stringWidth(cityName) / 2, y + 38);
        }
    }

    private void drawAnts(Graphics2D g) {
        for (Ant ant : colony.getAnts()) {
            Point2D point = pointFor(ant.getDisplayX(), ant.getDisplayY());
            double angle = 0;
            if (ant.getTargetNode() != null) {
                angle = Math.atan2(ant.getTargetNode().getY() - ant.getCurrentNode().getY(),
                        ant.getTargetNode().getX() - ant.getCurrentNode().getX());
            }
            drawAntSilhouette(g, (int) Math.round(point.getX()), (int) Math.round(point.getY()),
                    angle, ANT_COLORS[ant.getId() % ANT_COLORS.length]);
        }
    }

    /** Рисует небольшого муравья векторными фигурами; направление совпадает с движением. */
    private void drawAntSilhouette(Graphics2D graphics, int x, int y, double angle, Color color) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.rotate(angle);
            g.transform(transform);

            Color dark = color.darker().darker();
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 220));
            g.fillOval(-14, -10, 29, 20);

            // Шесть лапок.
            g.setColor(dark);
            g.drawLine(-2, -2, -6, -8);
            g.drawLine(-1, 0, -6, 0);
            g.drawLine(-2, 2, -6, 8);
            g.drawLine(2, -2, 6, -8);
            g.drawLine(2, 0, 7, 0);
            g.drawLine(2, 2, 6, 8);

            // Брюшко, грудь и голова.
            g.setColor(dark);
            g.fillOval(-12, -4, 11, 8);
            g.setColor(color);
            g.fillOval(-11, -4, 9, 8);
            g.setColor(dark);
            g.fillOval(-3, -3, 7, 6);
            g.fillOval(3, -4, 7, 8);

            // Усики.
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(8, -2, 13, -7);
            g.drawLine(8, 2, 13, 7);
            g.fillOval(12, -8, 2, 2);
            g.fillOval(12, 6, 2, 2);

            // Маленький блик на брюшке.
            g.setColor(new Color(255, 255, 255, 185));
            g.fillOval(-9, -2, 3, 2);
        } finally {
            g.dispose();
        }
    }

    private void drawFooter(Graphics2D g) {
        int y = getHeight() - 22;
        g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g.setColor(new Color(100, 110, 123));
        g.drawString("Толщина дороги — количество феромона · Зелёный — лучший маршрут · Цветные силуэты — муравьи", 22, y);
    }

    private Point2D pointFor(Node node) { return pointFor(node.getX(), node.getY()); }

    private Point2D pointFor(double normalizedX, double normalizedY) {
        int marginLeft = 90;
        int marginRight = 90;
        int marginTop = 100;
        int marginBottom = 90;
        double x = marginLeft + normalizedX * Math.max(1, getWidth() - marginLeft - marginRight);
        double y = marginTop + normalizedY * Math.max(1, getHeight() - marginTop - marginBottom);
        return new Point2D.Double(x, y);
    }

    private Edge findNearestEdge(Point2D mouse) {
        Edge best = null;
        double bestDistance = 10;
        for (Edge edge : graph.getEdges()) {
            Point2D a = pointFor(edge.getFirst());
            Point2D b = pointFor(edge.getSecond());
            double distance = Line2D.ptSegDist(a.getX(), a.getY(), b.getX(), b.getY(), mouse.getX(), mouse.getY());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = edge;
            }
        }
        return best;
    }

    private Color blend(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int green = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int blue = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, green, blue);
    }
}
