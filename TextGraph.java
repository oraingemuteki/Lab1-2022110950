import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class TextGraph {
    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z]");
    private final Map<String, Map<String, Integer>> graph = new HashMap<>();
    private final Map<String, Double> tfidf = new HashMap<>();

    public void buildGraph(String filePath) throws Exception {
        List<String> words = processFile(filePath);
        if (words.size() < 2) throw new IllegalArgumentException("文件需要包含至少两个单词");
        
        calculateTFIDF(words);
        // 将每个单词作为节点添加到图中
        for (String word : words) graph.putIfAbsent(word, new HashMap<>());
        
        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);
            graph.get(current).merge(next, 1, Integer::sum);
        }
    }

    // 处理文件，将文件内容转换为单词列表
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
                        // System.out.println(word);
                    }
                }
            }
        }
        return words;
    }

    // 规范化单词，将单词转换为小写并去除非字母字符
    public String normalizeWord(String word) {
        return WORD_PATTERN.matcher(word).replaceAll("").toLowerCase();
    }

    private void calculateTFIDF(List<String> words) {
        // 存储每个单词的词频
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
            double tfidfValue = (double) tf / totalWords; 
            // 将单词的 TF-IDF 值存储到 tfidf 中
            tfidf.put(word, tfidfValue);
        }
    }

    public Map<String, Map<String, Integer>> getGraph() {
        return graph;
    }

    public Map<String, Double> getTFIDF() {
        return tfidf;
    }

}