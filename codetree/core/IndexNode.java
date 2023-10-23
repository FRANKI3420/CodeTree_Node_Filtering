package codetree.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import codetree.common.Pair;

public class IndexNode implements Serializable {
    public IndexNode parent;
    public ArrayList<IndexNode> children;
    public CodeFragment frag;
    protected ArrayList<Integer> matchGraphIndices;
    protected int count;
    protected int depth;
    protected boolean supNode;
    protected BitSet matchGraphIndicesBitSet;
    public HashSet<Byte> adjLabels;
    protected int nodeID;

    protected BitSet traverseCount1;
    protected BitSet traverseCount2;

    protected int traverse_num = 0;
    // protected int[] g_traverse_num;
    // protected LinkedHashMap<Integer, int[]> mapping;
    protected LinkedHashMap<Integer, Integer> mapping2;
    protected int qMapping;

    static BitSet In = new BitSet();
    static BitSet Can = new BitSet();
    static BitSet result = new BitSet();
    static BitSet U = new BitSet();

    static ArrayList<Integer> can = new ArrayList<>();
    static final Runtime runtime = Runtime.getRuntime();
    static final Path out = Paths.get("datagraph.gfu");
    static ArrayList<String> lines = new ArrayList<>();
    static final Path path = Paths.get("output2.txt");

    static int nodeIDcount = 0;
    static long search_time = 0;
    static int totoal_kai = 0;
    static int doukeicount = 0;
    static long write_time = 0;
    static long process_time = 0;
    static long read_time = 0;
    static double verification_time = 0;
    static double total_time = 0;
    static double fileter_time = 0;
    static double verify_time = 0;
    static double query_per_veq = 0;
    static double query_per_veqF = 0;
    static double query_per_veqV = 0;
    static double query_per_sum = 0;
    static double query_per_time = 0;
    static double veq_Can_total = 0;
    static double veq_per_Can = 0;
    static double CT_verify = 0;
    static int nonfail = 0;
    static double FPper_q = 0;
    static double FPre = 0;
    static double filpertime = 0;
    static double SPper_q = 0;
    static String command;
    static Process p;
    static double FP = 0;
    static double SP = 0;
    static double FP2 = 0;
    static int fil_count = 0;
    static int in_count = 0;
    static int a_fil_count = 0;
    static long induce_search = 0;
    static long contain_search = 0;

    static long a_filterTime = 0;
    static ArrayList<IndexNode> removeNode = new ArrayList<>();

    IndexNode(IndexNode parent, CodeFragment frag, int G_size) {
        this.parent = parent;
        this.frag = frag;

        children = new ArrayList<>();
        matchGraphIndicesBitSet = new BitSet();
        matchGraphIndices = new ArrayList<>();

        supNode = false;
        count = 0;
        depth = 0;
        nodeID = 0;
        adjLabels = new HashSet<>();
        traverse_num = 0;
        // g_traverse_num = new int[G_size];
        // mapping = new LinkedHashMap<>();

        traverseCount1 = new BitSet(G_size);
        traverseCount2 = new BitSet(G_size);
        mapping2 = new LinkedHashMap<>();
        qMapping = -1;

    }

    int size() {
        int s = 1;
        for (IndexNode m : children) {
            s += m.size();
        }
        return s;
    }

    static int c = 0;
    static int query_per_nf_count = 0;

    // IndexNode isLowestTraversOne(IndexNode lowest) {
    // for (IndexNode m : children) {
    // if (m.traverse_num == 1) {
    // lowest = m;
    // } else if (m.traverse_num == 0) {
    // continue;
    // }
    // lowest = m.isLowestTraversOne(lowest);
    // }
    // return true;
    // }

    void init_traverse() {
        traverse_num = 0;
        qMapping = -1;
        for (IndexNode m : children) {
            m.init_traverse();
        }
    }

    void addAdjLabels() {
        for (IndexNode m : children) {
            adjLabels.add(m.frag.getVlabel());
        }
        for (IndexNode m : children) {
            m.addAdjLabels();
        }
    }

    void addPath(List<CodeFragment> code, int graphIndex, boolean supergraphSearch, int G_size) {
        final int height = code.size();

        if (this.nodeID == 0) {
            nodeIDcount++;
            nodeID = nodeIDcount;
        }

        if (supergraphSearch) {
            ++count;
            supNode = true;
            if (height <= 0 && graphIndex != -1) {
                matchGraphIndices.add(graphIndex);
                return;
            }

        } else {
            if (graphIndex != -1)
                matchGraphIndicesBitSet.set(graphIndex, true);

            int dep = -1;
            for (IndexNode a = this; a != null; a = a.parent) {
                dep++;
            }
            depth = dep;
            if (height <= 0)
                return;
        }

        CodeFragment car = code.get(0);
        List<CodeFragment> cdr = code.subList(1, height);

        for (IndexNode m : children) {
            if (m.frag.equals(car)) {
                m.addPath(cdr, graphIndex, supergraphSearch, G_size);
                return;
            }
        }

        IndexNode m = new IndexNode(this, car, G_size);
        if (supergraphSearch)
            m.supNode = true;
        children.add(m);

        m.addPath(cdr, graphIndex, supergraphSearch, G_size);
    }

