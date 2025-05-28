import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TestWhite {
    private TextGraph graph1, graph2, graph3;
    private GraphOperations operations1, operations2, operations3;
    private static final String TEST_FILE_PATH1 =
            "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\test\\test1.txt";
    private static final String TEST_FILE_PATH2 =
            "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\test\\test2.txt";
    private static final String TEST_FILE_PATH3 =
            "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\test\\test3.txt";

    @Before
    public void setUp() throws Exception {
        graph1 = new TextGraph();
        graph1.buildGraph(TEST_FILE_PATH1);
        operations1 = new GraphOperations(graph1, new GraphVisualizer());

        graph2 = new TextGraph();
        graph2.buildGraph(TEST_FILE_PATH2);
        operations2 = new GraphOperations(graph2, new GraphVisualizer());

        graph3 = new TextGraph();
        graph3.buildGraph(TEST_FILE_PATH3);
        operations3 = new GraphOperations(graph3, new GraphVisualizer());
    }

    @Test
    public void testEmptyGraph() {
        String result = operations1.randomWalk(graph1);
        assertEquals("", result);
    }

    @Test
    public void testSingleWordGraph() {
        String result = operations2.randomWalk(graph2);
        assertEquals("helloworld", result);
//        assertTrue(judge(graph2, result));
    }

    @Test
    public void testMultipleWordGraph() {
        String result = operations3.randomWalk(graph3);
        assertTrue(judge(graph3, result));
    }

    private boolean judge(TextGraph graph, String path) {
        if (path.isEmpty()) return false;

        String[] nodes = path.split(" -> ");
        Set<String> visitedEdges = new HashSet<>();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes.length == 1) return true;

            if (i < nodes.length - 1) {
                String current = nodes[i];
                String next = nodes[i + 1];
                String edge = current + " -> " + next;

                if (!graph.getGraph().get(current).containsKey(next)) {
                    return false;
                }

                if (visitedEdges.contains(edge)) {
                    return false;
                }
                visitedEdges.add(edge);
            }
        }
        return true;
    }
}
