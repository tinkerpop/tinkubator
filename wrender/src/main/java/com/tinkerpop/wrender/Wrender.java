package com.tinkerpop.wrender;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.jung.JungGraph;
import com.tinkerpop.blueprints.pgm.oupls.jung.GraphJung;
import com.tinkerpop.gremlin.GremlinScriptEngine;
import com.tinkerpop.gremlin.compiler.context.GremlinScriptContext;
import com.tinkerpop.gremlin.compiler.util.Tokens;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import org.apache.commons.collections15.Transformer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Wrender extends JFrame implements ActionListener {

    private Graph baseGraph;
    private Graph memoryGraph = new TinkerGraph();
    private GraphJung graphJung = new GraphJung(this.memoryGraph);
    private ScriptEngine gremlin = new GremlinScriptEngine();
    private GremlinScriptContext context = new GremlinScriptContext();
    private JTextArea codeArea = new JTextArea("gremlin> ", 5, 10);
    private Layout<Vertex, Edge> layout;
    private JPanel vizPanel;
    private VisualizationViewer<Vertex, Edge> viz;
    private JCheckBox checkBox = new JCheckBox("render");
    int codeRange = 9;


    public Wrender(Graph baseGraph) {
        super("Wrender: A Simple Graph Visualization Tool");
        this.baseGraph = baseGraph;
        context.getBindings(ScriptContext.ENGINE_SCOPE).put(Tokens.GRAPH_VARIABLE, this.baseGraph);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton submit = new JButton("submit");
        submit.setActionCommand("submit");
        submit.addActionListener(this);

        JButton load = new JButton("clear");
        load.setActionCommand("clear");
        load.addActionListener(this);
        this.codeArea.setBackground(Color.BLACK);
        this.codeArea.setForeground(Color.GREEN);
        this.codeArea.setAutoscrolls(true);
        this.codeArea.setBorder(new LineBorder(Color.BLACK));
        controlPanel.add(this.codeArea);
        controlPanel.add(buttonPanel);
        buttonPanel.add(this.checkBox, FlowLayout.LEFT);
        this.checkBox.setSelected(true);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(submit);
        buttonPanel.add(load);


        this.vizPanel = new JPanel();
        setUpVisualization();
        vizPanel.add(viz);
        mainPanel.add(this.vizPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(mainPanel);
        this.pack();
        this.setVisible(true);

        KeyListener listener = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                switch (keyChar) {
                    case KeyEvent.VK_ENTER:
                        actionPerformed(new ActionEvent(new Object(), 1, "submit"));
                }
            }
        };
        this.codeArea.addKeyListener(listener);

        setUpVisualization();
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("submit")) {
            try {
                String text = this.codeArea.getText().trim();
                String code = text.substring(codeRange, text.length());
                System.out.println(code);
                Object results = this.gremlin.eval(code, this.context);
                if (this.checkBox.isSelected())
                    this.processIterator(createResultIterator(results));
                Iterator itty = createResultIterator(results);
                while (itty.hasNext()) {
                    text = text + "\n" + itty.next();
                }
                text = text + "\ngremlin> ";
                this.codeArea.setText(text);
                codeRange = text.length();

            } catch (Exception e) {
                this.codeArea.setText(e.getMessage());
            }
        } else if (event.getActionCommand().equals("clear")) {
            this.memoryGraph.clear();
        }
        this.refresh();

    }

    private Iterator createResultIterator(Object results) {
        if (results instanceof Iterable)
            return ((Iterable) results).iterator();
        else if (!(results instanceof Iterator))
            return Arrays.asList(results).iterator();
        else
            return (Iterator) results;
    }

    private void processIterator(Iterator itty) {
        while (itty.hasNext()) {
            Object object = itty.next();
            if (object instanceof Element)
                mapElement((Element) object);
            else if (object instanceof Iterator)
                processIterator((Iterator) object);
            else if (object instanceof Iterable)
                processIterator(((Iterable) object).iterator());
        }
    }

    private void mapElement(Element element) {
        if (element instanceof Vertex) {
            try {
                this.memoryGraph.addVertex(element.getId());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (element instanceof Edge) {

            Edge edge = (Edge) element;
            Vertex v = this.memoryGraph.getVertex(edge.getOutVertex().getId());
            Vertex u = this.memoryGraph.getVertex(edge.getInVertex().getId());

            if (null == v) {
                v = this.memoryGraph.addVertex(edge.getOutVertex().getId());
            }

            if (null == u) {
                u = this.memoryGraph.addVertex(edge.getInVertex().getId());
            }
            try {
                this.memoryGraph.addEdge(edge.getId(), v, u, edge.getLabel());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    public void setUpVisualization() {
        this.layout = new FRLayout<Vertex, Edge>(this.graphJung);
        this.viz = new VisualizationViewer<Vertex, Edge>(this.layout);
        this.layout.reset();
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        viz.setGraphMouse(gm);
        viz.setBorder(new LineBorder(Color.BLACK));


        Transformer<Vertex, String> vertexLabelTransformer = new Transformer<Vertex, String>() {
            public String transform(Vertex vertex) {
                return vertex.getId().toString();
            }
        };

        Transformer<Edge, String> edgeLabelTransformer = new Transformer<Edge, String>() {
            public String transform(Edge edge) {
                return edge.getLabel();
            }
        };

        viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
        viz.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
        this.refresh();
    }

    public void refresh() {
        this.layout.setGraph(new GraphJung(this.memoryGraph));
        this.layout.reset();
        this.vizPanel.repaint();
    }

    public static void main(String[] args) {
        new Wrender(TinkerGraphFactory.createTinkerGraph());
    }
}