    List<Integer> search(Graph q, GraphCode impl) {
        HashSet<IndexNode> result0 = new HashSet<>();
        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch(q, this);
        for (Pair<IndexNode, SearchInfo> info : infoList) {
            if (info.left.supNode) {
                info.left.search(result0, q, info.right, impl);
            }
        }

        ArrayList<Integer> result = new ArrayList<>();
        for (IndexNode p : result0) {
            result.addAll(p.matchGraphIndices);

            final int c = p.matchGraphIndices.size();
            for (; p != null; p = p.parent) {
                p.count += c;
            }
        }

        Collections.sort(result);
        return result;
    }

    private void search(Set<IndexNode> result, Graph q, SearchInfo info, GraphCode impl) {
        final int c = matchGraphIndices.size();
        if (c > 0 && !result.contains(this)) {
            result.add(this);

            for (IndexNode p = this; p != null; p = p.parent) {// 最適化アルゴリズム発動
                p.count -= c;
            }
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, null);

        for (IndexNode m : children) {
            if (m.count > 0 && m.supNode) {
                for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                    if (frag.left.contains1(m.frag)) {
                        m.search(result, q, frag.right, impl);
                    }
                }
            }
        }
    }

    void find_once_node(int size, HashMap<Integer, HashSet<Integer>> filteringNodes) {
        // System.out.println(this.nodeID + ":" + traverse_num);
        if (depth > 1 && traverse_num == 1) {

            for (int i = 0; i < size; i++) {
                // if (i != 959)
                // continue;
                if ((traverseCount1.get(i) && !traverseCount2.get(i)) && Can.get(i)) {// possibel node filtering
                    HashSet<Integer> arrayList = filteringNodes.get(i);

                    if (arrayList == null) {
                        arrayList = new HashSet<>();
                    }

                    HashSet<Integer> noVF = new HashSet<>();
                    for (IndexNode m : children) {
                        if (m.traverse_num >= 1 && m.traverseCount1.get(i) && !m.traverseCount2.get(i)) {
                            noVF.add(m.mapping2.get(i));
                        }
                    }
                    for (IndexNode m : children) {
                        if (m.traverse_num == 0 && m.traverseCount1.get(i) && !m.traverseCount2.get(i)) {
                            if (m.mapping2.get(i) == null)
                                continue;
                            int target_node = m.mapping2.get(i);
                            if (noVF.contains(target_node))
                                continue;
                            // System.out.println("NF node:" + target_node);
                            // for (IndexNode a = m; a.parent != null; a = a.parent) {
                            // System.out.println(a.mapping2.get(i));
                            // }
                            arrayList.add(target_node);
                            // c++;
                        }
                    }
                    if (arrayList.size() == 0)
                        continue;
                    filteringNodes.put(i, arrayList);
                    // c++;
                }
            }

        }
        for (IndexNode m : children) {
            m.find_once_node(size, filteringNodes);
        }
    }

    private void find_once_node(int size, LinkedHashMap<Integer, List<Pair<Integer, Integer>>> filteringNode2) {
        if (traverse_num == 1) {
            for (int i = 0; i < size; i++) {
                // if (i != 23962)
                // continue;
                if ((traverseCount1.get(i) && !traverseCount2.get(i)) && Can.get(i)) {// possibel node filtering
                    List<Pair<Integer, Integer>> arrayList = filteringNode2.get(i);
                    int left = mapping2.get(i);

                    if (arrayList == null) {
                        arrayList = new ArrayList<>();
                    }

                    HashSet<Integer> noVF = new HashSet<>();
                    for (IndexNode m : children) {
                        if (m.traverse_num >= 1 && m.traverseCount1.get(i) && !m.traverseCount2.get(i)) {
                            noVF.add(m.mapping2.get(i));
                        }
                    }
                    for (IndexNode m : children) {

                        if (m.traverse_num == 0 && m.traverseCount1.get(i) && !m.traverseCount2.get(i)) {
                            if (m.mapping2.get(i) == null)
                                continue;

                            int target_node = m.mapping2.get(i);

                            if (noVF.contains(target_node))
                                continue;
                            // System.out.println("\nNF node:" + target_node);
                            // for (IndexNode a = m; a.parent != null; a = a.parent) {
                            // System.out.println(a.mapping2.get(i) + ":" + a.qMapping + "," + a.nodeID);
                            // }
                            arrayList.add(new Pair<Integer, Integer>(left, target_node));
                        }

                        if (arrayList.size() == 0)
                            continue;
                        filteringNode2.put(i, arrayList);
                    }
                }

            }
        }
        for (IndexNode m : children) {
            m.find_once_node(size, filteringNode2);
        }

    }

    // private void find_once_node(int size, LinkedHashMap<Integer,
    // List<Pair<Integer, Integer>>> filteringNode2) {
    // if(depth>1)return;
    // if (traverse_num == 1) {
    // for (int i = 0; i < size; i++) {
    // // if (i != 23962)
    // // continue;
    // if ((traverseCount1.get(i) && !traverseCount2.get(i)) && Can.get(i)) {//
    // possibel node filtering
    // List<Pair<Integer, Integer>> arrayList = filteringNode2.get(i);
    // int left = mapping2.get(i);

    // if (arrayList == null) {
    // arrayList = new ArrayList<>();
    // }

    // HashSet<Integer> noVF = new HashSet<>();
    // for (IndexNode m : children) {
    // if (m.traverse_num >= 1 && m.traverseCount1.get(i) &&
    // !m.traverseCount2.get(i)) {
    // noVF.add(m.mapping2.get(i));
    // }
    // }
    // for (IndexNode m : children) {

    // if (m.traverse_num == 0 && m.traverseCount1.get(i) &&
    // !m.traverseCount2.get(i)) {
    // if (m.mapping2.get(i) == null)
    // continue;

    // int target_node = m.mapping2.get(i);

    // if (noVF.contains(target_node))
    // continue;
    // // System.out.println("\nNF node:" + target_node);
    // // for (IndexNode a = m; a.parent != null; a = a.parent) {
    // // System.out.println(a.mapping2.get(i) + ":" + a.qMapping + "," + a.nodeID);
    // // }
    // arrayList.add(new Pair<Integer, Integer>(left, target_node));
    // }

    // if (arrayList.size() == 0)
    // continue;
    // filteringNode2.put(i, arrayList);
    // }
    // }

    // }
    // }
    // for (IndexNode m : children) {
    // m.find_once_node(size, filteringNode2);
    // }

    // }

    static int removeTotalGraphs = 0;

    BitSet subsearch(Graph q, GraphCode impl, int size, BufferedWriter bw, String mode, String dataset,
            BufferedWriter bw_data, BufferedWriter allbw, HashMap<Integer, ArrayList<String>> gMaps, List<Graph> G)
            throws IOException, InterruptedException {

        long start = System.nanoTime();

        init_Bitset(q, size);

        // if (q.id != 27)
        // return new BitSet(size);

        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch_sub(q, this);
        for (Pair<IndexNode, SearchInfo> info : infoList) {
            info.left.subsearch(q, info.right, impl);
        }
        long traverse_time = System.nanoTime();
        contain_search += traverse_time - start;

        for (Pair<IndexNode, SearchInfo> info : infoList) {
            info.left.subsearch_contain(q, info.right, impl);
        }

        induce_search += System.nanoTime() - traverse_time;

        infoList = null;

        a_fil_count = size - Can.cardinality();
        fil_count += a_fil_count;
        in_count += In.cardinality();

        result.or(In);
        // Can.or(Ex);
        Can.xor(In);

        doukeicount += Can.cardinality();
        a_filterTime = System.nanoTime() - start;
        search_time += a_filterTime;// filtering time

        // LinkedHashMap<Integer, List<Pair<Integer, Integer>>> filteringNode2 = new
        // LinkedHashMap<>();
        // find_once_node(size, filteringNode2);
        // int nf_count = 0;
        // for (List<Pair<Integer, Integer>> value : filteringNode2.values()) {
        // nf_count += value.size();
        // }
        // query_per_nf_count += nf_count;

        // write_file_for_Ver(gMaps, filteringNode2, G);

        // System.out.println("削除対象グラフ" + q.id + ":" + filteringNode2.keySet().size());
        // System.out.println("削除できた辺数" + q.id + ":" + removeTotalSize);

        LinkedHashMap<Integer, HashSet<Integer>> filteringNodes = new LinkedHashMap<>();
        int nf_count = 0;
        // System.out.println(q.id);
        find_once_node(size, filteringNodes);
        // System.out.println(filteringNodes.toString());
        for (Integer value : filteringNodes.keySet()) {
            nf_count += filteringNodes.get(value).size();
            // System.out.println(value + ":" + filteringNodes.get(value).size());
        }
        // System.out.print(nf_count + " ");
        query_per_nf_count += nf_count;

        write_file_for_Ver(gMaps, filteringNodes, G);

        verification_VEQ(dataset, mode, q, size);

        write_file_indiv(q, bw_data, size);

        init_traverse();

        if (q.id == 99) {
            System.out.println(nfg_count);
            System.out.println("\n候補グラフ数" + doukeicount);
            System.out.println("辺を削除できたグラフ数" + removeTotalGraphs);
            System.out.println("削除できた辺数" + removeTotalSize);
            write_file(allbw, bw, size);

            nfg_count = 0;
            removeTotalSize = 0;
            removeTotalGraphs = 0;
            contain_search = 0;
            induce_search = 0;
            init_param();
            // System.out.println(q.size + mode + ":" + (double) query_per_nf_count / 100);
            query_per_nf_count = 0;
        }
        return result;
    }

    static int removeTotalSize = 0;

    private void write_file_for_Ver(HashMap<Integer, ArrayList<String>> gMaps,
            LinkedHashMap<Integer, List<Pair<Integer, Integer>>> filteringNode2, List<Graph> G) {

        long time = System.nanoTime();
        try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {

            for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                    .nextSetBit(++trueIndex)) {
                // if (trueIndex != 23962)
                // continue;

                // can.add(trueIndex);
                // a graph be able to be possibl filtering node
                if (filteringNode2.get(trueIndex) != null && filteringNode2.get(trueIndex).size() > 0) {
                    // System.out.println(filteringNodes.get(trueIndex).size());
                    // if (trueIndex != 959)
                    // continue;
                    nfg_count++;
                    Graph g = G.get(trueIndex);
                    int newOrder = 0;
                    int newSize = 0;
                    int[] map = new int[g.order];
                    byte[][] newEdges = new byte[g.order][];
                    // System.arraycopy(g.edges, 0, newEdges, 0, g.order);
                    for (int i = 0; i < g.order; i++) {
                        newEdges[i] = g.edges[i].clone(); // オブジェクトがクローン可能な場合
                    }

                    for (Pair<Integer, Integer> p : filteringNode2.get(trueIndex)) {
                        newEdges[p.left][p.right] = 0;
                        newEdges[p.right][p.left] = 0;
                    }

                    for (int v = 0; v < g.order; ++v) {
                        boolean allZeros = true; // 一行が全て0かどうかを示すフラグ
                        // if (!filteringNodes.get(trueIndex).contains(v)) {
                        // map[newOrder++] = v;
                        // continue;
                        // }
                        for (int u = 0; u < g.order; ++u) {
                            if (newEdges[v][u] > 0) {
                                ++newSize;
                                allZeros = false; // 一行に0以外の要素がある場合、フラグをfalseに設定
                            }
                        }
                        if (!allZeros) {
                            map[newOrder++] = v;
                        }
                    }
                    newSize /= 2;

                    removeTotalSize += g.size - newSize;
                    if (g.size - newSize != 0)
                        removeTotalGraphs++;

                    if (newOrder == 0 || newSize == 0) {
                        continue;
                    }

                    can.add(trueIndex);

                    bw2.write("#" + g.id + "\n");
                    bw2.write(newOrder + "\n");
                    for (int i = 0; i < newOrder; i++) {
                        bw2.write(g.vertices[map[i]] + "\n");
                    }

                    bw2.write(newSize + "\n");
                    for (int i = 0; i < newOrder; i++) {
                        for (int j = i; j < newOrder; j++) {
                            if (newEdges[map[i]][map[j]] > 0) {
                                bw2.write(i + " " + j + "\n");
                                // bw2.write(i + " " + j + "\n");
                            }
                        }
                    }
                } else {
                    can.add(trueIndex);
                    for (String line : gMaps.get(trueIndex)) {
                        bw2.write(line + "\n");
                    }
                }
            }

            bw2.close();
        } catch (IOException e) {
            System.exit(1);
        }
        write_time += System.nanoTime() - time;

    }

    int nfg_count = 0;

    private void write_file_for_Ver(HashMap<Integer, ArrayList<String>> gMaps,
            HashMap<Integer, HashSet<Integer>> filteringNodes, List<Graph> G) {

        long time = System.nanoTime();
        try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {

            for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                    .nextSetBit(++trueIndex)) {
                // if (trueIndex != 959)
                // continue;

                // can.add(trueIndex);
                // a graph be able to be possibl filtering node
                if (filteringNodes.get(trueIndex) != null && filteringNodes.get(trueIndex).size() > 0) {
                    // System.out.println(filteringNodes.get(trueIndex).size());
                    // if (trueIndex != 76)
                    // continue;
                    nfg_count++;
                    Graph g = G.get(trueIndex);
                    int newOrder = 0;
                    int newSize = 0;
                    int[] map = new int[g.order];
                    byte[][] newEdges = new byte[g.order][];
                    // System.arraycopy(g.edges, 0, newEdges, 0, g.order);
                    for (int i = 0; i < g.order; i++) {
                        newEdges[i] = g.edges[i].clone(); // オブジェクトがクローン可能な場合
                    }

                    for (int v : filteringNodes.get(trueIndex)) {
                        for (int u : g.adjList[v]) {
                            newEdges[u][v] = 0;
                            newEdges[v][u] = 0;
                        }
                    }
                    for (int v = 0; v < g.order; ++v) {
                        boolean allZeros = true; // 一行が全て0かどうかを示すフラグ
                        // if (!filteringNodes.get(trueIndex).contains(v)) {
                        // map[newOrder++] = v;
                        // continue;
                        // }
                        for (int u = 0; u < g.order; ++u) {
                            if (newEdges[v][u] > 0) {
                                ++newSize;
                                allZeros = false; // 一行に0以外の要素がある場合、フラグをfalseに設定
                            }
                        }
                        if (!allZeros) {
                            map[newOrder++] = v;
                        }
                    }
                    newSize /= 2;

                    if (newOrder == 0 || newSize == 0) {
                        continue;
                    }

                    can.add(trueIndex);

                    bw2.write("#" + g.id + "\n");
                    bw2.write(newOrder + "\n");
                    for (int i = 0; i < newOrder; i++) {
                        bw2.write(g.vertices[map[i]] + "\n");
                    }

                    bw2.write(newSize + "\n");
                    for (int i = 0; i < newOrder; i++) {
                        for (int j = i; j < newOrder; j++) {
                            if (newEdges[map[i]][map[j]] > 0) {
                                bw2.write(i + " " + j + "\n");
                                // bw2.write(i + " " + j + "\n");
                            }
                        }
                    }
                } else {
                    can.add(trueIndex);
                    for (String line : gMaps.get(trueIndex)) {
                        bw2.write(line + "\n");
                    }
                }
            }

            bw2.close();
        } catch (IOException e) {
            System.exit(1);
        }
        write_time += System.nanoTime() - time;
    }

    private void subsearch_contain(Graph q, SearchInfo info, GraphCode impl) {
        traverse_num++;

        // if (info.getVertexIDs()[info.getVertexIDs().length - 1] == 3 &&
        // mapping2.get(23962) != null
        // && mapping2.get(23962) == 10) {
        // int a = 0;
        // }
        // if (nodeID == 194) {
        // int a = 0;
        // }
        // if (info.getVertexIDs()[info.getVertexIDs().length - 1] == 3 &&
        // mapping2.get(23962) != null
        // && mapping2.get(23962) == 10) {
        // System.out.println(nodeID);
        // }

        if (traverse_num == 1) {
            int[] vertexIDs = info.getVertexIDs();
            qMapping = vertexIDs[vertexIDs.length - 1];
        } else if (traverse_num > 1) {
            qMapping = -2;
        }

        if (children.size() == 0) {
            return;
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (frag.left.contains(m.frag)) {
                    m.subsearch_contain(q, frag.right, impl);
                }
            }
        }
    }

    private void subsearch(Graph q, SearchInfo info, GraphCode impl) {
        // Ex.and(matchGraphIndicesBitSet);
        Can.and(matchGraphIndicesBitSet);

        // traverse_num++;

        // if (traverse_num == 1) {
        // int[] vertexIDs = info.getVertexIDs();
        // qMapping = vertexIDs[vertexIDs.length - 1];
        // if (vertexIDs[0] == 5 && vertexIDs[vertexIDs.length - 1] == 2) {
        // int a = 0;
        // }
        // } else if (traverse_num > 1) {
        // qMapping = -1;
        // }

        if (depth == q.order) {
            In.or(matchGraphIndicesBitSet);
            return;
        }

        if (children.size() == 0) {
            return;
        }

        // if (!In.isEmpty()) {
        // BitSet G = (BitSet) U.clone();
        // G.xor(In);
        // G.and(Ex); // G-In-Ex
        // G.and(matchGraphIndicesBitSet);

        // if (G.isEmpty()) { // if U and ID(n) = null then backtrack
        // // backtrackCount++;
        // return;
        // }
        // }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (m.frag.equals(frag.left)) {
                    m.subsearch(q, frag.right, impl);
                }
            }
        }
    }

    void addIDtoTree(Graph g, GraphCode impl, int id) {
        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch_sub(g, this);
        for (Pair<IndexNode, SearchInfo> info : infoList) {
            info.left.addIDtoTree(g, info.right, impl, id, true);
        }
    }

    private void addIDtoTree(Graph g, SearchInfo info, GraphCode impl, int id, boolean fragIsEqual) {
        matchGraphIndicesBitSet.set(id, true);

        // 子に2 回以上訪問した場合は，2 個以上のコブを除去可能の実装ができていない
        // 親節点nには１回の訪問、そのある1つの子mにはa(>1)回到達＝mにてa回のNFが可能
        // その実装をしよう！
        // 親の到達回数が１回
        // if (parent.traverseCount1.get(id) && !parent.traverseCount2.get(id)) {
        // traverseCount1.set(id);
        // int[] vertexIDs = info.getVertexIDs();
        // mapping のvalueをListにする必要がある
        // mapping2.put(id, vertexIDs[vertexIDs.length - 1]);

        // //親が２回以上探索されている場合、現在の節点のmappingを消す
        // }else if (parent.traverseCount1.get(id) && parent.traverseCount2.get(id)) {
        // mapping2.remove(id);
        // }

        // if (nodeID == 2475 && id == 23962) {
        // int a = 0;
        // }

        if (!traverseCount1.get(id) && !traverseCount2.get(id)) {// 0 times
            traverseCount1.set(id, true);
            if (fragIsEqual) {
                int[] vertexIDs = info.getVertexIDs();
                mapping2.put(id, vertexIDs[vertexIDs.length - 1]);
            }
        } else if (traverseCount1.get(id) && !traverseCount2.get(id)) {// onec
            traverseCount2.set(id, true);
            mapping2.remove(id);
            // 親が２回以上探索されたため、子(or子孫)の節点のmappingを消す
            // removeDescendantNodeMapping(id);
        }

        // if (id == 23962 && depth == 4 &&
        // info.getVertexIDs()[info.getVertexIDs().length - 1] == 8
        // && info.getVertexIDs()[info.getVertexIDs().length - 2] == 10
        // && info.getVertexIDs()[info.getVertexIDs().length - 3] == 22) {
        // int a = 0;
        // }

        if (children.size() == 0) {
            return;
        }

        // if (backtrackJudge(g, id)) {
        // return;
        // }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(g, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (frag.left.contains(m.frag)) {
                    fragIsEqual = frag.left.equals(m.frag);
                    m.addIDtoTree(g, frag.right, impl, g.id, fragIsEqual);
                }
            }
        }
    }

    private void removeDescendantNodeMapping(int id) {
        if (mapping2.get(id) == null)
            return;
        traverseCount2.set(id, true);
        mapping2.remove(id);
        for (IndexNode m : children) {
            m.removeDescendantNodeMapping(id);
        }
    }

    private boolean backtrackJudge(Graph g, int id) {
        for (IndexNode m : children) {
            if (!m.matchGraphIndicesBitSet.get(id)) {
                return false;
            }
            if (m.depth <= g.order) {
                if (!m.backtrackJudge(g, id))
                    return false;
            }
        }
        return true;
    }

    void getLeafGraph(List<Graph> leafGraphs) {
        for (IndexNode m : children) {
            if (m.children.size() == 0) {
                List<CodeFragment> code = m.getCode();
                Graph g = generateGraph(code, m.nodeID);
                leafGraphs.add(g);
            } else {
                m.getLeafGraph(leafGraphs);
            }
        }
    }

    Graph generateGraph(List<CodeFragment> code, int id) {
        byte[] vertices = new byte[code.size()];
        byte[][] edges = new byte[code.size()][code.size()];
        int index = 0;
        for (CodeFragment c : code) {
            vertices[index] = c.getVlabel();
            byte eLabels[] = c.getelabel();
            for (int i = 0; i < eLabels.length; i++) {
                if (eLabels[i] == 1) {
                    edges[index][i] = 1;
                    edges[i][index] = 1;
                }
            }
            index++;
        }
        return new Graph(id, vertices, edges);
    }

    List<CodeFragment> getCode() {
        List<CodeFragment> code = new ArrayList<>();
        for (IndexNode n = this; n != null; n = n.parent) {
            code.add(n.frag);
        }
        Collections.reverse(code);
        code.remove(0);
        return code;
    }

    void removeTree() {
        if (removeNode.size() == 0)
            return;
        ArrayList<IndexNode> target = new ArrayList<>();
        for (IndexNode m : children) {
            target.add(m);
        }
        for (IndexNode m : target) {
            if (removeNode.contains(m)) {
                m.parent.children.remove(m.parent.children.indexOf(m));
                for (IndexNode p = m.parent; p.children.size() < 1; p = p.parent) {
                    p.parent.children.remove(p.parent.children.indexOf(p));
                }
                removeNode.remove(removeNode.indexOf(m));
            }
            m.removeTree();
        }
    }

    void pruningEquivalentNodes(Graph g, GraphCode impl, int leafID, ArrayList<Integer> idList,
            ArrayList<Integer> removeIDList) {
        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch(g, this);
        for (Pair<IndexNode, SearchInfo> info : infoList) {
            info.left.pruningEquivalentNodes(g, info.right, impl, leafID, idList, removeIDList);
        }
    }

    private void pruningEquivalentNodes(Graph g, SearchInfo info, GraphCode impl, int leafID, ArrayList<Integer> idList,
            ArrayList<Integer> removeIDList) {

        // this node is leaf and & not needed by index because of same path
        if (children.size() == 0 && leafID != this.nodeID && !removeNode.contains(this)
                && !idList.contains(this.nodeID)) {
            removeIDList.add(this.nodeID);
            removeNode.add(this);
            return;
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(g, info);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (frag.left.equals(m.frag)) {
                    m.pruningEquivalentNodes(g, frag.right, impl, nodeID, idList, removeIDList);
                }
            }
        }
    }

    private void init_Bitset(Graph q, int size) {
        In.clear();
        Can.clear();
        result.clear();
        can.clear();
        if (q.id == 0) {
            U.clear();
            for (int j = 0; j < size; j++)
                U.flip(j);
        }
        // Ex = (BitSet) U.clone();
        Can = (BitSet) U.clone();
    }

    private void verification_VEQ(String dataset, String mode, Graph q, int size) throws InterruptedException {
        long start = System.nanoTime();

        try {
            String command = String.format("./VEQ_S -dg datagraph.gfu -qg Query/%s/%s/%d/q%d.gfu -o output2.txt",
                    dataset, mode, q.size, q.id);
            // String command = String.format("./VEQ_S -dg datagraph.gfu -qg
            // Query/%s/%s/%d/q%d.gfu",
            // dataset, mode, q.size, q.id);
            p = runtime.exec(command);
            boolean timelimit = p.waitFor(10, TimeUnit.MINUTES);
            if (!timelimit) {
                verification_time += 600000;// 10m
                query_per_time = 600000;
                query_per_veq = 0;
                veq_per_Can = 0;
                fileter_time = 0;// veq
                verify_time = 0;// veq
                a_filterTime = 0;
                FPper_q = 0;
                FPre = 0;
                filpertime = 0;
                SPper_q = 0;
            } else {
                nonfail++;
                InputStream inputStream = p.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println(line);
                    if (line.charAt(0) == 'F') {

                        String time = line.substring(21);
                        fileter_time = Double.parseDouble(time);
                        query_per_veqF += Double.parseDouble(time);
                    } else if (line.charAt(0) == 'V') {
                        String time = line.substring(24);
                        verify_time = Double.parseDouble(time);
                        query_per_veqV += Double.parseDouble(time);
                    } else if (line.startsWith("Number of C")) {
                        String answer = line.substring(28);
                        veq_per_Can = Double.parseDouble(answer);
                    }
                    // else if (line.startsWith("Number of A")) {
                    // String answer = line.substring(28);
                    // veq_per_Can = Double.parseDouble(answer);
                    // }
                }
                query_per_veq = fileter_time + verify_time;
                CT_verify += query_per_veq;
                query_per_time = query_per_veq + (double) a_filterTime / 1000 / 1000;
                verification_time += query_per_veq;
                query_per_sum += query_per_veq;

                long start_read = System.nanoTime();
                readAnswer(path, can, result);
                read_time += System.nanoTime() - start_read;

                SPper_q = (double) In.cardinality() / result.cardinality();

                if (Can.cardinality() != 0) {
                    FPper_q = (double) (Can.cardinality() - result.cardinality()) /
                            (Can.cardinality());
                }

                if (size != result.cardinality()) {
                    // FPre = (double) (size - Ex.cardinality())
                    // / (size - result.cardinality());
                    FPre = (double) (size - Can.cardinality() - result.cardinality())
                            / (size - result.cardinality());
                } else {
                    FPre = 1;
                }

                if (size - veq_per_Can != 0) {
                    filpertime = (size - veq_per_Can) / (((double) a_filterTime / 1000 / 1000) +
                            fileter_time);
                }

            }
            p.destroyForcibly();
        } catch (IOException ex) {
            System.out.println(ex);
            System.exit(0);
        }
        process_time += System.nanoTime() - start;
        totoal_kai += result.cardinality();
        veq_Can_total += veq_per_Can;
        FP += FPre;
        FP2 += FPper_q;
        SP += SPper_q;
    }

    private void readAnswer(Path path, ArrayList<Integer> can, BitSet result) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            int id;
            String line;
            while ((line = br.readLine()) != null) {
                id = Integer.parseInt(line);
                result.set(can.get(id));
            }
            br.close();
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private void write_file_indiv(Graph q, BufferedWriter bw_data, int size) throws IOException {
        if (q.id == 0) {
            bw_data.write(
                    "query_id,FP_ratio,(G-C)/(G-A),SP,filtering_time(ms),filtering_time(ms)(VEQ),verification_time(ms)(VEQ),VEQs_time(ms),query_time(ms),filtering_num,inclusion_num,Candidate_num,VEQs_Candidate_num,VEQs_filtering_num,answer_num,|G|,filter_time/filter_num\n");
        }
        bw_data.write(q.id + "," + FPper_q + "," + FPre + ","
                + SPper_q + ","
                + String.format("%.8f", (double) a_filterTime / 1000 / 1000) + ","
                + String.format("%.8f", fileter_time) + "," +
                String.format("%.8f", verify_time) + "," + String.format("%.8f",
                        query_per_veq)
                +
                "," + String.format("%.8f", query_per_time) + ","
                + a_fil_count + "," + In.cardinality() + "," +
                Can.cardinality() + "," + veq_per_Can + ","
                + (Can.cardinality() - veq_per_Can) + ","
                + result.cardinality() + "," + size + ","
                + String.format("%.6f", filpertime)
                + "\n");
    }

    private void write_file(BufferedWriter allbw, BufferedWriter bw, int size) {
        try {
            allbw.write(String.format("%.5f", FP2 / nonfail) + "," +
                    String.format("%.5f", FP / nonfail) + ","
                    + String.format("%.5f", SP / nonfail) + ","
                    + String.format("%.6f", (double) search_time / 1000 / 1000 / nonfail) + ","
                    + String.format("%.6f", CT_verify / nonfail) + ","
                    + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                            verification_time) / 100)
                    + ","
                    + String.format("%.6f", (((double) search_time / 1000 / 1000) / (size *
                            nonfail - doukeicount)))
                    + "," + (size * nonfail - doukeicount) + ","
                    + String.format("%.6f",
                            (size * nonfail - veq_Can_total)
                                    / (((double) search_time / 1000 / 1000) + query_per_veqF))
                    + "," + (size * nonfail - veq_Can_total) + "," + nonfail
                    + "\n");

            bw.write("(C)書き込み関数時間(ms): " + String.format("%.2f", (double) write_time /
                    1000 / 1000) + "\n");
            bw.write("(D)プロセス関数時間(ms): " + String.format("%.2f", (double) process_time /
                    1000 / 1000) + "\n");
            bw.write("(E)読み込み関数時間(ms): " + String.format("%.2f", (double) read_time /
                    1000 / 1000) + "\n");

            bw.write("query_contain_travers_time(ms): " + String.format("%.5f", (double) contain_search / 1000 / 1000)
                    + "\n");
            bw.write("query_equal_travers_time(ms): " + String.format("%.5f", (double) induce_search / 1000 / 1000)
                    + "\n");

            bw.write("Number of Filtering Graphs: " + fil_count + "\n");
            bw.write("Number of inclusion Graphs: " + in_count + "\n");

            bw.write("Number of Candidate Graphs: " + doukeicount + "\n");
            bw.write("Number of Answer Graphs: " + totoal_kai + "\n");
            bw.write("filtering Presison : " + String.format("%.5f", FP / nonfail) +
                    "\n");
            bw.write("FP ratio : " + String.format("%.5f", FP2 / nonfail) + "\n");
            bw.write("inclusion Presison : " + String.format("%.5f", SP) + "%" + "\n");

            // bw.write(
            // "filter_time/filter_num : "
            // + String.format("%.6f", (((double) search_time / 1000 / 1000) +
            // query_per_veqF)
            // / (size * nonfail - veq_Can_total))
            // + "\n");

            bw.write("(a)a VEQs Filtering Time (ms): " + String.format("%.6f",
                    query_per_veqF / nonfail) + "\n");
            bw.write("(b)a VEQs Verification Time (ms): " + String.format("%.6f",
                    query_per_veqV / nonfail) + "\n");

            bw.write("(A)a Filtering Time (ms): "
                    + String.format("%.6f", (double) search_time / 1000 / 1000 / nonfail)
                    + "\n");
            bw.write("(B=a+b)a Verification Time (ms): " + String.format("%.6f",
                    query_per_sum / nonfail)
                    + "\n");
            bw.write("(A)+(B)a Processing Time (ms): "
                    + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                            verification_time) / 100)
                    + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init_param() {
        nonfail = 0;
        CT_verify = 0;
        doukeicount = 0;
        verification_time = 0;
        process_time = 0;
        write_time = 0;
        read_time = 0;
        totoal_kai = 0;
        search_time = 0;
        query_per_veqF = 0;
        query_per_veqV = 0;
        query_per_sum = 0;
        veq_Can_total = 0;
        FP = 0;
        FP2 = 0;
        SP = 0;
        fil_count = 0;
        in_count = 0;
    }
}

