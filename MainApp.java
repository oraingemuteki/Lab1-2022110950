import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        TextGraph graph = new TextGraph();
        GraphVisualizer visualizer = new GraphVisualizer();
        GraphOperations operations = new GraphOperations(graph, visualizer);
        
        Scanner scanner = new Scanner(System.in);
        // String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\Cursed Be The Treasure.txt";
        // String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\test.txt";
        String filePath = "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\Easy Test.txt";

        // 接收用户输入的文件路径
        // System.out.println("请输入文件路径:");
        // String filePath = scanner.nextLine();

        try {
            graph.buildGraph(filePath);
            
            while (true) {
                System.out.println("\n请选择功能:");
                System.out.println("1. 可视化图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 根据桥接词生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 计算PageRank"); 
                System.out.println("6. 随机游走");
                System.out.println("7. 退出");
                System.out.println("----------------------");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // 消耗换行符

                switch (choice) {
                    case 1:
                        visualizer.showDirectedGraph(graph);
                        break;
                    case 2:
                        System.out.println("请输入两个英文单词（用空格分隔）:");
                        String input1 = scanner.nextLine();
                        String[] words = input1.split("\\s+");
                        if (words.length != 2) {
                            System.out.println("输入格式错误");
                        }
                        String result = operations.queryBridgeWords(words[0], words[1]);
                        System.out.println(result);
                        break;
                    case 3:
                        System.out.println("请输入一行新文本:");
                        String input2 = scanner.nextLine();
                        String generatedText = operations.generateNewText(input2);
                        System.out.println("new text: " + generatedText);
                        break;
                    case 4:
                        System.out.println("请输入一个或两个英文单词（用空格分隔）:");
                        String pathInput = scanner.nextLine().trim();
                        String[] pathWords = pathInput.split("\\s+");
    
                        String pathResult;
                        if (pathWords.length == 1) {
                            pathResult = operations.calcShortestPath(pathWords[0]);
                        } else if (pathWords.length == 2) {
                            pathResult = operations.calcShortestPath(pathWords[0], pathWords[1]);
                        } else {
                            pathResult = "输入格式错误";
                        }
                        System.out.println(pathResult);
                        break;
                    case 5:
                        System.out.println("请输入要查询的单词:");
                        String word = scanner.nextLine();
                        Double prValue = operations.calPageRank(word);
                        if (prValue != null) {
                            System.out.printf("%s 的PageRank值: %.6f%n", word, prValue);
                        } else {
                            System.out.println("单词不存在于图中");
                        }
                        break;
                    case 6:
                        String walkPath = operations.randomWalk();
                        System.out.println("随机游走路径: " + walkPath);
    
                        // 可选：保留原文件保存功能
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"))) {
                            writer.write(walkPath.replace(" -> ", " "));
                            System.out.println("路径已保存至文件");
                        } catch (IOException e) {
                            System.err.println("保存失败: " + e.getMessage());
                        }
                        break;
                    case 7:
                        visualizer.closeWindow();
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
}