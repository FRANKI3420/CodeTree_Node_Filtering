package codetree.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    protected int traverse_num = 0;
    protected LinkedHashMap<Integer, BitSet> labelFiltering;
    protected BitSet childEdgeFrag;

    static BitSet In = new BitSet();
    static BitSet Can = new BitSet();
    static BitSet result = new BitSet();

    static IndexNode root;
    // static BitSet U = new BitSet();

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
    static long a_filterTime = 0;

    static long contains_search = 0;
    static long equals_search = 0;
    static int backtrack_count = 0;
    static long edgeFiltering_time = 0;
    static int filtering_edge_num = 0;
    static long nodeFiltering_time = 0;
    static long a_nodeFiltering_time = 0;
    static int c = 0;
    static int query_per_nf_count = 0;
    static int removeTotalSize = 0;
    static int removeTotalGraphs = 0;
    static int deletedVsumPerq = 0;
    static int labelFilteringGraph = 0;
    static int lab_fil_num = 0;
    static int verfyNum = 0;
    static boolean traverse;

    static ArrayList<IndexNode> removeNode = new ArrayList<>();

    IndexNode(IndexNode parent, CodeFragment frag) {
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
        labelFiltering = new LinkedHashMap<>();
        childEdgeFrag = new BitSet();
    }

    int size() {
        int s = 1;
        for (IndexNode m : children) {
            s += m.size();
        }
        return s;
    }

    void init_traverse() {
        if (traverse_num == 0 && depth > 0)
            return;

        traverse_num = 0;
        for (IndexNode m : children) {
            m.init_traverse();
        }
    }

    void init_removeNode() {
        removeNode = new ArrayList<>();
    }

    void addInfo() {
        for (IndexNode m : children) {
            adjLabels.add(m.frag.getVlabel());
            for (int v = 0; v < depth; v++) {
                if (m.frag.getelabel()[v] > 0)
                    childEdgeFrag.set(v);
            }
        }
        for (IndexNode m : children) {
            m.addInfo();
        }
    }

    void addPath(List<CodeFragment> code, int graphIndex, boolean supergraphSearch) {
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
                m.addPath(cdr, graphIndex, supergraphSearch);
                return;
            }
        }

        IndexNode m = new IndexNode(this, car);
        if (supergraphSearch)
            m.supNode = true;
        children.add(m);

        m.addPath(cdr, graphIndex, supergraphSearch);
    }

    void find_labelFiltering(List<Graph> G, int qOrder) {

        for (IndexNode m : children) {
            if (m.traverse_num == 0) {
                int mapsize = m.labelFiltering.size();
                int cansize = Can.cardinality();
                if (cansize <= mapsize) {
                    for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                            .nextSetBit(++trueIndex)) {
                        BitSet labelFrag = m.labelFiltering.get(trueIndex);
                        if (labelFrag != null) {
                            Graph g = G.get(trueIndex);
                            g.filterFlag.or(labelFrag);
                            deletedVsumPerq += labelFrag.cardinality();
                            query_per_nf_count += labelFrag.cardinality();
                            if (g.order - g.filterFlag.cardinality() < qOrder) {
                                Can.set(trueIndex, false);
                                g.filterFlag.clear();
                                labelFilteringGraph++;
                            }
                        }
                    }
                } else {
                    for (int trueIndex : m.labelFiltering.keySet()) {
                        if (Can.get(trueIndex)) {
                            BitSet labelFrag = m.labelFiltering.get(trueIndex);

                            Graph g = G.get(trueIndex);
                            deletedVsumPerq += labelFrag.cardinality();
                            query_per_nf_count += labelFrag.cardinality();
                            if (g.order - g.filterFlag.cardinality() < qOrder) {
                                Can.set(trueIndex, false);
                                g.filterFlag.clear();
                                labelFilteringGraph++;
                            }
                        }
                    }
                }

            }
        }
    }

    static int labelNumFiltering = 0;
    static int a_labelNumFiltering = 0;

    static int labelFiltering_time = 0;

    BitSet subsearch(Graph q, GraphCode impl, int size, BufferedWriter bw, String mode, String dataset,
            BufferedWriter bw_data, BufferedWriter allbw, List<Graph> G,
            String directory, int qsize, HashMap<Integer, ArrayList<String>> gMaps, int delta)
            throws IOException, InterruptedException {

        // if (q.id == 0)
        // System.out.println("\n辿った節点数" + traverse_cou);

        long start = System.nanoTime();

        // HashMap<Byte, Integer> qLabelMap = q.getLabelMap();
        // System.out.println(qLabelMap.toString());

        init_Bitset();
        root = this;
        Can.or(matchGraphIndicesBitSet);
        traverse = true;

        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch(q, this);
        if (delta <= q.order) {
            for (Pair<IndexNode, SearchInfo> info : infoList) {
                info.left.subsearch2(q, info.right, impl);
                info.left.supersearch(q, info.right, impl);
                if (!traverse)
                    break;
            }
        } else {
            for (Pair<IndexNode, SearchInfo> info : infoList) {
                info.left.subsearch(q, info.right, impl);
            }
        }
        result.or(In);
        infoList = null;

        if (!traverse) {
            Can.clear();
        }

        // System.out.println(Can.cardinality());

        contains_search += System.nanoTime() - start;

        if (!Can.isEmpty()) {
            verfyNum++;

            a_fil_count = size - Can.cardinality();
            fil_count += a_fil_count;
            in_count += In.cardinality();

            // doukeicount += Can.cardinality();
            a_filterTime = System.nanoTime() - start;
            search_time += a_filterTime;// filtering time

            start = System.nanoTime();

            find_labelFiltering(G, q.order);

            a_nodeFiltering_time = System.nanoTime() - start;

            nodeFiltering_time += a_nodeFiltering_time;

            // start = System.nanoTime();

            // find_labelFiltering(G, q.id, qLabelMap);

            // labelFiltering_time += System.nanoTime() - start;
            // System.out.println(Can.cardinality());

            // lab_fil_num += labelFilteringGraph;

            doukeicount += Can.cardinality();

            // start = System.nanoTime();

            // edgeFiltering_time += System.nanoTime() - start;

            write_file_for_Ver(G, q, gMaps);

            verification_VEQ(directory, dataset, mode, q, qsize);
        } else {
            a_fil_count = size - Can.cardinality();
            fil_count += a_fil_count;
            in_count += In.cardinality();
            doukeicount += Can.cardinality();
            a_filterTime = System.nanoTime() - start;
            search_time += a_filterTime;// filtering time
            nonfail++;
            SPper_q = (double) In.cardinality() / result.cardinality();
            FPre = 1;
            FPper_q = 0;
            totoal_kai += result.cardinality();
            FP += FPre;
            FP2 += FPper_q;
            SP += SPper_q;
            query_per_time = (double) a_filterTime / 1000 / 1000;
        }

        init_traverse();

        write_file_indiv(q, bw_data, size);

        if (q.id == 99) {
            // System.out.println("\n候補グラフ数" + doukeicount);
            // System.out.println("何かを削除できたグラフ数" + removeTotalGraphs);
            System.out.println("削除できた頂点数/|Can(Q)|:" + (double) query_per_nf_count / (doukeicount + lab_fil_num));
            // System.out.println("削除できた総辺数:" + removeTotalSize);
            System.out.println("label filteirng graph num:" + ":" + (lab_fil_num));
            System.out.println("label Num filteirng graph num:" + ":" + (labelNumFiltering));
            System.out.println("query time:" + ":" + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                    verification_time + (double) (edgeFiltering_time + nodeFiltering_time) / 1000 / 1000) / 100));

            write_file(allbw, bw, size);
            init_param();
            removeTotalGraphs = 0;
            edgeFiltering_time = 0;
            filtering_edge_num = 0;
            removeTotalSize = 0;
            lab_fil_num = 0;
            labelNumFiltering = 0;
            verfyNum = 0;
            labelFiltering_time = 0;
        }
        return result;
    }

    private void write_file_for_Ver(List<Graph> D, Graph q, HashMap<Integer, ArrayList<String>> gMaps) {
        long time = System.nanoTime();
        try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {

            for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                    .nextSetBit(++trueIndex)) {

                Graph g = D.get(trueIndex);

                if (g.filterFlag.cardinality() >= 0) {

                    int newSize = 0;
                    int newOrder = 0;
                    int[] map = new int[g.order];

                    for (int v = g.filterFlag.nextClearBit(0); v < g.order; v = g.filterFlag.nextClearBit(++v)) {
                        boolean allZeros = true; // 一行が全て0かどうかを示すフラグ
                        int[] adj = g.adjList[v];
                        for (int u : adj) {
                            if (g.filterFlag.get(u))
                                continue;
                            ++newSize;
                            allZeros = false; // 一行に0以外の要素がある場合、フラグをfalseに設定
                        }
                        if (!allZeros) {
                            map[newOrder++] = v;
                        }
                    }
                    newSize /= 2;
                    g.filterFlag = new BitSet();

                    removeTotalSize += g.size - newSize;
                    if (g.size - newSize != 0)
                        removeTotalGraphs++;

                    if (newOrder == 0 || newSize == 0) {
                        continue;
                    }

                    if (q.order > newOrder || q.size > newSize) {
                        continue;
                    }
                    can.add(trueIndex);

                    String line = "#" + g.id + "\n";
                    bw2.write(line);
                    line = newOrder + "\n";
                    bw2.write(line);
                    for (int i = 0; i < newOrder; i++) {
                        bw2.write(g.vertices[map[i]] + "\n");
                    }

                    bw2.write(newSize + "\n");

                    for (int i = 0; i < newOrder; i++) {
                        for (int j = i; j < newOrder; j++) {
                            if (g.edges[map[i]][map[j]] > 0) {
                                bw2.write(i + " " + j + "\n");
                            }
                        }
                    }

                } else {
                    can.add(trueIndex);
                    for (String line : gMaps.get(trueIndex)) {
                        bw2.write(line + "\n");
                    }
                    // can.add(trueIndex);
                    // String line = "#" + g.id + "\n";
                    // bw2.write(line);
                    // line = g.order + "\n";
                    // bw2.write(line);
                    // for (int i = 0; i < g.order; i++) {

                    // bw2.write(g.vertices[i] + "\n");
                    // }

                    // bw2.write(g.size + "\n");

                    // for (int i = 0; i < g.size; i++) {
                    // for (int j = i; j < g.size; j++) {
                    // if (g.edges[i][j] > 0) {
                    // bw2.write(i + " " + j + "\n");
                    // }
                    // }
                    // }
                }

            }
            bw2.close();
        } catch (IOException e) {
            System.exit(1);
        }
        write_time += System.nanoTime() - time;
    }

    private void write_file_for_Ver(HashMap<Integer, ArrayList<String>> gMaps) {
        long time = System.nanoTime();
        try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {

            for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                    .nextSetBit(++trueIndex)) {

                can.add(trueIndex);
                for (String line : gMaps.get(trueIndex)) {
                    bw2.write(line + "\n");
                }
            }

            bw2.close();
        } catch (IOException e) {
            System.exit(1);
        }
        write_time += System.nanoTime() - time;
    }

    private void subsearch2(Graph q, SearchInfo info, GraphCode impl) {
        Can.and(matchGraphIndicesBitSet);
        traverse_num++;

        if (children.size() == 0) {
            return;
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (frag.left.contains(m.frag)) {
                    m.subsearch(q, frag.right, impl);
                }
            }
        }
    }

    private void subsearch(Graph q, SearchInfo info, GraphCode impl) {
        Can.and(matchGraphIndicesBitSet);
        traverse_num++;
        if (children.size() == 0) {
            return;
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                // if (m.frag.equals(frag.left)) {
                if (frag.left.contains(m.frag)) {
                    m.subsearch(q, frag.right, impl);
                    if (!traverse)
                        return;
                }
            }
        }
    }

    private void supersearch(Graph q, SearchInfo info, GraphCode impl) {
        if (depth == q.order) {
            In.or(matchGraphIndicesBitSet);
            traverse = false;
            return;
        }

        if (children.size() == 0) {
            return;
        }

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(q, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                // if (frag.left.bigger(m.frag)) {
                if (frag.left.contains(m.frag)) {
                    m.supersearch(q, frag.right, impl);
                    if (!traverse)
                        return;
                }
            }
        }
    }

    BitSet G = new BitSet();

    static int traverse_cou = 0;

    void addIDtoTree(Graph g, GraphCode impl, int id, BitSet gLabels) {
        matchGraphIndicesBitSet.set(id, true);
        List<Pair<IndexNode, SearchInfo>> infoList = impl.beginSearch(g, this);
        for (Pair<IndexNode, SearchInfo> info : infoList) {
            info.left.addIDtoTree(g, info.right, impl, id, gLabels);
        }
    }

    private void addIDtoTree(Graph g, SearchInfo info, GraphCode impl, int id, BitSet gLabels) {
        traverse_cou++;
        matchGraphIndicesBitSet.set(id, true);

        if (depth == 1) {
            if (labelFiltering.get(id) == null) {
                labelFiltering.put(id, new BitSet(g.order));
            }
            labelFiltering.get(id).set(info.getVertexIDs()[0]);
        }

        if (children.size() == 0) {
            return;
        }

        if (backtrackJudge(g, id)) {
            return;
        }

        // if (getNoTraversNode(g, id, gLabels))
        // return;

        List<Pair<CodeFragment, SearchInfo>> nextFrags = impl.enumerateFollowableFragments(g, info, adjLabels,
                childEdgeFrag);
        // List<Pair<CodeFragment, SearchInfo>> nextFrags =
        // impl.enumerateFollowableFragments(g, info, adjLabels);

        for (IndexNode m : children) {
            for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
                if (frag.left.contains(m.frag)) {
                    m.addIDtoTree(g, frag.right, impl, g.id, gLabels);
                }
            }
        }
    }

    private boolean getNoTraversNode(Graph g, int id, BitSet glabels) {
        for (IndexNode m : children) {
            if (!m.matchGraphIndicesBitSet.get(id)) {
                if (glabels.get(m.frag.getVlabel())) {
                    return false;// no backtrack
                }
            }
            if (m.depth <= g.order) {
                if (!m.getNoTraversNode(g, id, glabels))
                    return false;
            }
        }
        return true;
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

    List<CodeFragment> getCode() {
        List<CodeFragment> code = new ArrayList<>();
        for (IndexNode n = this; n != null; n = n.parent) {
            code.add(n.frag);
        }
        Collections.reverse(code);
        code.remove(0);
        return code;
    }

    Graph generateGraph(List<CodeFragment> code, int id) {
        byte[] vertices = new byte[code.size()];
        byte[][] edges = new byte[code.size()][code.size()];
        int index = 0;
        for (CodeFragment c : code) {
            vertices[index] = c.getVlabel();
            byte eLabels[] = c.getelabel();
            if (eLabels == null) {
                if (index < code.size() - 1) {
                    edges[index][index + 1] = 1;
                    edges[index + 1][index] = 1;
                }
            } else {

                for (int i = 0; i < eLabels.length; i++) {
                    if (eLabels[i] == 1) {
                        edges[index][i] = 1;
                        edges[i][index] = 1;
                    }
                }
            }
            index++;
        }
        return new Graph(id, vertices, edges);
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
                    if (p.depth == 1)
                        continue;
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

        //// this node is leaf and & not needed by index because of same path
        if (children.size() == 0 && depth != 1 && leafID != this.nodeID && !removeNode.contains(this)
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

    private void find_labelFiltering(List<Graph> G, int id, HashMap<Byte, Integer> qLabelMap) {
        for (IndexNode m : children) {
            if (m.traverse_num > 0) {
                int mapsize = m.labelFiltering.size();
                int cansize = Can.cardinality();
                if (cansize <= mapsize) {
                    for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
                            .nextSetBit(++trueIndex)) {
                        if (m.labelFiltering.get(trueIndex) != null) {
                            int qLabelNum = qLabelMap.get(m.frag.getVlabel());
                            int gLabelNum = m.labelFiltering.get(trueIndex).cardinality();
                            if (qLabelNum > gLabelNum) {
                                Can.set(trueIndex, false);
                                labelNumFiltering++;
                                a_labelNumFiltering++;
                                G.get(trueIndex).filterFlag.clear();
                                // System.out.println(id + ":" + trueIndex);
                            }
                        }
                    }
                } else {
                    for (int trueIndex : m.labelFiltering.keySet()) {
                        if (Can.get(trueIndex)) {
                            int qLabelNum = qLabelMap.get(m.frag.getVlabel());
                            int gLabelNum = m.labelFiltering.get(trueIndex).cardinality();
                            if (qLabelNum > gLabelNum) {
                                Can.set(trueIndex, false);
                                labelNumFiltering++;
                                a_labelNumFiltering++;
                                G.get(trueIndex).filterFlag.clear();
                                // System.out.println(id + ":" + trueIndex);
                            }
                        }
                    }
                }

            }
        }

    }

    private void init_Bitset() {
        In.clear();
        Can.clear();
        result.clear();
        can.clear();
    }

    private void verification_VEQ(String directory, String dataset, String mode, Graph q, int size)
            throws InterruptedException {
        long start = System.nanoTime();

        try {
            String command = String.format("./VEQ_S -dg datagraph.gfu -qg %s/%s/%s/%d/q%d.gfu -o output2.txt",
                    directory, dataset, mode, size, q.id);
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
                query_per_time = query_per_veq + (double) a_filterTime / 1000 / 1000;
                CT_verify += query_per_veq;
                // query_per_time = query_per_veq + (double) a_filterTime / 1000 / 1000;
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

                if (0 != Can.cardinality()) {
                    FPre = (double) result.cardinality() / Can.cardinality();
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
                    "query_id,FP,A/C,SP,filtering_time(ms),node filtering time(ms),filtering_time(ms)(VEQ),verification_time(ms)(VEQ),VEQs_time(ms),query_time(ms),filtering_num,inclusion_num,Candidate_num,VEQs_Candidate_num,VEQs_filtering_num,answer_num,|Σ(p)|,node graph fil,label graph fil,deleted Vsum/|Can|\n");
            // "query_id,FP,A/C,SP,filtering_time(ms),filtering_time(ms)(VEQ),verification_time(ms)(VEQ),VEQs_time(ms),query_time(ms),filtering_num,inclusion_num,Candidate_num,VEQs_Candidate_num,VEQs_filtering_num,answer_num,|G|,filter_time/filter_num\n");

        }

        double deletedVsumPerqPerg = 0;
        if (Can.cardinality() + labelFilteringGraph != 0) {
            deletedVsumPerqPerg = (double) deletedVsumPerq / (Can.cardinality() + labelFilteringGraph);
        }

        bw_data.write(q.id + "," + FPper_q + "," + FPre + ","
                + SPper_q + ","
                + String.format("%.8f", (double) a_filterTime / 1000 / 1000) + ","
                + String.format("%.8f", (double) a_nodeFiltering_time / 1000 / 1000) + ","
                + String.format("%.8f", fileter_time) + "," +
                String.format("%.8f", verify_time) + "," + String.format("%.8f",
                        query_per_veq)
                +
                "," + String.format("%.8f", query_per_time + (double) a_nodeFiltering_time / 1000 / 1000) + ","
                + a_fil_count + "," + In.cardinality() + "," +
                Can.cardinality() + "," + veq_per_Can + ","
                + (Can.cardinality() - veq_per_Can) + ","
                + result.cardinality() + "," +
                q.labels_Set().cardinality() + ","
                + labelFilteringGraph + ","
                + a_labelNumFiltering + ","
                + String.format("%.2f", deletedVsumPerqPerg)
                // + "," +
                // size
                // + ","
                // + String.format("%.6f", filpertime)
                + "\n");

        deletedVsumPerq = 0;
        a_nodeFiltering_time = 0;
        a_labelNumFiltering = 0;
        labelFilteringGraph = 0;
        query_per_veq = 0;
        fileter_time = 0;
        verify_time = 0;
        veq_per_Can = 0;

    }

    private void write_file(BufferedWriter allbw, BufferedWriter bw, int size) {
        try {
            allbw.write(String.format("%.5f", FP2 / nonfail) + "," +
                    String.format("%.5f", FP / nonfail) + ","
                    + String.format("%.5f", SP / nonfail) + ","
                    + String.format("%.6f", (double) search_time / 1000 / 1000 / nonfail) + ","// fil
                    + String.format("%.6f", CT_verify / nonfail) + ","// ver
                    // + String.format("%.6f", ((double) search_time / 1000 / 1000 + // query
                    // verification_time) / 100)
                    + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                            verification_time + (double) (edgeFiltering_time + nodeFiltering_time) / 1000 / 1000) / 100)
                    + ","
                    + String.format("%.6f", (double) contains_search / 1000 / 1000 / nonfail) + ","// tree1
                    + String.format("%.6f", (double) equals_search / 1000 / 1000 / nonfail) + ","// tree2
                    + String.format("%.6f", (double) edgeFiltering_time / 1000 / 1000 / nonfail) + ","// edge fil
                    + String.format("%.6f", (double) nodeFiltering_time / 1000 / 1000 / nonfail) + ","// node fil
                    + String.format("%.1f", (double) totoal_kai) + ","
                    + String.format("%.1f", (double) doukeicount) + ","
                    + String.format("%.1f", (double) fil_count) + ","
                    + String.format("%.1f", (double) lab_fil_num) + ","
                    + String.format("%.1f", (double) labelNumFiltering) + ","
                    // + String.format("%.1f", (double) removeTotalGraphs / nonfail) + ","//
                    // 何かを削除できたグラフ数
                    + String.format("%.1f", (double) query_per_nf_count) + ","// 削除できた頂点数
                    + String.format("%.1f", (double) filtering_edge_num) + ","// 削除できた辺数
                    + String.format("%.1f", (double) removeTotalSize) + ","// 削除できた総辺数

                    + String.format("%.6f", (((double) search_time / 1000 / 1000) / (size *
                            nonfail - doukeicount)))
                    + "," + (size * nonfail - doukeicount) + ","
                    + String.format("%.6f",
                            (size * nonfail - veq_Can_total)
                                    / (((double) search_time / 1000 / 1000) + query_per_veqF))
                    + "," + (size * nonfail - veq_Can_total) + "," + nonfail + "," + verfyNum
                    + "\n");

            bw.write("(C)書き込み関数時間(ms): " + String.format("%.2f", (double) write_time /
                    1000 / 1000) + "\n");
            bw.write("(D)プロセス関数時間(ms): " + String.format("%.2f", (double) process_time /
                    1000 / 1000) + "\n");
            bw.write("(E)読み込み関数時間(ms): " + String.format("%.2f", (double) read_time /
                    1000 / 1000) + "\n");

            bw.write("contain_search_time (ms): "
                    + String.format("%.6f", (double) contains_search / 1000 / 1000 / nonfail)
                    + "\n");
            bw.write("equals_search_time (ms): "
                    + String.format("%.6f", (double) equals_search / 1000 / 1000 / nonfail)
                    + "\n");

            bw.write("Number of Filtering Graphs: " + fil_count + "\n");
            bw.write("Number of Node Filteirng Graphs: " + lab_fil_num + "\n");
            bw.write("Number of Label Filteirng Graphs: " + labelNumFiltering + "\n");

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

            // bw.write("何かを削除できたグラフ数/q:" + (double) removeTotalGraphs / nonfail + "\n");
            // bw.write("削除できた頂点数/q:" + (double) query_per_nf_count / nonfail + "\n");
            bw.write("削除できた頂点数/|Can(Q)|:" + (double) query_per_nf_count / (doukeicount + lab_fil_num) + "\n");

            bw.write("削除できた辺数/q:" + (double) filtering_edge_num / nonfail + "\n");
            bw.write("削除できた総辺数/q:" + (double) removeTotalSize / nonfail + "\n");

            bw.write("(A)a Filtering Time (ms): "
                    + String.format("%.6f", (double) search_time / 1000 / 1000 / nonfail)
                    + "\n");

            bw.write("(C)Edge_Filtering_Time (ms): "
                    + String.format("%.6f", (double) edgeFiltering_time / 1000 / 1000 / nonfail)
                    + "\n");

            bw.write("(D)Node_Filtering_Time (ms): "
                    + String.format("%.6f", (double) nodeFiltering_time / 1000 / 1000 / nonfail)
                    + "\n");
            bw.write("(D+)Node_Filtering_Time (ms): "
                    + String.format("%.6f", (double) labelFiltering_time / 1000 / 1000 / nonfail)
                    + "\n");

            bw.write("(a)a VEQs Filtering Time (ms): " + String.format("%.6f",
                    query_per_veqF / nonfail) + "\n");
            bw.write("(b)a VEQs Verification Time (ms): " + String.format("%.6f",
                    query_per_veqV / nonfail) + "\n");
            bw.write("(B=a+b)a Verification Time (ms): " + String.format("%.6f",
                    query_per_sum / nonfail)
                    + "\n");
            bw.write("(A)+(B)a Processing Time (ms): "
                    + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                            verification_time) / 100)
                    + "\n");
            bw.write("(A)+(B)+(C)+(D) a Processing Time (ms): "
                    + String.format("%.6f", ((double) search_time / 1000 / 1000 +
                            verification_time + (double) (edgeFiltering_time + nodeFiltering_time) / 1000 / 1000) / 100)
                    + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init_param() {

        contains_search = 0;
        equals_search = 0;
        removeTotalSize = 0;
        removeTotalGraphs = 0;
        query_per_nf_count = 0;

        deletedVsumPerq = 0;

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
        nodeFiltering_time = 0;
        veq_per_Can = 0;
        query_per_veq = 0;
        fileter_time = 0;
        verify_time = 0;

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
                    if (frag.left.equals(m.frag)) {
                        m.search(result, q, frag.right, impl);
                    }
                }
            }
        }
    }
}

