import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class TextGraphBuilder {
    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z]");
    private final Map<String, Map<String, Integer>> graph = new HashMap<>();
    private final Map<String, Double> tfidf = new HashMap<>(); // 新增：存储每个单词的TF-IDF值
    private JFrame graphFrame; // 用于保存图的窗口实例

    public static void main(String[] args) {
        TextGraphBuilder visualizer = new TextGraphBuilder();
        Scanner scanner = new Scanner(System.in);

        // 接收用户输入的文件路径
        // System.out.println("请输入文件路径:");
        // String filePath = scanner.nextLine();

        // String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\Cursed Be The Treasure.txt";
        // String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\Easy Test.txt";
        String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\test.txt";

        try {
            // 生成图
            visualizer.buildGraph(filePath);
            // visualizer.printGraph();

            while (true) {
                System.out.println("\n请选择功能:");
                System.out.println("1. 可视化图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 根据桥接词生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 计算PageRank"); 
                System.out.println("6. 随机游走");
                System.out.println("7. 退出");
                int choice = scanner.nextInt();
                scanner.nextLine(); // 消耗掉换行符

                switch (choice) {
                    case 1:
                        visualizer.visualizeGraph();
                        break;
                    case 2:
                        visualizer.queryBridgeWords();
                        break;
                    case 3:
                        visualizer.generateNewText();
                        break;
                    case 4:
                        visualizer.calculateShortestPath();
                        break;
                    case 5:
                        visualizer.calculatePageRank(); // 新增功能调用
                        break;
                    case 6:
                        visualizer.randomWalk();
                    case 7:
                        if (visualizer.graphFrame != null) {
                            visualizer.graphFrame.dispose(); // 关闭图的窗口
                        }
                        System.out.println("程序退出。");
                        return;
                    default:
                        System.out.println("无效的选择，请重新输入。");
                }
            }
        } catch (Exception e) {
            System.err.println("处理文件时出错: " + e.getMessage());
        }
    }

    private void buildGraph(String filePath) throws Exception {
        List<String> words = processFile(filePath);
        if (words.size() < 2) {
            throw new IllegalArgumentException("文件需要包含至少两个单词");
        }

        // 计算TF-IDF
        calculateTFIDF(words);

        // 确保所有单词都作为节点添加到图中
        for (String word : words) {
            graph.putIfAbsent(word, new HashMap<>());
        }

        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);
            graph.get(current).merge(next, 1, Integer::sum);
        }
    }

    private void calculateTFIDF(List<String> words) {
        Map<String, Integer> termFrequency = new HashMap<>();
        int totalWords = words.size();

        // 计算词频
        for (String word : words) {
            termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
        }

        // 计算TF-IDF
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String word = entry.getKey();
            int tf = entry.getValue();
            double tfidfValue = (double) tf / totalWords; // 简单的TF-IDF计算，可根据需求改进
            tfidf.put(word, tfidfValue);
        }
    }

    private void calculatePageRank() {
        Map<String, Double> pageRank = new HashMap<>();
        int numNodes = graph.size();
        double dampingFactor = 0.85;
        double tolerance = 0.0001;
        double initialPR = 1.0 / numNodes;
    
        // 初始化PageRank值，使用TF-IDF作为初始PR值
        for (String node : graph.keySet()) {
            double initialValue = tfidf.getOrDefault(node, initialPR);
            pageRank.put(node, initialValue);
        }
    
        boolean converged = false;
        while (!converged) {
            Map<String, Double> newPageRank = new HashMap<>();
            double diff = 0.0;
    
            // 计算所有出度为0的节点的总PR值
            double sumDanglingPR = 0.0;
            for (String node : graph.keySet()) {
                if (graph.get(node).isEmpty()) {
                    sumDanglingPR += pageRank.get(node);
                }
            }
    
            for (String node : graph.keySet()) {
                double sum = 0.0;
                // 处理来自正常节点的贡献（有出边的节点）
                for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
                    String source = entry.getKey();
                    Map<String, Integer> neighbors = entry.getValue();
                    if (neighbors.containsKey(node)) {
                        int outDegree = neighbors.size();
                        sum += pageRank.get(source) / outDegree;
                    }
                }
    
                // 处理出度为0的节点的贡献：均分给其他节点（numNodes -1）
                if (numNodes > 1) { // 避免除以0
                    sum += sumDanglingPR / (numNodes - 1);
                }
    
                double newPR = (1 - dampingFactor) / numNodes + dampingFactor * sum;
                newPageRank.put(node, newPR);
                diff += Math.abs(newPR - pageRank.get(node));
            }
    
            pageRank = newPageRank;
            if (diff < tolerance) {
                converged = true;
            }
        }
    
        // 输出PageRank值
        System.out.println("PageRank值:");
        for (Map.Entry<String, Double> entry : pageRank.entrySet()) {
            System.out.printf("%s: %.6f%n", entry.getKey(), entry.getValue());
        }
    }

    // 其他现有函数保持不变
    private List<String> processFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        String word = normalizeWord(token);
                        if (!word.isEmpty()) {
                            words.add(word);
                        }
                    }
                }
            }
        }
        return words;
    }

    private String normalizeWord(String word) {
        return WORD_PATTERN.matcher(word).replaceAll("").toLowerCase();
    }

    private void printGraph() {
        System.out.println("有向图结构:");
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String target = edge.getKey();
                int weight = edge.getValue();
                System.out.printf("%s -> %s (w=%d)%n", source, target, weight);
            }
        }
    }

    private void visualizeGraph() {
        graphFrame = new JFrame("文本有向图可视化");
        graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        graphFrame.setSize(800, 600);

        GraphPanel panel = new GraphPanel(graph, null);
        graphFrame.add(panel);
        graphFrame.setVisible(true);
    }

    private void queryBridgeWords() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入两个英文单词（用空格分隔）:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");
        if (words.length != 2) {
            System.out.println("输入格式错误，请输入两个英文单词并用空格分隔。");
            return;
        }
        String word1 = normalizeWord(words[0]);
        String word2 = normalizeWord(words[1]);

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            System.out.printf("No %s or %s in the graph!%n", word1, word2);
            return;
        }

        List<String> bridgeWords = new ArrayList<>();
        for (String potentialBridge : graph.get(word1).keySet()) {
            if (graph.get(potentialBridge) != null && graph.get(potentialBridge).containsKey(word2)) {
                bridgeWords.add(potentialBridge);
            }
        }

        StringBuilder result;
        if (bridgeWords.isEmpty()) {
            System.out.printf("No bridge words from %s to %s!%n", word1, word2);
        } else if (bridgeWords.size() == 1) {
            result = new StringBuilder("The bridge words from " + word1 + " to " + word2 + " is: " + bridgeWords.get(0));
            System.out.println(result.toString());
        } else {
            result = new StringBuilder("The bridge words from " + word1 + " to " + word2 + " are: ");
            for (int i = 0; i < bridgeWords.size(); i++) {
                if (i > 0) {
                    if (i == bridgeWords.size() - 1) {
                        result.append(" and ");
                    } else {
                        result.append(", ");
                    }
                }
                result.append(bridgeWords.get(i));
            }
            result.append(".");
            System.out.println(result.toString());
        }
    }

    private void generateNewText() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入一行新文本:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");

        StringBuilder newText = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < words.length; i++) {
            String current = normalizeWord(words[i]);
            newText.append(words[i]);

            if (i < words.length - 1) {
                String next = normalizeWord(words[i + 1]);
                if (graph.containsKey(current) && graph.containsKey(next)) {
                    List<String> bridgeWords = new ArrayList<>();
                    for (String potentialBridge : graph.get(current).keySet()) {
                        if (graph.get(potentialBridge) != null && graph.get(potentialBridge).containsKey(next)) {
                            bridgeWords.add(potentialBridge);
                        }
                    }
                    if (!bridgeWords.isEmpty()) {
                        int randomIndex = random.nextInt(bridgeWords.size());
                        newText.append(" ").append(bridgeWords.get(randomIndex));
                    }
                }
                newText.append(" ");
            }
        }

        System.out.println("生成的新文本: " + newText.toString());
    }

    private void calculateShortestPath() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入一个或两个英文单词（用空格分隔）:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");

        if (words.length == 1) {
            String startWord = normalizeWord(words[0]);
            if (!graph.containsKey(startWord)) {
                System.out.printf("No %s in the graph!%n", startWord);
                return;
            }
            for (String endWord : graph.keySet()) {
                if (!endWord.equals(startWord)) {
                    List<String> path = dijkstra(startWord, endWord);
                    if (path != null) {
                        int pathLength = calculatePathLength(path);
                        System.out.printf("从 %s 到 %s 的最短路径: %s，路径长度: %d%n", startWord, endWord, String.join(" -> ", path), pathLength);
                    } else {
                        System.out.printf("从 %s 到 %s 不可达%n", startWord, endWord);
                    }
                }
            }
        } else if (words.length == 2) {
            String startWord = normalizeWord(words[0]);
            String endWord = normalizeWord(words[1]);
            if (!graph.containsKey(startWord) || !graph.containsKey(endWord)) {
                System.out.printf("No %s or %s in the graph!%n", startWord, endWord);
                return;
            }
            List<String> path = dijkstra(startWord, endWord);
            if (path != null) {
                int pathLength = calculatePathLength(path);
                System.out.printf("最短路径: %s，路径长度: %d%n", String.join(" -> ", path), pathLength);
                visualizeGraphWithPath(path);
            } else {
                System.out.printf("从 %s 到 %s 不可达%n", startWord, endWord);
            }
        } else {
            System.out.println("输入格式错误，请输入一个或两个英文单词并用空格分隔。");
        }
    }

    private List<String> dijkstra(String start, String end) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        for (String node : graph.keySet()) {
            distance.put(node, Integer.MAX_VALUE);
            previous.put(node, null);
        }
        distance.put(start, 0);
        queue.add(new Node(start, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            String currentNode = current.node;

            if (currentNode.equals(end)) {
                break;
            }

            if (current.distance > distance.get(currentNode)) {
                continue;
            }

            for (Map.Entry<String, Integer> neighborEntry : graph.get(currentNode).entrySet()) {
                String neighbor = neighborEntry.getKey();
                int weight = neighborEntry.getValue();
                int newDistance = distance.get(currentNode) + weight;

                if (newDistance < distance.get(neighbor)) {
                    distance.put(neighbor, newDistance);
                    previous.put(neighbor, currentNode);
                    queue.add(new Node(neighbor, newDistance));
                }
            }
        }

        if (previous.get(end) == null) {
            return null;
        }

        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private int calculatePathLength(List<String> path) {
        int length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String source = path.get(i);
            String target = path.get(i + 1);
            // 检查是否存在从 source 到 target 的边
            Map<String, Integer> neighbors = graph.get(source);
            if (neighbors != null && neighbors.containsKey(target)) {
                length += neighbors.get(target);
            }
        }
        return length;
    }

    private void visualizeGraphWithPath(List<String> path) {
        graphFrame = new JFrame("文本有向图可视化（标注最短路径）");
        graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        graphFrame.setSize(800, 600);

        GraphPanel panel = new GraphPanel(graph, path);
        graphFrame.add(panel);
        graphFrame.setVisible(true);
    }

    private void randomWalk() {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<String> visitedNodes = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        // 随机选择一个起点
        List<String> nodes = new ArrayList<>(graph.keySet());
        String currentNode = nodes.get(random.nextInt(nodes.size()));
        visitedNodes.add(currentNode);
    
        System.out.println("开始随机游走，输入 'stop' 随时停止遍历。");
    
        while (true) {
            Map<String, Integer> neighbors = graph.get(currentNode);
            if (neighbors == null || neighbors.isEmpty()) {
                System.out.println("当前节点没有出边，随机游走结束。");
                break;
            }
            List<String> neighborList = new ArrayList<>(neighbors.keySet());
            String nextNode = neighborList.get(random.nextInt(neighborList.size()));
            String edge = currentNode + " -> " + nextNode;
            if (visitedEdges.contains(edge)) {
                System.out.println("出现重复边，随机游走结束。");
                break;
            }
            visitedEdges.add(edge);
            visitedNodes.add(nextNode);
            System.out.println("当前路径: " + String.join(" -> ", visitedNodes));
            System.out.print("输入 'stop' 停止遍历，按回车键继续: ");
            String input = scanner.nextLine();
            if ("stop".equalsIgnoreCase(input)) {
                System.out.println("用户手动停止遍历。");
                break;
            }
            currentNode = nextNode;
        }
    
        // 将遍历的节点输出为文本，并以文件形式写入磁盘
        writeVisitedNodesToFile(visitedNodes);
    }

    private void writeVisitedNodesToFile(List<String> visitedNodes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\random_walk_path.txt"))) {
            writer.write(String.join(" ", visitedNodes));
            System.out.println("遍历路径已保存到 random_walk_path.txt 文件中。");
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }

    static class Node {
        String node;
        int distance;

        Node(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    static class GraphPanel extends JPanel {
        private final Map<String, Map<String, Integer>> graph;
        private final Map<String, Point> nodePositions = new HashMap<>();
        private static final int NODE_RADIUS = 27;
        private final List<String> path;

        public GraphPanel(Map<String, Map<String, Integer>> graph, List<String> path) {
            this.graph = graph;
            this.path = path;
            setBackground(Color.WHITE); // 设置背景色
        }

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
                String source = entry.getKey();
                Point srcPos = nodePositions.get(source);
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String target = edge.getKey();
                    Point tgtPos = nodePositions.get(target);
                    // 检查 tgtPos 是否为 null
                    if (srcPos != null && tgtPos != null) {
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

                // 设置文字颜色为黑色
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

            // 绘制权重
            int midX = (x1 + targetX) / 2;
            int midY = (y1 + targetY) / 2;
            g.setColor(Color.RED);
            g.drawString(String.valueOf(weight), midX, midY);
        }
    }
}