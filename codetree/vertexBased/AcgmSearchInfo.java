package codetree.vertexBased;

import java.util.ArrayList;
import java.util.BitSet;

import codetree.core.*;

final class AcgmSearchInfo
        implements SearchInfo {

    BitSet open;
    BitSet closed;

    int[] vertexIDs;

    // ArrayList<Integer> vertexIDsList;

    AcgmSearchInfo(Graph g, int v0) {
        final int n = g.order();

        open = new BitSet(n);

        int[] adj = g.adjList[v0];
        for (int u : adj) {
            open.set(u);
        }

        // closed = new BitSet(n);
        // closed.set(v0);

        vertexIDs = new int[1];
        vertexIDs[0] = v0;

        // vertexIDsList = new ArrayList<>();
        // vertexIDsList.add(v0);
    }

    AcgmSearchInfo(AcgmSearchInfo src, Graph g, Integer v) {

        open = (BitSet) src.open.clone();
        // closed = (BitSet) src.closed.clone();

        final int n = src.vertexIDs.length;
        vertexIDs = new int[n + 1];
        System.arraycopy(src.vertexIDs, 0, vertexIDs, 0, n);
        vertexIDs[n] = v;

        open.set(v, false);
        // closed.set(v);

        int[] adj = g.adjList[v];
        // for (int u : adj) {
        // if (!closed.get(u)) {
        // open.set(u);
        // }
        // }
        for (int u : adj) {
            if (contain(vertexIDs, u)) {
                open.set(u);
            }
        }
    }

    boolean contain(int[] vertexIDs, int num) {
        for (int i : vertexIDs) {
            if (i == num)
                return false;
        }
        return true;
    }

    // // no clone
    // AcgmSearchInfo(AcgmSearchInfo src, Graph g, Integer v, int i) {

    // open = src.open;
    // closed = src.closed;

    // // vertexIDsList = src.vertexIDsList;

    // // vertexIDsList.add(v);

    // // final int n = src.vertexIDs.length;
    // // vertexIDs = new int[n + 1];
    // // System.arraycopy(src.vertexIDs, 0, vertexIDs, 0, n);
    // // vertexIDs[n] = v;

    // open.set(v, false);
    // closed.set(v);

    // int[] adj = g.adjList[v];
    // for (int u : adj) {
    // if (!closed.get(u)) {
    // open.set(u);
    // }
    // }
    // }

    // // undo
    // public AcgmSearchInfo(Graph g, AcgmSearchInfo src) {

    // // final int n = src.vertexIDs.length;
    // // int v = src.vertexIDs[n - 1];

    // vertexIDsList = src.vertexIDsList;

    // final int n = vertexIDsList.size();
    // int v = vertexIDsList.get(n - 1);
    // vertexIDsList.remove(n - 1);

    // open = src.open;
    // closed = src.closed;

    // // vertexIDs = new int[n - 1];
    // // System.arraycopy(src.vertexIDs, 0, vertexIDs, 0, n - 1);

    // open.set(v, true);
    // closed.set(v, false);

    // open.clear();

    // for (int w : vertexIDsList) {

    // int[] adj = g.adjList[w];
    // for (int u : adj) {
    // if (!closed.get(u))
    // open.set(u);
    // }
    // }
    // // int[] adj = g.adjList[v];
    // // for (int u : adj) {
    // // open.set(u, false);
    // // }
    // }

    @Override
    public BitSet getOpen() {
        return this.open;
    }

    @Override
    public BitSet getClose() {
        return this.closed;
    }

    @Override
    public int[] getVertexIDs() {
        return this.vertexIDs;
    }
}