// private boolean chech_addMapping(int id, int[] vertexIDs) {
// for(int map:this.mapping.get(id)){

// }
// return false;
// }

// void find_once_node() {
// if (traverse_num == 1) {
// // System.out.println(nodeID);
// }
// for (IndexNode m : children) {
// m.find_once_node();
// }
// }

// BitSet labels = getNoTraversNode(g, id, new BitSet());
// if (containsAll(gLabels, labels))// 45055890
// {
// // System.out.println(labels.toString());
// // System.out.println(gLabels.toString());
// return;
// }

// private boolean backtrackJudge2(Graph g, int id) {
// for (IndexNode m : children) {
// if (!m.traverseCount1.get(id) && !m.traverseCount2.get(id)) {
// // if (!m.matchGraphIndicesBitSet.get(id)) {
// return false;
// }
// if (m.depth <= g.order) {
// if (!m.backtrackJudge2(g, id))
// return false;
// }
// }
// return true;
// }

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

// private void addIDtoTree2(Graph g, SearchInfo info, GraphCode impl, int id) {
// traverse_cou++;
// g_traverse_num++;

// matchGraphIndicesBitSet.set(id, true);

// if (g_traverse_num == 1) {
// int[] vertexIDs = info.getVertexIDs();
// mapping.put(id, vertexIDs);
// } else if (g_traverse_num == 2) {
// // int[] vertexIDs = info.getVertexIDs();
// // if (chech_addMapping(id, vertexIDs)) {
// // mapping.put(id, vertexIDs);
// // }
// mapping.remove(id);
// }

