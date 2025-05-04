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
                        visualizer.visualizeGraph(graph);
                        break;
                    case 2:
                        operations.queryBridgeWords(scanner);
                        break;
                    case 3:
                        operations.generateNewText(scanner);
                        break;
                    case 4:
                        operations.calculateShortestPath(scanner);
                        break;
                    case 5:
                        operations.calculatePageRank(scanner);
                        break;
                    case 6:
                        operations.randomWalk(scanner);
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
// git test