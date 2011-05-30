GitGraph is a Blueprints Graph which stores its data in a hierarchy of plain
text files and plays well with version control software such as Git.
This allows multiple parties to edit a graph concurrently and later to merge
their changes.

GitGraph also enables partitioning of a graph into functional hierarchies, and
to combine graphs in a tree-like fashion.  For example, if you have a graph with
information about cities, and another with information about people, you can
place the two graph directories side by side in a parent directory and load this
as a single graph.  You can create new vertices and edges, at the parent level,
which connect people and cities, and then you can save the whole thing and check
it in to Git.  Another developer can then check out your work and either load
the whole graph or, if he is only interested in either cities or people, one of
the subdirectories.

Even if he only loads the graph about cities, he can make local changes to it
and push them back to you without invalidating the top-level graph.

Here is how the graph directory next to this README was created:

[bash]
    cd /Users/josh/projects/tinkerpop
    git clone git@github.com:tinkerpop/tinkubator.git
    cd tinkubator
    git add gitgraph-sandbox
    cd gitgraph-sandbox
    
[Java]
    Graph graph = new GitGraph(new File("/Users/josh/projects/tinkerpop/tinkubator/gitgraph-sandbox/graph"));
    GraphMLReader.inputGraph(graph, new FileInputStream("/tmp/graph-example-1.xml"));
    graph.shutdown();    

[bash]
    git add graph
    git commit -a -m "Adding version 1 of the graph."
    
[Java]
    Graph g = new GitGraph(new File("/Users/josh/projects/tinkerpop/tinkubator/gitgraph-sandbox/graph"));
    Vertex josh = g.getVertex(4);
    josh.setProperty("full name", "Joshua Shinavier");
    josh.setProperty("age", 34);
    g.shutdown();
    
[bash]
    git commit -a -m "First minor edit to the graph: I modified one property of the vertex representing me, and added another property."
    
[Java]
    Graph g = new GitGraph(new File("/Users/josh/projects/tinkerpop/tinkubator/gitgraph-sandbox/graph/extras/josh"));
    Vertex leon = g.addVertex(null);
    leon.setProperty("full name", "Leon Shinavier");
    leon.setProperty("age", 0.5);
    Vertex yellow = g.addVertex(null);
    yellow.setProperty("name", "yellow");
    Edge e = g.addEdge(null, leon, yellow, "favorite color");
    e.setProperty("comment", "as far as we can tell");
    g.shutdown();
    
[bash]
    git add graph/extras
    git commit -a -m "Added a child graph."
    
[Java]
    Graph g = new GitGraph(new File("/Users/josh/projects/tinkerpop/tinkubator/gitgraph-sandbox/graph"));
    Vertex josh = g.getVertex(4);
    Vertex leon = g.getVertex("extras/josh/0");
    g.addEdge(null, leon, josh, "parent");
    g.shutdown();

[bash]
    git commit -a -m "Added a new edge from a top-level vertex to a vertex defined in a child graph."
    git push origin master
    

Share and enjoy.