// // int[] vertexIDs = info.getVertexIDs();
// // mapping2.put(id, vertexIDs[vertexIDs.length - 1]);

// // if (!traverseCount1.get(id) && !traverseCount2.get(id)) {// 0 times
// // traverseCount1.set(id, true);
// // int[] vertexIDs = info.getVertexIDs();
// // // mapping2.put(id, vertexIDs[vertexIDs.length - 1]);
// // mapping.put(id, vertexIDs);
// // } else if (traverseCount1.get(id) && !traverseCount2.get(id)) {// onec
// // traverseCount2.set(id, true);
// // mapping.remove(id);
// // // mapping2.remove(id);
// // // 親が２回以上探索されたため、子(or子孫)の節点のmappingを消す
// // // removeDescendantNodeMapping(id);
// // }

// if (children.size() == 0) {
// return;
// }

// if (backtrackJudge(g)) {
// return;
// }

// List<Pair<CodeFragment, SearchInfo>> nextFrags =
// impl.enumerateFollowableFragments_adj(g, info, adjLabels);

// for (IndexNode m : children) {
// for (Pair<CodeFragment, SearchInfo> frag : nextFrags) {
// if (frag.left.equals(m.frag)) {
// // if (frag.left.contains(m.frag)) {
// // if (m.frag.contains(frag.left)) {// 逆contain
// // if (m.frag.contains(frag.left) && !frag.left.equals(m.frag)) {
// m.addIDtoTree2(g, frag.right, impl, g.id);
// }
// }
// }
// }