// / System.out.println("aboutfirst" + (double) nonfirstnode_num /
// traverse_num);
// // System.out.println("aboutnonfirst" + (double) firstnode_num /
// traverse_num);

// // about_traverse += (double) nonfirstnode_num / traverse_num;
// // about_nontraverse += (double) firstnode_num / traverse_num;
// //
// if (CodeTree.datasetSize - 1 == g.id) {
// // System.out.println(CodeTree.datasetSize);
// // System.out.println(nonfirstnode_num);
// // System.out.println(firstnode_num);
// // System.out.println(traverse_num);
// // System.out.println(about_traverse / CodeTree.datasetSize);
// // System.out.println(about_nontraverse / CodeTree.datasetSize);
// System.out.println(backtrackCount);
// traverse_num = 0;
// firstnode_num = 0;
// nonfirstnode_num = 0;
// about_traverse = 0;
// about_nontraverse = 0;
// }

// private void write_file_for_Ver2(HashMap<Integer, ArrayList<String>> gMaps,
// HashMap<Integer, HashSet<Integer>> filteringNodes, List<Graph> G) {

// long time = System.nanoTime();

// for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
// .nextSetBit(++trueIndex)) {

// can.add(trueIndex);
// // a graph be able to be possibl filtering node
// if (filteringNodes.get(trueIndex) != null &&
// filteringNodes.get(trueIndex).size() > 0) {
// try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {

