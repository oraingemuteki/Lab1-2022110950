import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphOperations {
    private final TextGraph graph;
    private final GraphVisualizer visualizer;
    private final Map<String, Double> tfidf;

    public GraphOperations(TextGraph graph, GraphVisualizer visualizer) {
        this.graph = graph;
        this.visualizer = visualizer;
        this.tfidf = graph.getTFIDF();
    }

    static class Node {
        String node;
        int distance;

        Node(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    public void queryBridgeWords(Scanner scanner) {
        scanner = new Scanner(System.in);
        System.out.println("请输入两个英文单词（用空格分隔）:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");
        if (words.length != 2) {
            System.out.println("输入格式错误，请输入两个英文单词并用空格分隔。");
            return;
        }
        String word1 = graph.normalizeWord(words[0]);
        String word2 = graph.normalizeWord(words[1]);

        if (!graph.getGraph().containsKey(word1) || !graph.getGraph().containsKey(word2)) {
            System.out.printf("No %s or %s in the graph!%n", word1, word2);
            return;
        }

        List<String> bridgeWords = new ArrayList<>();
        // 遍历第一个单词的所有邻居节点
        for (String potentialBridge : graph.getGraph().get(word1).keySet()) {
            // 检查该邻居节点是否有到第二个单词的边
            if (graph.getGraph().get(potentialBridge) != null && graph.getGraph().get(potentialBridge).containsKey(word2)) {
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

    public void generateNewText(Scanner scanner) {
        scanner = new Scanner(System.in);
        System.out.println("请输入一行新文本:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");

        StringBuilder newText = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < words.length; i++) {
            String current = graph.normalizeWord(words[i]);
            newText.append(words[i]);

            if (i < words.length - 1) {
                String next = graph.normalizeWord(words[i + 1]);
                // 检查图中是否包含当前单词和下一个单词
                if (graph.getGraph().containsKey(current) && graph.getGraph().containsKey(next)) {
                    List<String> bridgeWords = new ArrayList<>();
                    // 遍历当前单词的所有邻居节点
                    for (String potentialBridge : graph.getGraph().get(current).keySet()) {
                        // 检查该邻居节点是否有到下一个单词的边
                        if (graph.getGraph().get(potentialBridge) != null && graph.getGraph().get(potentialBridge).containsKey(next)) {
                            bridgeWords.add(potentialBridge);
                        }
                    }
                    if (!bridgeWords.isEmpty()) {
                        // 如果找到桥接词，随机选择一个添加到新文本中
                        int randomIndex = random.nextInt(bridgeWords.size());
                        newText.append(" ").append(bridgeWords.get(randomIndex));
                    }
                }
                newText.append(" ");
            }
        }

        System.out.println("生成的新文本: " + newText.toString());
    }

    public void calculateShortestPath(Scanner scanner) {
        scanner = new Scanner(System.in);
        System.out.println("请输入一个或两个英文单词（用空格分隔）:");
        String input = scanner.nextLine();
        String[] words = input.split("\\s+");

        if (words.length == 1) {
            String startWord = graph.normalizeWord(words[0]);
            if (!graph.getGraph().containsKey(startWord)) {
                System.out.printf("No %s in the graph!%n", startWord);
                return;
            }
            // 计算从起始单词到图中所有其他单词的最短路径
            for (String endWord : graph.getGraph().keySet()) {
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
            String startWord = graph.normalizeWord(words[0]);
            String endWord = graph.normalizeWord(words[1]);
            // 检查图中是否包含起始单词和结束单词
            if (!graph.getGraph().containsKey(startWord) || !graph.getGraph().containsKey(endWord)) {
                System.out.printf("No %s or %s in the graph!%n", startWord, endWord);
                return;
            }
            List<String> path = dijkstra(startWord, endWord);
            if (path != null) {
                int pathLength = calculatePathLength(path);
                System.out.printf("最短路径: %s，路径长度: %d%n", String.join(" -> ", path), pathLength);
                visualizer.visualizeGraphWithPath(graph, path);
            } else {
                System.out.printf("从 %s 到 %s 不可达%n", startWord, endWord);
            }
        } else {
            System.out.println("输入格式错误，请输入一个或两个英文单词并用空格分隔。");
        }
    }

    private int calculatePathLength(List<String> path) {
        int length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String source = path.get(i);
            String target = path.get(i + 1);
            // 检查是否存在从 source 到 target 的边
            Map<String, Integer> neighbors = graph.getGraph().get(source);
            if (neighbors != null && neighbors.containsKey(target)) {
                length += neighbors.get(target);
            }
        }
        return length;
    }

    private List<String> dijkstra(String start, String end) {
        // 存储每个节点到起始节点的最短距离
        Map<String, Integer> distance = new HashMap<>();
        // 存储每个节点的前一个节点
        Map<String, String> previous = new HashMap<>();
        // 优先队列，用于存储待处理的节点
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        // 初始化所有节点的距离为无穷大，前一个节点为 null
        for (String node : graph.getGraph().keySet()) {
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

             // 遍历当前节点的所有邻居节点
            for (Map.Entry<String, Integer> neighborEntry : graph.getGraph().get(currentNode).entrySet()) {
                String neighbor = neighborEntry.getKey();
                int weight = neighborEntry.getValue();
                int newDistance = distance.get(currentNode) + weight;

                // 如果新的距离小于已记录的最短距离，更新距离和前一个节点
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

    public void calculatePageRank(Scanner scanner) {
        Map<String, Double> pageRank = new HashMap<>();
        int numNodes = graph.getGraph().size();
        // 阻尼因子
        double dampingFactor = 0.85;
        // 收敛阈值
        double tolerance = 0.0001;
        // 初始 PageRank 值
        double initialPR = 1.0 / numNodes;
    
        // 初始化PageRank值，使用TF-IDF作为初始PR值
        for (String node : graph.getGraph().keySet()) {
            double initialValue = tfidf.getOrDefault(node, initialPR);
            pageRank.put(node, initialValue);
        }
        
        boolean converged = false;
        while (!converged) {
            Map<String, Double> newPageRank = new HashMap<>();
            double diff = 0.0;
    
            // 计算所有出度为0的节点的总PR值
            double sumDanglingPR = 0.0;
            for (String node : graph.getGraph().keySet()) {
                if (graph.getGraph().get(node).isEmpty()) {
                    sumDanglingPR += pageRank.get(node);
                }
            }
    
            for (String node : graph.getGraph().keySet()) {
                double sum = 0.0;
                // 处理来自正常节点的贡献（有出边的节点）
                for (Map.Entry<String, Map<String, Integer>> entry : graph.getGraph().entrySet()) {
                    String source = entry.getKey();
                    Map<String, Integer> neighbors = entry.getValue();
                    if (neighbors.containsKey(node)) {
                        int outDegree = neighbors.size();
                        sum += pageRank.get(source) / outDegree;
                    }
                }
    
                // 处理出度为0的节点的贡献：均分给其他节点（numNodes -1）
                if (numNodes > 1) { // 避免除以0
                    sum += sumDanglingPR / numNodes;
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

    private void writeVisitedNodesToFile(List<String> visitedNodes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\random_walk_path.txt"))) {
            writer.write(String.join(" ", visitedNodes));
            System.out.println("遍历路径已保存到 random_walk_path.txt 文件中。");
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }

    public void randomWalk(Scanner scanner) {
        scanner = new Scanner(System.in);
        Random random = new Random();
        List<String> visitedNodes = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        // 随机选择一个起点
        List<String> nodes = new ArrayList<>(graph.getGraph().keySet());
        String currentNode = nodes.get(random.nextInt(nodes.size()));
        visitedNodes.add(currentNode);
    
        System.out.println("开始随机游走，输入 'stop' 随时停止遍历。");
    
        while (true) {
            Map<String, Integer> neighbors = graph.getGraph().get(currentNode);
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
}