// private void write_file_for_Ver_new(HashMap<Integer, ArrayList<String>>
// gMaps,
// LinkedHashMap<Integer, List<Pair<Integer, Integer>>> edgeFiltering,
// List<Graph> G) {

// long time = System.nanoTime();
// // try (BufferedWriter bw2 = Files.newBufferedWriter(out)) {
// try (
// OutputStream os = Files.newOutputStream(out);
// BufferedOutputStream bos = new BufferedOutputStream(os);) {

// for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
// .nextSetBit(++trueIndex)) {
// // if (trueIndex != 23962)
// // continue;

// // can.add(trueIndex);
// // a graph be able to be possibl filtering node
// if (edgeFiltering.get(trueIndex) != null &&
// edgeFiltering.get(trueIndex).size() > 0) {
// // System.out.println(filteringNodes.get(trueIndex).size());
// // if (trueIndex != 959)
// // continue;
// Graph g = G.get(trueIndex);
// int newOrder = 0;
// int newSize = 0;
// int[] map = new int[g.order];
// byte[][] newEdges = new byte[g.order][];
// // System.arraycopy(g.edges, 0, newEdges, 0, g.order);
// for (int i = 0; i < g.order; i++) {
// newEdges[i] = g.edges[i].clone(); // オブジェクトがクローン可能な場合
// }

