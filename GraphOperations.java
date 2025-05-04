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

    public String queryBridgeWords(String word1, String word2) {

        word1 = graph.normalizeWord(word1);
        word2 = graph.normalizeWord(word2);

        if (!graph.getGraph().containsKey(word1) || !graph.getGraph().containsKey(word2)) {
            // System.out.printf("No %s or %s in the graph!%n", word1, word2);
            String error = "单词 " + word1 + " 或 " + word2 + " 不在图中";
            return error;
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
            // System.out.printf("No bridge words from %s to %s!%n", word1, word2);
            String error = "No bridge words！";
            return error;
        } else if (bridgeWords.size() == 1) {
            result = new StringBuilder("从 " + word1 + " 到 " + word2 + " 的桥接词为: " + bridgeWords.get(0));
            // System.out.println(result.toString());
            return result.toString();
        } else {
            result = new StringBuilder("从 " + word1 + " 到 " + word2 + " 的桥接词为: ");
            for (int i = 0; i < bridgeWords.size(); i++) {
                if (i > 0) {
                    if (i == bridgeWords.size() - 1) {
                        result.append(" 和 ");
                    } else {
                        result.append(", ");
                    }
                }
                result.append(bridgeWords.get(i));
            }
            result.append(".");
            // System.out.println(result.toString());
            return result.toString();
        }
    }

    public String generateNewText(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return ""; 
        }
    
        String[] words = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();
        Random random = new Random();
    
        for (int i = 0; i < words.length; i++) {
            String current = graph.normalizeWord(words[i]);
            newText.append(words[i]);
    
            if (i < words.length - 1) {
                String next = graph.normalizeWord(words[i + 1]);
                if (graph.getGraph().containsKey(current) && graph.getGraph().containsKey(next)) {
                    List<String> bridgeWords = new ArrayList<>();
                    for (String potentialBridge : graph.getGraph().get(current).keySet()) {
                        if (graph.getGraph().get(potentialBridge) != null && 
                            graph.getGraph().get(potentialBridge).containsKey(next)) {
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
    
        return newText.toString().trim(); // 移除末尾多余空格
    }

    public String calcShortestPath(String word1) {
        String startWord = graph.normalizeWord(word1);
        if (!graph.getGraph().containsKey(startWord)) {
            return String.format("No '%s' in the graph!", startWord);
        }
    
        StringBuilder result = new StringBuilder();
        for (String endWord : graph.getGraph().keySet()) {
            if (endWord.equals(startWord)) continue;
            
            List<String> path = dijkstra(startWord, endWord);
            if (path != null) {
                int length = calculatePathLength(path);
                result.append(String.format("从 %s 到 %s: %s (长度: %d)\n",
                          startWord, endWord, String.join(" -> ", path), length));
            } else {
                result.append(String.format("\"%s 到 %s 没有路径\n", startWord, endWord));
            }
        }
        return result.toString().trim();
    }
    
    public String calcShortestPath(String word1, String word2) {
        String startWord = graph.normalizeWord(word1);
        String endWord = graph.normalizeWord(word2);
        
        if (!graph.getGraph().containsKey(startWord)) {
            return String.format("'%s' 不在图中\n", startWord);
        }
        if (!graph.getGraph().containsKey(endWord)) {
            return String.format("'%s' 不在图中\n", endWord);
        }
    
        List<String> path = dijkstra(startWord, endWord);
        if (path == null) {
            return String.format("%s 到 %s 没有路径\n", startWord, endWord);
        }
        
        int length = calculatePathLength(path);
        return String.format("Shortest path: %s (Length: %d)", 
                String.join(" -> ", path), length);
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

    public Double calPageRank(String word) {
        // 参数预处理和验证
        String normalizedWord = graph.normalizeWord(word);
        if (!graph.getGraph().containsKey(normalizedWord)) {
            return null; 
        }
    
        // 保留原PageRank计算逻辑
        Map<String, Double> pageRank = new HashMap<>();
        int numNodes = graph.getGraph().size();
        double dampingFactor = 0.85;
        double tolerance = 0.0001;
        double initialPR = 1.0 / numNodes;
    
        // 初始化（使用TF-IDF值）
        for (String node : graph.getGraph().keySet()) {
            pageRank.put(node, tfidf.getOrDefault(node, initialPR));
        }
    
        // 迭代计算
        boolean converged;
        do {
            converged = true;
            Map<String, Double> newPageRank = new HashMap<>();
            double danglingSum = pageRank.entrySet().stream()
                .filter(e -> graph.getGraph().get(e.getKey()).isEmpty())
                .mapToDouble(Map.Entry::getValue)
                .sum();
    
            for (String node : graph.getGraph().keySet()) {
                double sum = 0.0;
                // 计算来自其他节点的贡献
                for (Map.Entry<String, Map<String, Integer>> entry : graph.getGraph().entrySet()) {
                    String source = entry.getKey();
                    if (entry.getValue().containsKey(node)) {
                        int outDegree = entry.getValue().size();
                        sum += pageRank.get(source) / outDegree;
                    }
                }
                // 处理悬挂节点
                sum += dampingFactor * danglingSum / numNodes;
                
                // 计算新PageRank值
                double newPR = (1 - dampingFactor)/numNodes + dampingFactor * sum;
                newPageRank.put(node, newPR);
                
                // 检查收敛
                if (Math.abs(newPR - pageRank.get(node)) > tolerance) {
                    converged = false;
                }
            }
            pageRank = newPageRank;
        } while (!converged);
    
        // 返回指定单词的PageRank值
        return pageRank.get(normalizedWord);
    }

    private void writeVisitedNodesToFile(List<String> visitedNodes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\random_walk_path.txt"))) {
            writer.write(String.join(" ", visitedNodes));
            System.out.println("遍历路径已保存到 random_walk_path.txt 文件中。");
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }

    public String randomWalk() {
        Random random = new Random();
        List<String> visitedNodes = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        
        // 随机选择起点
        List<String> nodes = new ArrayList<>(graph.getGraph().keySet());
        if (nodes.isEmpty()) {
            return ""; // 处理空图情况
        }
        String currentNode = nodes.get(random.nextInt(nodes.size()));
        visitedNodes.add(currentNode);
    
        while (true) {
            Map<String, Integer> neighbors = graph.getGraph().get(currentNode);
            
            // 终止条件1: 没有出边
            if (neighbors == null || neighbors.isEmpty()) {
                break;
            }
            
            // 选择下一个节点
            List<String> neighborList = new ArrayList<>(neighbors.keySet());
            String nextNode = neighborList.get(random.nextInt(neighborList.size()));
            String edge = currentNode + " -> " + nextNode;
            
            // 终止条件2: 出现重复边
            if (visitedEdges.contains(edge)) {
                break;
            }
            
            // 记录路径
            visitedEdges.add(edge);
            visitedNodes.add(nextNode);
            currentNode = nextNode;
        }
        
        return String.join(" -> ", visitedNodes); // 返回遍历路径
    }
}