# Generate Artificial Graphs
# Alex Averbuch (alex.averbuch@gmail.com)

from igraph import *
from pyjavaproperties import Properties
from random import *

p = Properties()
p.load(open('../resources/com/tinkerpop/bench/bench.properties'))

degree = int(p['bench.graph.barabasi.degree'])
vertices = int(p['bench.graph.barabasi.vertices'])

g = Graph.Barabasi(n=vertices, m=degree, power=1, directed=False, zero_appeal=8)

for v in g.vs:
    g.vs[v.index][p['bench.graph.property.id']] = "v" + str(v.index)
for e in g.es:
    g.es[e.index][p['bench.graph.property.id']] = "e" + str(e.index)
for e in g.es:
    if random() < 0.5:
        g.es[e.index][p['bench.graph.label']] = p['bench.graph.label.friend']
    else:
        g.es[e.index][p['bench.graph.label']] = p['bench.graph.label.family']
g.write_graphml('../../../' + p['bench.datasets.directory'] + 
                '/' + p['bench.graph.barabasi.file'])
