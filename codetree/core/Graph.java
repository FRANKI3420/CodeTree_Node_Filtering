package codetree.core;

import java.io.Serializable;
import java.util.*;
import codetree.common.*;

public class Graph implements Serializable {
    public final int id;

    public final byte[] vertices;
    public byte[][] edges;

    public int[][] adjList;

    public int size;
    public int order;

    static Random rand;

    public Graph(int id, byte[] vertices, byte[][] edges) {
        this.id = id;
        this.vertices = vertices;
        this.edges = edges;

        this.order = this.order();
        this.size = this.size();

        adjList = makeAdjList();
    }

    private int[][] makeAdjList() {
        final int n = order();
        int[][] adjList = new int[n][];

        ArrayList<Integer> adj = new ArrayList<>();
        for (int v = 0; v < n; ++v) {
            for (int u = 0; u < n; ++u) {
                if (edges[v][u] > 0) {
                    adj.add(u);
                }
            }

            final int s = adj.size();

            adjList[v] = new int[s];
            for (int i = 0; i < adj.size(); ++i) {// ｖ行目の隣接行列作成
                adjList[v][i] = adj.get(i);
            }

            adj.clear();
        }

        return adjList;
    }

    public int order() {
        return vertices.length;
    }

    public int size()// グラフの辺の数を返す
    {
        int s = 0;

        final int n = order();
        for (int v = 0; v < n; ++v) {
            for (int u = 0; u < n; ++u) {
                if (edges[v][u] > 0) {
                    ++s;
                }
            }
        }

        return s / 2;
    }

    public double degree() {
        double d = 0;
        int n = this.order();
        for (int i = 0; i < n; i++) {
            d += adjList[i].length;
        }
        return d / (double) n;
    }

    public int labels() {
        Set<Byte> label = new HashSet<>();

        final int n = order();
        for (int v = 0; v < n; ++v) {
            label.add(vertices[v]);
        }

        return label.size();
    }

    public static int max_vertice(List<Graph> G) {
        int max = 0;
        for (int i = 0; i < G.size(); i++) {
            Graph g = G.get(i);
            if (g.order() >= max)
                max = g.order();
        }
        return max;
    }

    public static int max_size(List<Graph> G) {
        int max = 0;
        for (int i = 0; i < G.size(); i++) {
            Graph g = G.get(i);
            if (g.size() >= max)
                max = g.size();
        }
        return max;
    }

    public static int numOflabels(List<Graph> G) {
        Set<Byte> label = new HashSet<>();

        for (int i = 0; i < G.size(); i++) {
            Graph g = G.get(i);
            for (int v = 0; v < g.order(); ++v) {
                label.add(g.vertices[v]);
            }
        }
        return label.size();
    }

    public Graph shirinkNEC() {

        int order = 0;
        int[] map = new int[order()];
        ArrayList<Integer> remove = new ArrayList<>();

        for (int v = 0; v < this.order; ++v) {
            if (this.adjList[v].length > 1 || remove.contains(v))
                continue;

            if (this.adjList[v].length == 0) {
                remove.add(v);
                continue;
            }
            int adj = this.adjList[v][0];

            for (int u : this.adjList[adj]) {
                if (this.adjList[u].length > 1 || u == v || vertices[u] != vertices[v])
                    continue;
                remove.add(u);
            }
        }

        for (int v = 0; v < this.order; v++) {
            if (!remove.contains(v)) {
                map[order++] = v;
            }
        }
        byte[] vertices = new byte[order];
        byte[][] edges = new byte[order][order];

        for (int v = 0; v < order; ++v) {
            vertices[v] = this.vertices[map[v]];
            for (int u = 0; u < order; ++u) {
                edges[v][u] = this.edges[map[v]][map[u]];
            }
        }
        return new Graph(id, vertices, edges);
    }

    public Graph shrink() {
        final byte H = VertexLabel.string2id("H");

        int[] map = new int[order()];
        int order = 0;
        for (int v = 0; v < map.length; ++v) {
            if (vertices[v] != H) {
                map[order++] = v;
            }
        }

        byte[] vertices = new byte[order];
        byte[][] edges = new byte[order][order];

        for (int v = 0; v < order; ++v) {
            vertices[v] = this.vertices[map[v]];

            for (int u = 0; u < order; ++u) {
                edges[v][u] = this.edges[map[v]][map[u]];
            }
        }

        return new Graph(id, vertices, edges);
    }

    public boolean isConnected() {
        ArrayDeque<Integer> open = new ArrayDeque<>();
        ArrayList<Integer> closed = new ArrayList<>();

        open.add(0);
        closed.add(0);

        final int n = order();

        while (!open.isEmpty()) {
            int v = open.poll();

            for (int u = 0; u < n; ++u) {
                if (edges[v][u] > 0 && !closed.contains(u)) {// v uが繋がっている＆＆uが探索済みでない
                    open.add(u);
                    closed.add(u);
                }
            }
        }

        return closed.size() == n;
    }