// for (Pair<Integer, Integer> p : edgeFiltering.get(trueIndex)) {
// newEdges[p.left][p.right] = 0;
// newEdges[p.right][p.left] = 0;
// }

// for (int v = 0; v < g.order; ++v) {
// boolean allZeros = true; // 一行が全て0かどうかを示すフラグ
// // if (!filteringNodes.get(trueIndex).contains(v)) {
// // map[newOrder++] = v;
// // continue;
// // }
// for (int u = 0; u < g.order; ++u) {
// if (newEdges[v][u] > 0) {
// ++newSize;
// allZeros = false; // 一行に0以外の要素がある場合、フラグをfalseに設定
// }
// }
// if (!allZeros) {
// map[newOrder++] = v;
// }
// }
// newSize /= 2;

// removeTotalSize += g.size - newSize;
// if (g.size - newSize != 0)
// removeTotalGraphs++;

// if (newOrder == 0 || newSize == 0) {
// continue;
// }

// can.add(trueIndex);

// String line = "#" + g.id + "\n";
// byte[] bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み

// // bw2.write("#" + g.id + "\n");
// line = newOrder + "\n";
// bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み

// for (int i = 0; i < newOrder; i++) {
// line = g.vertices[map[i]] + "\n";
// bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み
// }

// line = newSize + "\n";
// bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み
// for (int i = 0; i < newOrder; i++) {
// for (int j = i; j < newOrder; j++) {
// if (newEdges[map[i]][map[j]] > 0) {
// line = i + " " + j + "\n";
// bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み
// }
// }
// }
// } else {
// can.add(trueIndex);
// byte[] bytes = new byte[1024];
// for (String line : gMaps.get(trueIndex)) {
// bytes = line.getBytes();
// bos.write(bytes); // バイトデータをファイルに書き込み
// // bw2.write(line + "\n");
// }
// }
// }

