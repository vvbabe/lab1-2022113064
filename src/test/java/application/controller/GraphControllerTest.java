package application.controller;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import application.model.Graph;
import application.model.Vertex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxToolkit.registerPrimaryStage;

public class GraphControllerTest extends ApplicationTest {

    private GraphController controller;
    private Graph graph;

    @Override
    public void start(Stage stage) throws Exception {
        controller = new GraphController();

        // 创建控件并设置ID
        TextField bridgeWord1Field = new TextField();
        bridgeWord1Field.setId("word1");
        TextField bridgeWord2Field = new TextField();
        bridgeWord2Field.setId("word2");
        TextArea bridgeWordsResult = new TextArea();
        bridgeWordsResult.setId("result");

        // 按钮：模拟你界面上的查询按钮，绑定控制器事件
        Button queryButton = new Button("查询桥接词");
        queryButton.setOnAction(e -> controller.handleQueryBridgeWords());

        // 注入控件给控制器
        controller.setBridgeWord1Field(bridgeWord1Field);
        controller.setBridgeWord2Field(bridgeWord2Field);
        controller.setBridgeWordsResult(bridgeWordsResult);

        VBox root = new VBox(bridgeWord1Field, bridgeWord2Field, queryButton, bridgeWordsResult);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        registerPrimaryStage();

        graph = new Graph();

        Vertex the = new Vertex("the");
        Vertex scientist = new Vertex("scientist");
        Vertex report = new Vertex("report");
        Vertex team = new Vertex("team");

        graph.addEdge(the, scientist);
        graph.addEdge(scientist, report);
        graph.addEdge(the, team);
        graph.addEdge(team, report);

        controller.setGraph(graph);
    }

    @Test
    public void testBridgeWordsExist() {
        clickOn("#word1").write("the");
        clickOn("#word2").write("report");
        clickOn("查询桥接词");  // 点击按钮，触发查询

        String result = lookup("#result").queryAs(TextArea.class).getText();
        assertEquals("The bridge words from \"the\" to \"report\" are: scientist and team", result);
    }

    @Test
    public void testWordsNotInGraph() {
        clickOn("#word1").write("hello");
        clickOn("#word2").write("python");
        clickOn("查询桥接词");

        String result = lookup("#result").queryAs(TextArea.class).getText();
        assertEquals("No \"hello\" or \"python\" in the graph!", result);
    }

    @Test
    public void testBridgeWordsNotExist() {
        clickOn("#word1").write("data");
        clickOn("#word2").write("team");
        clickOn("查询桥接词");

        String result = lookup("#result").queryAs(TextArea.class).getText();
        assertEquals("No \"data\" or \"team\" in the graph!", result);
    }

    @Test
    public void testBridgeBiWordsExist() {
        clickOn("#word1").write("The");
        clickOn("#word2").write("REPORT");
        clickOn("查询桥接词");

        String result = lookup("#result").queryAs(TextArea.class).getText();
        assertEquals("The bridge words from \"the\" to \"report\" are: scientist and team", result);
    }

    @Test
    public void testBridgeoneNotWordsExist() {
        clickOn("#word1").write("again");
        clickOn("#word2").write("hello");
        clickOn("查询桥接词");

        String result = lookup("#result").queryAs(TextArea.class).getText();
        assertEquals("No \"again\" or \"hello\" in the graph!", result);
    }
}