// Graph g = G.get(trueIndex);
// HashSet<Integer> set = filteringNodes.get(trueIndex);
// int newOrder = 0;
// int newSize = g.size;
// int[] map = new int[g.order];
// byte[][] newEdges = new byte[g.order][];
// System.arraycopy(g.edges, 0, newEdges, 0, g.order);

// for (int i = 0; i < g.order; i++) {
// if (!filteringNodes.get(trueIndex).contains(i)) {
// map[newOrder++] = i;
// } else {
// for (int j : g.adjList[i]) {
// // if(g.edges[i][j])
// newEdges[i][j] = 0;
// newEdges[j][i] = 0;

// }
// }
// }

// newSize = 0;
// newOrder = 0;

// for (int v = 0; v < g.order; ++v) {
// boolean allZeros = true; // 一行が全て0かどうかを示すフラグ
// for (int u = v; u < g.order; ++u) {
// if (newEdges[v][u] > 0) {
// ++newSize;
// allZeros = false; // 一行に0以外の要素がある場合、フラグをfalseに設定
// }
// }

// // 一行が全て0の場合、mapに値を設定
// if (allZeros) {
// map[newOrder++] = v;
// }
// }

// // if (newOrder == 0 || newSize == 0) {
// // // System.out.println("a");
// // continue;

