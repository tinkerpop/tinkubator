# Generate Artificial Graphs
# Marko A. Rodriguez (http://markorodriguez.com)

library(igraph)
g <- barabasi.game(n=1000000, m=2, power=1, zero.appeal=8, directed=FALSE)
g <- simplify(g)
g <- as.directed(g, mode='mutual')
#diameter(g)
#average.path.length(g)

#write.graph(g, '../../../data/barabasi-graph.edge', format='edgelist')
write.graph(g, '../../../data/barabasi-graph.xml', format='graphml')

plot(subgraph(g,V(g)[1:1000]), layout=layout.fruchterman.reingold, vertex.size=2, edge.arrow.size=0, edge.width=0.5, vertex.label=NA)
plot(degree.distribution(g),log='xy',xlab='number of friends',ylab='frequency',cex.lab=1.5);


g <- barabasi.game(n=250, directed=TRUE)
plot(g, layout=layout.fruchterman.reingold, vertex.size=4, edge.arrow.size=0.3, edge.width=1, vertex.label=NA, vertex.color="blue")