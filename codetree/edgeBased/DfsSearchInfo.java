package codetree.edgeBased;

import java.util.*;

import codetree.core.*;

class DfsSearchInfo
        implements SearchInfo {
    Stack<Integer> rightmostPath;

    boolean[][] closed;

    int numVertices;// 探索された頂点数
    int[] map;

    // 頂点１における探索状態
    DfsSearchInfo(Graph graph, int v0) {
        rightmostPath = new Stack<>();
        rightmostPath.push(v0);

        final int n = graph.order();

        closed = new boolean[n][n];
        closed[v0][v0] = true;

        numVertices = 1;
        map = new int[n];
    }

    @SuppressWarnings("unchecked")
    DfsSearchInfo(DfsSearchInfo src, int v, int u) {
        if (src.rightmostPath.peek() != v) {
            throw new IllegalArgumentException("Illegal vertex.");
        }

        rightmostPath = (Stack<Integer>) src.rightmostPath.clone();

        closed = cloneMatrix(src.closed);
        closed[v][u] = true;
        closed[u][v] = true;
        closed[v][v] = true;
        closed[u][u] = true;

        map = src.map.clone();
        if (src.closed[u][u]) { // backward edge
            numVertices = src.numVertices;
        } else { // forward edge
            rightmostPath.push(u);
            map[u] = src.numVertices;
            numVertices = src.numVertices + 1;
        }

    }

    private static boolean[][] cloneMatrix(boolean[][] src) {
        boolean[][] dest = new boolean[src.length][src[0].length];

        for (int i = 0; i < src.length; ++i) {
            dest[i] = src[i].clone();
        }

        return dest;
    }
    // @Override
    // public boolean check(SearchInfo info,Graph q)
    // {
    // DfsSearchInfo info0 = ( DfsSearchInfo )info;
    // if(info0.numVertices==q.vertices.length){
    // return true;
    // }
    // return false;
    // /*
    // for(int i=0;i<info0.closed.length;i++){
    // for(int j=0;j<info0.closed.length;j++){
    // if(info0.closed[i][j]==true){
    // return false;
    // }

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

    // }
    // }
    // return true;
    // */
    // }
}
