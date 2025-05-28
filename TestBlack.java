import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestBlack {
    private TextGraph graph;
    private GraphOperations operations;
    private static final String TEST_FILE_PATH =
            "F:\\作业\\7.大三春\\软件工程\\实验1\\code\\helloworld\\src\\Easy Test.txt";

    @Before
    public void setUp() throws Exception {
        graph = new TextGraph();
        graph.buildGraph(TEST_FILE_PATH);
        operations = new GraphOperations(graph, new GraphVisualizer());
    }

    //单个桥接词
    @Test
    public void testSingleBridgeWord() {
        String result = operations.queryBridgeWords("and", "the");
        assertEquals("从 and 到 the 的桥接词为: shared 和 submit.", result);
    }

    //多个桥接词
    @Test
    public void testMultipleBridgeWords() {
        String result = operations.queryBridgeWords("the", "carefully");
        assertEquals("从 the 到 carefully 的桥接词为: scientist", result);
    }

    //直接相连无桥接词
    @Test
    public void testDirectLinkNoBridge() {
        String result = operations.queryBridgeWords("and", "a");
        assertEquals("No bridge words！", result);
    }

    //word不存在
    @Test
    public void testWord1Missing() {
        String result = operations.queryBridgeWords("and", "thus");
        assertEquals("单词 and 或 thus 不在图中", result);
    }
}