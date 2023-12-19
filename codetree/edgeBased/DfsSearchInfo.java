package codetree.edgeBased;

import java.util.*;

import codetree.core.*;

class DfsSearchInfo
        implements SearchInfo {
    Stack<Integer> rightmostPath;

    // boolean[][] closed;
    // HashMap<Integer, BitSet> closedBitset;
    // int numVertices;// 探索された頂点数
    // int[] map;

    int[] vertexIDs;

    // 頂点１における探索状態
    DfsSearchInfo(Graph graph, int v0) {
        rightmostPath = new Stack<>();
        rightmostPath.push(v0);

        // final int n = graph.order();

        // closed = new boolean[n][n];
        // closed[v0][v0] = true;

        // closedBitset = new HashMap<>();
        // for (int i = 0; i < n; i++) {
        // closedBitset.put(i, new BitSet(n));
        // }
        // closedBitset.get(v0).set(v0);

        // numVertices = 1;
        // map = new int[n];

        vertexIDs = new int[1];
        vertexIDs[0] = v0;
    }

    @SuppressWarnings("unchecked")
    DfsSearchInfo(DfsSearchInfo src, int v, int u) {
        if (src.rightmostPath.peek() != v) {
            throw new IllegalArgumentException("Illegal vertex.");
        }

        rightmostPath = (Stack<Integer>) src.rightmostPath.clone();

        final int n = src.vertexIDs.length;
        // if (src.closedBitset.get(u).get(u)) { // backward edge
        if (checkContain(u, src.vertexIDs)) { // backward edge
            vertexIDs = new int[n];
            System.arraycopy(src.vertexIDs, 0, vertexIDs, 0, n);
        } else { // forward edge
            rightmostPath.push(u);
            vertexIDs = new int[n + 1];
            System.arraycopy(src.vertexIDs, 0, vertexIDs, 0, n);
            vertexIDs[n] = u;
        }

        // closed = cloneMatrix(src.closed);
        // closed[v][u] = true;
        // closed[u][v] = true;
        // closed[v][v] = true;
        // closed[u][u] = true;

        // closedBitset = (HashMap<Integer, BitSet>) src.closedBitset.clone();

        // closedBitset = cloneMatrix(src.closedBitset);
        // closedBitset.get(v).set(u);
        // // closedBitset.put(u, new BitSet());
        // closedBitset.get(u).set(v);
        // closedBitset.get(v).set(v);
        // closedBitset.get(u).set(u);

        // map = src.map.clone();
        // // if (src.closed[u][u]) { // backward edge
        // if (src.closedBitset.get(u).get(u)) { // backward edge
        // numVertices = src.numVertices;
        // } else { // forward edge
        // rightmostPath.push(u);
        // map[u] = src.numVertices;
        // numVertices = src.numVertices + 1;
        // }

    }

    private boolean checkContain(int v, int[] vertexIDs) {
        for (int i : vertexIDs) {
            if (i == v) {
                return true;
            }
        }
        return false;
    }

    private HashMap<Integer, BitSet> cloneMatrix(HashMap<Integer, BitSet> src) {
        HashMap<Integer, BitSet> dest = new HashMap<>();

        // for (int i = 0; i < src.size(); ++i) {
        // dest.put(i, (BitSet) src.get(i).clone());
        // }
        for (Map.Entry<Integer, BitSet> entry : src.entrySet()) {
            dest.put(entry.getKey(), (BitSet) entry.getValue().clone());
        }

        return dest;
    }

    private static boolean[][] cloneMatrix(boolean[][] src) {
        boolean[][] dest = new boolean[src.length][src[0].length];

        for (int i = 0; i < src.length; ++i) {
            dest[i] = src[i].clone();
        }
        return dest;
    }

    @Override
    public BitSet getOpen() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOpen'");
    }

    @Override
    public BitSet getClose() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getClose'");
    }

    @Override
    public int[] getVertexIDs() {
        int[] vers = new int[2];
        vers[0] = this.rightmostPath.peek();
        return vers;
    }
}