// } catch (IOException e) {
// System.exit(1);
// }
// write_time += System.nanoTime() - time;

// }

// void addDescendantsLabels() {
// // BitSet descendantsLabels = new BitSet();

// for (IndexNode m : children) {
// // System.out.println(m.nodeID);
// getDescendantsLabels(descendantsLabels);

// m.addDescendantsLabels();
// }
// }
// BitSet getDescendantsLabels(BitSet labels) {
// // BitSet descendantsLabels = new BitSet();
// for (IndexNode m : children) {
// labels.set(m.frag.getVlabel());
// labels = m.getDescendantsLabels(labels);
// }
// return labels;

// }

// String qFileName = String.format("Query2/%s/%s/%d/q%d.gfu",
// dataset, mode, q.size, q.id);

// directory = "Query2";
// String directories = String.format("Query2/%s/%s/%d",
// dataset, mode, q.size);

// Path target = Paths.get(directories);

// // ディレクトリを作成
// try {
// Files.createDirectories(target);
// } catch (IOException e) {
// System.out.println("ディレクトリの作成に失敗しました。");
// e.printStackTrace();
// }
// try (BufferedWriter bw2 = Files.newBufferedWriter(Paths.get(qFileName))) {
// q = q.shirinkNEC();
// q.writeGraph2Gfu(bw2);
// }
// private BitSet getNoTraversNode(Graph g, int id, BitSet labels) {
// for (IndexNode m : children) {
// if (!m.matchGraphIndicesBitSet.get(id)) {
// labels.set(m.frag.getVlabel());
// }
// if (m.depth <= g.order) {
// labels = m.getNoTraversNode(g, id, labels);
// }
// }
// return labels;
// }