    public byte getMaxVertexLabel() {
        byte max = -1;
        for (int v = 0; v < order(); ++v) {
            if (max < vertices[v]) {
                max = vertices[v];
            }
        }
        return max;
    }

    public ArrayList<Integer> getMaxVertexLabels() {
        byte max = -1;
        ArrayList<Integer> max_vertices = new ArrayList<>();
        for (int v = 0; v < order(); ++v) {
            if (max < vertices[v]) {
                max = vertices[v];
            }
        }

        for (int v = 0; v < order(); ++v) {
            if (max == vertices[v]) {
                max_vertices.add(v);
            }
        }

        return max_vertices;
    }

    public byte getMinVertexLabel() {
        byte min = Byte.MAX_VALUE;

        for (int v = 0; v < order(); ++v) {
            if (min > vertices[v]) {
                min = vertices[v];
            }
        }

        return min;
    }

    public List<Integer> getVertexList(int m) {
        ArrayList<Integer> res = new ArrayList<>();

        for (int v = 0; v < order(); ++v) {
            if (vertices[v] == m) {
                res.add(v);
            }
        }

        return res;
    }

    public ArrayList<Integer> get_adj(int v, ArrayList<Integer> closed, int n) {
        ArrayList<Integer> adj = new ArrayList<>(n);
        for (int u = 0; u < n; ++u) {
            if (this.edges[v][u] > 0 && !closed.contains(u)) {
                adj.add(u);
                ;
            }
        }
        return adj;
    }

    public byte getVertexLabel(int index) {
        return vertices[index];
    }

    // 幅優先探索ver
    public Graph set_BFS(int max)// 所望の辺の数
    {
        Queue<Integer> open = new ArrayDeque<>();
        ArrayList<Integer> closed = new ArrayList<>();
        ArrayList<Integer> closed2 = new ArrayList<>();

        Random rand = new Random(0);
        final int n = this.order();

        int[] vertex_status = new int[n]; // 0:white 1:gray 2:brack

        int s = rand.nextInt(n); // 2) Select a vertex from the selected graph at random;
        int count = 0;

        open.add(s);

        while (!open.isEmpty()) {
            int v = open.poll();
            vertex_status[v] = 2;// brack

            for (int u : adjList[v]) {
                if (vertex_status[u] == 0) {
                    vertex_status[u] = 1;// gray
                    open.add(u);
                } else if (vertex_status[u] == 2) {
                    if (!closed2.contains(v))
                        closed2.add(v);
                    if (!closed2.contains(u))
                        closed2.add(u);
                    closed.add(v);
                    closed.add(u);

                    count++;

                    if (count == max) {
                        int size = closed2.size();
                        byte[] vertices = new byte[size];
                        byte[][] edges = new byte[size][size];
                        int index = 0;
                        for (int t : closed2) {
                            vertices[index++] = this.vertices[t];
                        }

                        for (int j = 0; j < closed.size(); j += 2) {
                            int a = closed.get(j);
                            int b = closed.get(j + 1);
                            edges[closed2.indexOf(a)][closed2.indexOf(b)] = this.edges[a][b];
                            edges[closed2.indexOf(b)][closed2.indexOf(a)] = this.edges[b][a];
                        }
                        return new Graph(id, vertices, edges);
                    }
                }
            }
        }
        return null;
    }

    // ランダムウォークver
    public Graph set_ramQ(int max) {
        Deque<Integer> open = new ArrayDeque<>();
        ArrayList<Integer> closed = new ArrayList<>();

        Random rand = new Random(0);
        final int n = this.order();
        byte[] vertices = new byte[1];
        byte[][] edges = new byte[1][1];

        boolean[][] check_path = new boolean[n][n];

        int s = rand.nextInt(n); // 2) Select a vertex from the selected graph at random;
        int count = 0;
        int i = 0;

        ArrayList<Integer> adj = new ArrayList<>(n);

        open.push(s);

        vertices[i] = this.vertices[s];
        closed.add(s);

        while (!open.isEmpty()) {
            int v = open.pop();
            for (int u : adjList[v]) {// 隣接頂点を得る
                if (!check_path[v][u]) {
                    adj.add(u);// 頂点ｖの隣接リスト
                }
            }

            if (adj.size() == 0)
                continue;

            open.push(v);

            int nextV = adj.get(rand.nextInt(adj.size()));// ランダムに頂点を決定

            if (!closed.contains(nextV)) {
                closed.add(nextV);

                byte[] vertices1 = vertices.clone();// 避難
                vertices = new byte[vertices.length + 1];// サイズ拡張
                System.arraycopy(vertices1, 0, vertices, 0, vertices1.length);

                vertices[++i] = this.vertices[nextV];

                byte[][] edge1 = edges.clone();// 避難
                edges = new byte[edges.length + 1][edges.length + 1];// サイズ拡張
                for (int j = 0; j < edge1.length; j++) {
                    for (int k = 0; k < edge1.length; k++) {
                        edges[j][k] = edge1[j][k];
                    }
                }
            }
            edges[closed.indexOf(v)][closed.indexOf(nextV)] = this.edges[v][nextV];
            edges[closed.indexOf(nextV)][closed.indexOf(v)] = this.edges[nextV][v];
            check_path[v][nextV] = true;
            check_path[nextV][v] = true;

            count++;

            if (count == max) {
                return new Graph(id, vertices, edges);
            }

            open.push(nextV);
            adj.clear();
        }
        return null;
    }