// // }
// // can.add(trueIndex);

// bw2.write("#" + g.id + "\n");
// bw2.write(newOrder + "\n");
// for (int i = 0; i < newOrder; i++) {
// bw2.write(g.vertices[map[i]] + "\n");
// }

// bw2.write(newSize + "\n");
// for (int i = 0; i < newOrder; i++) {
// for (int j = i; j < newOrder; j++) {
// if (g.edges[map[i]][map[j]] > 0) {
// bw2.write(map[i] + " " + map[j] + "\n");
// // bw2.write(i + " " + j + "\n");
// }
// }
// }

// bw2.close();

// String command = String.format(
// "./VEQ_S -dg datagraph.gfu -qg Query/IMDB-MULTI/randomwalk/4/q0.gfu -o
// output2.txt");
// p = runtime.exec(command);
// InputStream inputStream = p.getInputStream();
// BufferedReader reader = new BufferedReader(new
// InputStreamReader(inputStream));
// String line;
// while ((line = reader.readLine()) != null) {
// System.out.println(line);
// }
// } catch (IOException e) {
// System.exit(1);
// }

// } else {
// // // can.add(trueIndex);
// // for (String line : gMaps.get(trueIndex)) {
// // bw2.write(line + "\n");
// // }
// }

// }
// write_time += System.nanoTime() - time;
// }