// there are no descendants node because those have labels g has not
// private boolean containsAll(BitSet gLabels, BitSet labels) {

// for (int trueIndex = labels.nextSetBit(0); trueIndex != -1; trueIndex =
// labels
// .nextSetBit(++trueIndex)) {
// if (gLabels.get(trueIndex)) {
// return false;
// }
// }
// return true;// 一度もたどっていないノードが全てGにないラベルであればバックトラック
// }

// void find_labelFiltering2(List<Graph> G) {

// for (IndexNode m : children) {
// if (m.traverse_num == 0) {
// int mapsize = m.labelFiltering.size();
// int cansize = Can.cardinality();
// if (cansize <= mapsize) {
// for (int trueIndex = Can.nextSetBit(0); trueIndex != -1; trueIndex = Can
// .nextSetBit(++trueIndex)) {
// if (m.labelFiltering.get(trueIndex) != null) {
// Graph g = G.get(trueIndex);
// for (int v = m.labelFiltering.get(trueIndex).nextSetBit(0); v != -1; v =
// m.labelFiltering
// .get(trueIndex)
// .nextSetBit(++v)) {
// query_per_nf_count++;
// g.vertices[v] = -1;
// }
// }
// }
// } else {
// for (int trueIndex : m.labelFiltering.keySet()) {
// if (Can.get(trueIndex)) {
// Graph g = G.get(trueIndex);
// for (int v = m.labelFiltering.get(trueIndex).nextSetBit(0); v != -1; v =
// m.labelFiltering
// .get(trueIndex)
// .nextSetBit(++v)) {
// query_per_nf_count++;
// g.vertices[v] = -1;

// }

// }
// }
// }

// }
// }
// }