package codetree.core;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import codetree.vertexBased.AcgmCode;

public class CodeTree implements Serializable {
    GraphCode impl;
    public IndexNode root;
    public static int delta;
    Random rand;

    public CodeTree(GraphCode impl, List<Graph> G, BufferedWriter bw, String dataset,
            BufferedWriter index) throws IOException {

        int limDepth = 0;
        this.impl = impl;
        this.root = new IndexNode(null, null);
        rand = new Random(2);

        List<CodeFragment> code = new ArrayList<>();

        long time = System.nanoTime();

        List<ArrayList<CodeFragment>> codelist = impl.computeCanonicalCode(Graph.numOflabels(G));
        for (ArrayList<CodeFragment> c : codelist) {
            root.addPath(c, -1, false);
        }

        switch (dataset) {
            case "AIDS":
                // limDepth = 9;
                limDepth = 5;

                break;

            case "COLLAB":
                limDepth = 5;
                break;

            case "REDDIT-MULTI-5K":
                limDepth = 3;
                break;

            case "pdbs":
                limDepth = 16;
                break;

            case "IMDB-MULTI":
                limDepth = 5;
                break;

            case "pcms":
                limDepth = 5;
                break;

            case "ppigo":
                limDepth = 7;
                break;
        }

        delta = limDepth;
        int loop = 1;
        for (Graph g : G) {
            for (int l = 0; l < loop; l++) {
                int start_vertice = rand.nextInt(g.order);
                code = impl.computeCanonicalCode(g, start_vertice, limDepth);
                // System.out.println(code.toString());
                root.addPath(code, g.id, false);
            }
        }
        index.write(dataset + "," + limDepth + ","
                + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000) +
                ",");

        System.out.println("depth " + (limDepth));
        bw.write("limDepth" + (limDepth) + "\n");
        System.out.println("Tree size: " + root.size());
        System.out.println("addPathtoTree(ms): " + (System.nanoTime() - time) / 1000 /
                1000);
        bw.write("Tree size(original): " + root.size() + "\n");
        bw.write("addPathtoTree(ms): " + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000)
                + "\n");

        long start = System.nanoTime();

        int treesize = root.size();

        System.out.println("tree size (original): " + treesize);
        index.write(treesize + ",");

        List<Graph> leafGraphs = new ArrayList<>();
        root.getLeafGraph(impl, leafGraphs);
        inclusionCheck2(impl, leafGraphs);
        root.removeTree();
        treesize = root.size();

        System.out.println("tree size (new): " + treesize);

        bw.write("Tree size(new): " + treesize + "\n");
        index.write(
                treesize + "," + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000) + ",");

        System.out.println(
                "remove node time :" + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000));

        if (impl == new AcgmCode()) {
            root.addInfo();
        }

        start = System.nanoTime();
        System.out.println("グラフIDの計算中");
        inclusionCheck(impl, G);
        bw.write("addIDtoTree(ms): " + String.format("%.3f", (double) (System.nanoTime() - start) / 1000 / 1000)
                + "\n");
        System.out.println("\naddIDtoTree: " + (System.nanoTime() - start) / 1000 /
                1000 + "msec");
        index.write(String.format("%.3f", (double) (System.nanoTime() - start) / 1000
                / 1000));

        // try {
        // String codetree = String.format("data_structure/%s/depth%d_structure.ser",
        // dataset, limDepth);
        // FileOutputStream fileOut = new FileOutputStream(codetree);
        // ObjectOutputStream objout = new ObjectOutputStream(fileOut);
        // objout.writeObject(this);
        // objout.close();
        // fileOut.close();
        // System.out.println("データ構造がシリアライズされ、ファイルに保存されました。");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    public CodeTree(GraphCode impl, List<Graph> G, int b) {
        this.impl = impl;
        this.root = new IndexNode(null, null);

        System.out.print("Indexing");
        for (int i = 0; i < G.size(); ++i) {
            Graph g = G.get(i);

            List<CodeFragment> code = impl.computeCanonicalCode(g, b);// 準正準コードを得る
            root.addPath(code, i, true);

            if (i % 100000 == 0) {
                System.out.println();
            } else if (i % 10000 == 0) {
                System.out.print("*");
            } else if (i % 1000 == 0) {
                System.out.print(".");
            }
        }

        System.out.println();
        System.out.println("Tree size: " + root.size());
    }

    private void inclusionCheck(GraphCode impl, List<Graph> G) {
        // root.addDescendantsLabels();
        for (Graph g : G) {
            // g = g.shirinkNEC();
            if (g.id % 100000 == 0) {
                // System.out.println();
            } else if (g.id % (G.size() / 2) == 0) {
                System.out.print("*");
            } else if (g.id % (G.size() / 10) == 0) {
                System.out.print(".");
            }
            BitSet gLabels = g.labels_Set();
            // root.addIDtoTree(g, impl, g.id);
            root.addIDtoTree(g, impl, g.id, gLabels);

        }
    }

    private void inclusionCheck2(GraphCode impl, List<Graph> leafGraphs) {

        ArrayList<Integer> idList = new ArrayList<>();
        ArrayList<Integer> removeIDList = new ArrayList<>();

        for (Graph g : leafGraphs) {
            if (removeIDList.contains(g.id))
                continue;

            idList.add(g.id);
            root.pruningEquivalentNodes(g, impl, g.id, idList, removeIDList);

        }
    }

    public List<Integer> supergraphSearch(Graph query) {
        return root.search(query, impl);
    }

    public BitSet subgraphSearch(Graph query, BufferedWriter bw, int size, String mode, String dataset,
            BufferedWriter bwout, BufferedWriter allbw, List<Graph> G, int qsize,
            HashMap<Integer, ArrayList<String>> gMaps, BufferedWriter br_whole)
            throws IOException, InterruptedException {
        return root.subsearch(query, impl, size, bw, mode, dataset, bwout, allbw, G, "Query", qsize, gMaps, delta,
                br_whole);
    }
}

// private void shirinkNEC(List<Graph> G) {
// int before = 0;
// int necNUM = 0;
// long shrinkTime = 0;
// long start = System.nanoTime();

// for (Graph g : G) {
// before = g.order;
// Graph g2 = g.shirinkNEC();
// G.set(g.id, g2);
// necNUM += before - g2.order;
// }
// shrinkTime = System.nanoTime() - start;

// System.out.println("削減頂点数:" + necNUM);
// System.out.println("削減関数時間[ms]" + shrinkTime / 1000 / 1000);
// }