import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 可视化文本有向图
public class GraphVisualizer {
    private JFrame graphFrame;

    public void visualizeGraph(TextGraph graph) {
        graphFrame = new JFrame("文本有向图");
        // 设置窗口关闭时的操作，关闭窗口时释放资源
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphFrame.setSize(1200, 1000);

        // 创建一个 GraphPanel 面板，用于绘制图
        GraphPanel panel = new GraphPanel(graph.getGraph(), null);
        graphFrame.add(panel);
        graphFrame.setVisible(true);

        // saveGraphAsImage(panel(true));

        saveGraphAsImage(panel, "graph.png");
    }

    private Object panel(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'panel'");
    }

    public void visualizeGraphWithPath(TextGraph graph, List<String> path) {
        graphFrame = new JFrame("文本有向图（最短路径）");
        graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        graphFrame.setSize(800, 600);

        GraphPanel panel = new GraphPanel(graph.getGraph(), path);
        graphFrame.add(panel);
        graphFrame.setVisible(true);
    }

    // 关闭可视化窗口
    public void closeWindow() {
        if (graphFrame != null) {
            graphFrame.dispose();
        }
    }

        private void saveGraphAsImage(GraphPanel panel, String fileName) {
        int width = panel.getWidth();
        int height = panel.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d);
        g2d.dispose();

        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println("图形已保存为 " + fileName);
        } catch (IOException e) {
            System.err.println("保存图形时出错: " + e.getMessage());
        }
    }

    // 内部类 GraphPanel 
    static class GraphPanel extends JPanel {
                private final Map<String, Map<String, Integer>> graph;
        private final Map<String, Point> nodePositions = new HashMap<>();
        private static final int NODE_RADIUS =30;
        private final List<String> path;

        public GraphPanel(Map<String, Map<String, Integer>> graph, List<String> path) {
            this.graph = graph;
            this.path = path;
            setBackground(Color.WHITE); // 设置背景色
        }

        // 重写绘制组件的方法
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 动态计算节点位置
            calculateNodePositions();

            // 绘制边
            g2d.setColor(Color.GRAY);
            for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
                 // 获取源节点的位置
                String source = entry.getKey();
                Point srcPos = nodePositions.get(source);
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String target = edge.getKey();
                    Point tgtPos = nodePositions.get(target);
                    // 检查 tgtPos 是否为 null
                    if (srcPos != null && tgtPos != null) {
                        // 检查边是否在最短路径上
                        boolean isOnPath = path != null && isEdgeOnPath(source, target);
                        if (isOnPath) {
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(3));
                        } else {
                            g2d.setColor(Color.GRAY);
                            g2d.setStroke(new BasicStroke(1));
                        }
                        drawArrowLine(g2d, srcPos.x, srcPos.y, tgtPos.x, tgtPos.y, edge.getValue());
                    }
                }
            }

            // 绘制节点
            g2d.setColor(Color.BLUE);
            for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                Point pos = entry.getValue();
                g2d.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

                // 设置文字颜色为白色
                g2d.setColor(Color.WHITE);
                String nodeLabel = entry.getKey();
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(nodeLabel);
                int textHeight = fm.getHeight();
                // 让文字在圆圈中心对齐
                int x = pos.x - textWidth / 2;
                int y = pos.y + textHeight / 4;
                g2d.drawString(nodeLabel, x, y);

                // 恢复颜色为蓝色，用于后续绘制圆圈
                g2d.setColor(Color.BLUE);
            }
        }

        private boolean isEdgeOnPath(String source, String target) {
            for (int i = 0; i < path.size() - 1; i++) {
                if (path.get(i).equals(source) && path.get(i + 1).equals(target)) {
                    return true;
                }
            }
            return false;
        }

        // 计算节点位置
        private void calculateNodePositions() {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            double angleStep = 2 * Math.PI / graph.size();
            double currentAngle = 0;

            for (String node : graph.keySet()) {
                int x = centerX + (int) (Math.min(centerX, centerY) * 0.8 * Math.cos(currentAngle));
                int y = centerY + (int) (Math.min(centerX, centerY) * 0.8 * Math.sin(currentAngle));
                nodePositions.put(node, new Point(x, y));
                currentAngle += angleStep;
            }
        }

        private void drawArrowLine(Graphics2D g, int x1, int y1, int x2, int y2, int weight) {
            // 计算从源节点中心到目标节点中心的向量
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
        
            // 调整目标点到圆圈边缘
            double scale = (length - NODE_RADIUS) / length;
            int targetX = (int) (x1 + dx * scale);
            int targetY = (int) (y1 + dy * scale);
        
            // 绘制线
            g.drawLine(x1, y1, targetX, targetY);
        
            // 减小箭头大小
            int arrowSize = 6;
        
            // 绘制箭头
            double angle = Math.atan2(targetY - y1, targetX - x1);
            int x3 = (int) (targetX - arrowSize * Math.cos(angle - Math.PI / 6));
            int y3 = (int) (targetY - arrowSize * Math.sin(angle - Math.PI / 6));
            int x4 = (int) (targetX - arrowSize * Math.cos(angle + Math.PI / 6));
            int y4 = (int) (targetY - arrowSize * Math.sin(angle + Math.PI / 6));
        
            g.fillPolygon(new int[]{targetX, x3, x4}, new int[]{targetY, y3, y4}, 3);
        
            // 计算边的中心点坐标
            int midX = (x1 + targetX) / 2;
            int midY = (y1 + targetY) / 2;
        
            // 计算文本宽度和高度
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(String.valueOf(weight));
            int textHeight = fm.getHeight();
        
            // 调整文本位置到边的中心
            int textX = midX - textWidth / 2;
            int textY = midY + textHeight / 4;
        
            // 绘制权重
            g.setColor(Color.RED);
            g.drawString(String.valueOf(weight), textX, textY);
        }
    }
}