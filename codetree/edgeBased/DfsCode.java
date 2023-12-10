package codetree.edgeBased;

import java.util.*;

import codetree.common.Pair;
import codetree.core.*;

public class DfsCode
        implements GraphCode {
    @Override
    public List<CodeFragment> computeCanonicalCode(Graph g, int b) {
        // final int n = g.size();
        // ArrayList<CodeFragment> code = new ArrayList<>(n + 1);

        // ArrayList<DfsSearchInfo> infoList1 = new ArrayList<>();
        // ArrayList<DfsSearchInfo> infoList2 = new ArrayList<>(b);

        // final byte min = g.getMinVertexLabel();
        // code.add(new DfsCodeFragment(min, (byte) -1, -1));

        // List<Integer> minVertexList = g.getVertexList(min);
        // for (int v0 : minVertexList) {
        // infoList1.add(new DfsSearchInfo(g, v0));
        // }

        // for (int i = 0; i < n; ++i) {
        // DfsCodeFragment minFrag = new DfsCodeFragment();

        // for (DfsSearchInfo info : infoList1) {
        // while (!info.rightmostPath.isEmpty()) {
        // int v = info.rightmostPath.peek();

        // int[] adj = g.adjList[v];
        // for (int u : adj) {
        // if (info.closed[v][u]) {
        // continue;
        // }

        // DfsCodeFragment frag = null;
        // if (info.closed[u][u]) {
        // if (info.map[u] < info.map[v]) { // backward edge
        // frag = new DfsCodeFragment((byte) -1, g.edges[u][v], info.map[u]);
        // }
        // } else { // forward edge
        // frag = new DfsCodeFragment(g.vertices[u], g.edges[v][u], info.map[v]);
        // }

        // if (frag != null) {
        // final int cmpres = minFrag.isMoreCanonicalThan(frag);
        // if (cmpres < 0) {
        // minFrag = frag;

        // infoList2.clear();
        // infoList2.add(new DfsSearchInfo(info, v, u));
        // } else if (cmpres == 0 && infoList2.size() < b) {
        // infoList2.add(new DfsSearchInfo(info, v, u));
        // }
        // }
        // }

        // if (infoList2.size() > 0) {
        // break;
        // }

        // info.rightmostPath.pop();
        // }
        // }

        // code.add(minFrag);

        // infoList1 = infoList2;
        // infoList2 = new ArrayList<>(b);
        // }

        // return code;
        return null;
    }

    @Override
    public List<Pair<IndexNode, SearchInfo>> beginSearch(Graph graph, IndexNode root) {
        ArrayList<Pair<IndexNode, SearchInfo>> infoList = new ArrayList<>();

        for (IndexNode m : root.children) {
            DfsCodeFragment frag = (DfsCodeFragment) m.frag;
            for (int v = 0; v < graph.order(); ++v) {
                if (graph.vertices[v] == frag.vLabel) {
                    infoList.add(new Pair<IndexNode, SearchInfo>(m, new DfsSearchInfo(graph, v)));
                }
            }
        }

        return infoList;
    }

    @Override
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph graph, SearchInfo info0) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        DfsSearchInfo info = (DfsSearchInfo) info0;

        boolean backtrack = false;
        while (!info.rightmostPath.isEmpty()) {
            int v = info.rightmostPath.peek();

            int[] adj = graph.adjList[v];
            for (int u : adj) {
                // if (info.closed[v][u]) {
                if (info.closedBitset.get(v).get(u)) {
                    continue;
                }

                // if (info.closed[u][u]) { // backward edge
                if (info.closedBitset.get(u).get(u)) { // backward edge
                    if (!backtrack) {
                        frags.add(new Pair<CodeFragment, SearchInfo>(
                                new DfsCodeFragment((byte) -1, graph.edges[u][v], info.map[u]),
                                new DfsSearchInfo(info, v, u)));
                    }
                } else { // forward edge
                    frags.add(new Pair<CodeFragment, SearchInfo>(
                            new DfsCodeFragment(graph.vertices[u], graph.edges[v][u], info.map[v]),
                            new DfsSearchInfo(info, v, u)));
                }
            }

            info.rightmostPath.pop();
            backtrack = true;
        }

        return frags;
        // return null;
    }

    @Override
    public List<CodeFragment> computeCanonicalCode(Graph g, int start, int limDepth) {
        final int n = g.size();
        ArrayList<CodeFragment> code = new ArrayList<>(n + 1);

        ArrayList<DfsSearchInfo> infoList1 = new ArrayList<>();

        code.add(new DfsCodeFragment(g.vertices[start], (byte) -1, -1));

        infoList1.add(new DfsSearchInfo(g, start));
        Random rand = new Random(0);

        for (int i = 0; i < limDepth - 1; ++i) {// edge-1=vnum
            ArrayList<Integer> next = new ArrayList<>();
            int now = code.size();

            for (DfsSearchInfo info : infoList1) {
                while (!info.rightmostPath.isEmpty()) {
                    int v = info.rightmostPath.peek();

                    int[] adj = g.adjList[v];
                    for (int u : adj) {
                        // if (!info.closed[v][u]) {
                        if (!info.closedBitset.get(v).get(u)) {
                            next.add(u);
                        }
                    }
                    if (next.size() == 0) {
                        info.rightmostPath.pop();
                        continue;
                    }

                    int random = rand.nextInt(next.size());
                    int u2 = next.get(random);

                    DfsCodeFragment frag = null;
                    // if (info.closed[u2][u2]) {
                    if (info.closedBitset.get(u2).get(u2)) {
                        if (info.map[u2] < info.map[v]) { // backward edge
                            frag = new DfsCodeFragment((byte) -1, g.edges[u2][v], info.map[u2]);
                        }
                    } else { // forward edge
                        frag = new DfsCodeFragment(g.vertices[u2], g.edges[v][u2], info.map[v]);
                    }

                    infoList1.clear();
                    infoList1.add(new DfsSearchInfo(info, v, u2));
                    code.add(frag);
                    break;
                }
            }
            if (now == code.size()) {
                return code;
            }
        }
        return code;
    }

    @Override
    public List<ArrayList<CodeFragment>> computeCanonicalCode(int labels_length) {
        List<ArrayList<CodeFragment>> codeList = new ArrayList<>(labels_length);
        for (int i = 0; i < labels_length; i++) {
            ArrayList<CodeFragment> code = new ArrayList<>(1);
            code.add(new DfsCodeFragment((byte) i, (byte) -1, -1));
            codeList.add(code);
        }
        return codeList;
    }

    @Override
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph graph, SearchInfo info0,
            HashSet<Byte> childrenVlabel) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        DfsSearchInfo info = (DfsSearchInfo) info0;

        boolean backtrack = false;
        while (!info.rightmostPath.isEmpty()) {
            int v = info.rightmostPath.peek();

            int[] adj = graph.adjList[v];
            for (int u : adj) {
                // if (info.closed[v][u] || !childrenVlabel.contains(graph.vertices[u])) {
                if (info.closedBitset.get(v).get(u) || !childrenVlabel.contains(graph.vertices[u])) {
                    continue;
                }

                // if (info.closed[u][u]) { // backward edge
                if (info.closedBitset.get(u).get(u)) { // backward edge
                    if (!backtrack) {
                        frags.add(new Pair<CodeFragment, SearchInfo>(
                                new DfsCodeFragment((byte) -1, graph.edges[u][v], info.map[u]),
                                new DfsSearchInfo(info, v, u)));
                    }
                } else { // forward edge
                    frags.add(new Pair<CodeFragment, SearchInfo>(
                            new DfsCodeFragment(graph.vertices[u], graph.edges[v][u], info.map[v]),
                            new DfsSearchInfo(info, v, u)));
                }
            }

            info.rightmostPath.pop();
            backtrack = true;
        }

        return frags;
    }

    @Override
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph graph, SearchInfo info0,
            HashSet<Byte> childrenVlabel, BitSet childEdgeFrag) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        DfsSearchInfo info = (DfsSearchInfo) info0;

        boolean backtrack = false;
        while (!info.rightmostPath.isEmpty()) {
            int v = info.rightmostPath.peek();

            int[] adj = graph.adjList[v];
            for (int u : adj) {
                // if (info.closed[v][u] || !childrenVlabel.contains(graph.vertices[u])) {
                if (info.closedBitset.get(v).get(u) || !childrenVlabel.contains(graph.vertices[u])) {
                    continue;
                }

                // if (info.closed[u][u]) { // backward edge
                if (info.closedBitset.get(u).get(u)) { // backward edge
                    if (!backtrack) {
                        frags.add(new Pair<CodeFragment, SearchInfo>(
                                new DfsCodeFragment((byte) -1, graph.edges[u][v], info.map[u]),
                                new DfsSearchInfo(info, v, u)));
                    }
                } else { // forward edge
                    frags.add(new Pair<CodeFragment, SearchInfo>(
                            new DfsCodeFragment(graph.vertices[u], graph.edges[v][u], info.map[v]),
                            new DfsSearchInfo(info, v, u)));
                }
            }

            info.rightmostPath.pop();
            backtrack = true;
        }

        return frags;
    }

    @Override
    public Graph generateGraph(List<CodeFragment> code, int nodeID) {
        byte[] vertices = new byte[code.size() + 1];
        byte[][] edges = new byte[code.size() + 1][code.size() + 1];
        int index = 0;
        for (CodeFragment c : code) {
            vertices[index] = c.getVlabel();
            int parent = c.getParent();
            if (parent != -1) {
                edges[index][parent] = 1;
                edges[parent][index] = 1;
            }
            index++;
        }
        return new Graph(nodeID, vertices, edges);
    }

}