    public static Graph createQueryByBFS(Graph graph, int size) {

        Queue<Integer> queue = new ArrayDeque<>();
        List<Pair<Integer, Integer>> edges = new ArrayList<Pair<Integer, Integer>>();
        HashSet<Integer> visit = new HashSet<Integer>();

        Random rand = new Random();

        int v = rand.nextInt(graph.order());
        queue.add(v);
        Pair<Integer, Integer> pair;

        while (edges.size() != size && !queue.isEmpty()) {
            v = queue.poll();
            if (!visit.contains(v)) {
                visit.add(v);

                for (int u : graph.adjList[v]) {
                    queue.add(u);
                    if (visit.contains(u)) {
                        if (u < v)
                            pair = new Pair(u, v);
                        else
                            pair = new Pair(v, u);

                        if (!contains(edges, pair)) {
                            edges.add(pair);
                        }
                    }

                    if (edges.size() == size)
                        break;
                }
            }
        }

        Graph newGraph = graph.createSubgraph(graph, edges);

        return newGraph;

    }

    static boolean contains(List<Pair<Integer, Integer>> edges, Pair<Integer, Integer> pair) {
        for (Pair<Integer, Integer> p : edges) {
            if (p.left == pair.left && p.right == pair.right) {
                return true;
            }
        }
        return false;
    }

    public Graph createSubgraph(Graph base, List<Pair<Integer, Integer>> edgeList) {

        ArrayList<Integer> map = new ArrayList<Integer>();
        for (Pair<Integer, Integer> edge : edgeList) {
            if (!map.contains(edge.left))
                map.add(edge.left);
            if (!map.contains(edge.right))
                map.add(edge.right);
        }

        int order = map.size();

        byte[] vertices = new byte[order];
        byte[][] edges = new byte[order][order];

        for (int v = 0; v < order; ++v) {
            vertices[v] = base.vertices[map.get(v)];
        }

        for (Pair<Integer, Integer> edge : edgeList) {
            int v = map.indexOf(edge.left);
            int u = map.indexOf(edge.right);
            edges[v][u] = base.edges[edge.left][edge.right];
            edges[u][v] = edges[v][u];
        }

        return new Graph(-1, vertices, edges);

    }
}

// private Map<Byte, List<Integer>> makeVlabelMap() {
// Map<Byte, List<Integer>> vertexMap = new HashMap<>();
// for (int i = 0; i < this.order; i++) {
// if (!vertexMap.containsKey(vertices[i])) {
// vertexMap.put(vertices[i], new ArrayList<Integer>());
// }
// vertexMap.get(vertices[i]).add(i);
// }

// return vertexMap;
// }

// public Graph shirinkNEC() {

// int order = 0;
// int[] map = new int[order()];
// ArrayList<Integer> remove = new ArrayList<>();

// for (int v = 0; v < this.order; ++v) {
// if (this.adjList[v].length > 1 || remove.contains(v))
// continue;

// if (this.adjList[v].length == 0) {
// remove.add(v);
// continue;
// }
// int adj = this.adjList[v][0];

// for (int u : this.adjList[adj]) {
// if (this.adjList[u].length > 1 || u == v || vertices[u] != vertices[v])
// continue;
// remove.add(u);
// }
// }

// for (int v = 0; v < this.order; v++) {
// if (!remove.contains(v)) {
// map[order++] = v;
// }
// }
// byte[] vertices = new byte[order];
// byte[][] edges = new byte[order][order];

// for (int v = 0; v < order; ++v) {
// vertices[v] = this.vertices[map[v]];

// for (int u = 0; u < order; ++u) {
// edges[v][u] = this.edges[map[v]][map[u]];
// }
// }
// return new Graph(id, vertices, edges);
// }