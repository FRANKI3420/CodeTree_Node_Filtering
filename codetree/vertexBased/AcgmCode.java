package codetree.vertexBased;

import java.util.*;

import codetree.common.Pair;
import codetree.core.*;

import java.io.Serializable;

public class AcgmCode
        implements GraphCode, Serializable {

    @Override
    public List<ArrayList<CodeFragment>> computeCanonicalCode(int labels_length) {
        List<ArrayList<CodeFragment>> codeList = new ArrayList<>(labels_length);
        for (int i = 0; i < labels_length; i++) {
            ArrayList<CodeFragment> code = new ArrayList<>(1);
            code.add(new AcgmCodeFragment((byte) i, 0));
            codeList.add(code);
        }
        return codeList;
    }

    // super graph search
    @Override
    public List<CodeFragment> computeCanonicalCode(Graph g, int b) {
        final int n = g.order();
        ArrayList<CodeFragment> code = new ArrayList<>(n);

        ArrayList<AcgmSearchInfo> infoList1 = new ArrayList<>();
        ArrayList<AcgmSearchInfo> infoList2 = new ArrayList<>(b);

        // origin
        final byte max = g.getMaxVertexLabel();
        code.add(new AcgmCodeFragment(max, 0));
        List<Integer> maxVertexList = g.getVertexList(max);
        for (int v0 : maxVertexList) {
            infoList1.add(new AcgmSearchInfo(g, v0));
        }

        for (int depth = 1; depth < n; ++depth) {
            AcgmCodeFragment maxFrag = new AcgmCodeFragment((byte) -1, depth);

            byte[] eLabels = new byte[depth];
            for (AcgmSearchInfo info : infoList1) {
                for (int v = 0; v < n; ++v) {
                    if (!info.open.get(v)) {
                        continue;
                    }

                    for (int i = 0; i < depth; ++i) {
                        final int u = info.vertexIDs[i];
                        eLabels[i] = g.edges[u][v];
                    }

                    AcgmCodeFragment frag = new AcgmCodeFragment(g.vertices[v], eLabels);
                    final int cmpres = maxFrag.isMoreCanonicalThan(frag);
                    if (cmpres < 0) {
                        maxFrag = frag;

                        infoList2.clear();
                        infoList2.add(new AcgmSearchInfo(info, g, v));
                    } else if (cmpres == 0 && infoList2.size() < b) {
                        infoList2.add(new AcgmSearchInfo(info, g, v));
                    }
                }
            }

            code.add(maxFrag);

            infoList1 = infoList2;
            infoList2 = new ArrayList<>(b);
        }

        return code;
    }

    @Override
    public List<CodeFragment> computeCanonicalCode(Graph g, int start, int limDepth) {
        final int n = g.order();
        ArrayList<CodeFragment> code = new ArrayList<>(n);
        ArrayList<AcgmSearchInfo> infoList1 = new ArrayList<>();

        code.add(new AcgmCodeFragment(g.vertices[start], 0));

        infoList1.add(new AcgmSearchInfo(g, start));

        Random rand = new Random(0);

        for (int depth = 1; depth < limDepth; ++depth) {
            byte[] eLabels = new byte[depth];
            ArrayList<Integer> next = new ArrayList<>();

            for (AcgmSearchInfo info : infoList1) {

                for (int v = 0; v < n; ++v) {
                    if (info.open.get(v)) {
                        next.add(v);
                    }
                }
                if (next.size() == 0) {
                    return code;
                }

                int random = rand.nextInt(next.size());
                int v2 = next.get(random);

                for (int i = 0; i < depth; ++i) {
                    final int u = info.vertexIDs[i];
                    eLabels[i] = g.edges[u][v2];
                }

                AcgmCodeFragment frag = new AcgmCodeFragment(g.vertices[v2], eLabels);
                infoList1.clear();
                infoList1.add(new AcgmSearchInfo(info, g, v2));
                code.add(frag);
            }
        }
        return code;
    }

    @Override // 最初の探索候補となる頂点を全出力
    public List<Pair<IndexNode, SearchInfo>> beginSearch(Graph g, IndexNode root) {
        ArrayList<Pair<IndexNode, SearchInfo>> infoList = new ArrayList<>();

        for (IndexNode m : root.children) {
            for (int v = 0; v < g.order; ++v) {
                AcgmCodeFragment frag = (AcgmCodeFragment) m.frag;
                if (g.vertices[v] == frag.vLabel) {// 頂点だけ注目すれば良い
                    infoList.add(new Pair<IndexNode, SearchInfo>(m, new AcgmSearchInfo(g, v)));
                }
            }
        }

        return infoList;
    }

    @Override // super graph
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info0) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        AcgmSearchInfo info = (AcgmSearchInfo) info0;

        int n = g.order;

        final int depth = info.vertexIDs.length;

        byte[] eLabels = new byte[depth];
        for (int v = 0; v < n; ++v) {

            if (!info.open.get(v)) {
                continue;
            }

            for (int i = 0; i < depth; ++i) {
                final int u = info.vertexIDs[i];
                eLabels[i] = g.edges[u][v];
            }

            frags.add(new Pair<CodeFragment, SearchInfo>(
                    new AcgmCodeFragment(g.vertices[v], eLabels), new AcgmSearchInfo(info, g, v)));
        }

        return frags;
    }

    @Override // subgraph search
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info0,
            HashSet<Byte> childrenVlabel) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        AcgmSearchInfo info = (AcgmSearchInfo) info0;

        final int depth = info.vertexIDs.length;

        byte[] eLabels = new byte[depth];

        for (int v = info.open.nextSetBit(0); v != -1; v = info.open
                .nextSetBit(++v)) {

            if (!childrenVlabel.contains(g.vertices[v])) {
                continue;
            }

            for (int i = 0; i < depth; ++i) {
                final int u = info.vertexIDs[i];
                eLabels[i] = g.edges[u][v];// 辺ラベル決定
            }

            frags.add(new Pair<CodeFragment, SearchInfo>(
                    new AcgmCodeFragment(g.vertices[v], eLabels), new AcgmSearchInfo(info, g, v)));
        }

        return frags;
    }

    BitSet openBitSet = new BitSet();

    @Override
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info0,
            HashSet<Byte> childrenVlabel, BitSet childEdgeFrag) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        AcgmSearchInfo info = (AcgmSearchInfo) info0;

        final int depth = info.vertexIDs.length;

        byte[] eLabels = new byte[depth];

        openBitSet.clear();
        for (int v = childEdgeFrag.nextSetBit(0); v != -1; v = childEdgeFrag.nextSetBit(++v)) {
            int u = info.vertexIDs[v];
            openBitSet.or(g.edgeBitset.get(u));
        }

        for (int v = openBitSet.nextSetBit(0); v != -1; v = openBitSet.nextSetBit(++v)) {

            if (!childrenVlabel.contains(g.vertices[v])) {
                continue;
            }

            if (contain(info.vertexIDs, v))
                continue;

            for (int i = 0; i < depth; ++i) {
                final int u = info.vertexIDs[i];
                eLabels[i] = g.edges[u][v];
            }

            frags.add(new Pair<CodeFragment, SearchInfo>(
                    new AcgmCodeFragment(g.vertices[v], eLabels), new AcgmSearchInfo(info, g, v, 0)));
        }

        return frags;
    }

    boolean contain(int[] vertexIDs, int num) {
        for (int i : vertexIDs) {
            if (i == num) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<CodeFragment> computeCode(Graph g, int start, int limDepth) {
        final int n = g.order();
        ArrayList<CodeFragment> code = new ArrayList<>(limDepth);
        ArrayList<AcgmSearchInfo> infoList1 = new ArrayList<>();

        start = g.minDegreeVertices();

        code.add(new AcgmCodeFragment(g.vertices[start], 0));

        infoList1.add(new AcgmSearchInfo(g, start));

        Random rand = new Random(0);

        ArrayList<Integer> next = new ArrayList<>();
        ArrayList<Integer> past = new ArrayList<>();
        for (int depth = 1; depth < limDepth; ++depth) {
            byte[] eLabels = new byte[depth];
            next.clear();
            past.clear();

            for (AcgmSearchInfo info : infoList1) {

                for (int v = 0; v < n; ++v) {
                    if (info.open.get(v) && g.edgeBitset.get(info.vertexIDs[depth - 1]).get(v)) {
                        next.add(v);
                    } else if (info.open.get(v)) {
                        past.add(v);
                    }
                }
                int v2 = 0;
                if (next.size() == 0) {
                    if (past.size() == 0) {
                        return code;
                    }
                    int random = rand.nextInt(past.size());
                    v2 = past.get(random);
                    for (int i = 0; i < depth - 1; ++i) {
                        final int u = info.vertexIDs[i];
                        eLabels[i] = g.edges[u][v2];
                    }
                    eLabels[depth - 1] = 1;

                } else {
                    int random = rand.nextInt(next.size());
                    v2 = next.get(random);

                    for (int i = 0; i < depth; ++i) {
                        final int u = info.vertexIDs[i];
                        eLabels[i] = g.edges[u][v2];
                    }
                }

                AcgmCodeFragment frag = new AcgmCodeFragment(g.vertices[v2], eLabels);
                infoList1.clear();
                infoList1.add(new AcgmSearchInfo(info, g, v2));
                code.add(frag);
            }
        }
        return code;
    }

    static long contain_search_time = 0;

    @Override
    public List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments_adj2(Graph g, SearchInfo info0,
            HashSet<Byte> childrenVlabel, BitSet childEdgeFrag, int index) {
        ArrayList<Pair<CodeFragment, SearchInfo>> frags = new ArrayList<>();

        AcgmSearchInfo info = (AcgmSearchInfo) info0;

        final int depth = info.vertexIDs.length;

        byte[] eLabels = new byte[depth];

        openBitSet.clear();
        // openBitSet.or(g.edgeBitset.get(info.vertexIDs[index]));
        // if (childEdgeFrag.cardinality() > 1) {
        // System.out.println(childEdgeFrag.toString());
        // }
        if (g.id == 19) {
            System.out.println((double) contain_search_time / 1000 / 1000 / 1000 + "s");
        }
        int count = 0;
        for (int v = childEdgeFrag.nextSetBit(0); v != -1; v = childEdgeFrag.nextSetBit(++v)) {
            int u = info.vertexIDs[v];
            if (count == 0) {
                openBitSet.or(g.edgeBitset.get(u));
            } else {
                openBitSet.and(g.edgeBitset.get(u));
            }
            count++;
        }

        for (int v = openBitSet.nextSetBit(0); v != -1; v = openBitSet.nextSetBit(++v)) {
            // long time = System.nanoTime();

            if (!childrenVlabel.contains(g.vertices[v])) {
                // contain_search_time += System.nanoTime() - time;
                continue;
            }
            // contain_search_time += System.nanoTime() - time;

            // long time = System.nanoTime();

            if (contain(info.vertexIDs, v)) {
                // contain_search_time += System.nanoTime() - time;
                continue;
            }
            // contain_search_time += System.nanoTime() - time;

            for (int i = 0; i < depth; ++i) {
                final int u = info.vertexIDs[i];
                eLabels[i] = g.edges[u][v];
            }

            frags.add(new Pair<CodeFragment, SearchInfo>(
                    new AcgmCodeFragment(g.vertices[v], eLabels), new AcgmSearchInfo(info, g, v, 0)));
        }

        return frags;
    }

}
