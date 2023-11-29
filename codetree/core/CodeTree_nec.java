package codetree.core;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class CodeTree_nec implements Serializable {
    GraphCode impl;
    public IndexNode root;
    public static int datasetSize;
    public static int delta;
    Random rand;

    public CodeTree_nec(GraphCode impl, List<Graph> G, BufferedWriter bw, String dataset,
            BufferedWriter index) throws IOException {

        int limDepth = 0;
        datasetSize = G.size();// test
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
                limDepth = 6;
                break;

            case "ppigo":
                limDepth = 7;
                // rand = new Random(1);
                break;
        }

        int totalOrder = 0;
        int newOrder = 0;

        for (Graph g : G) {
            totalOrder += g.order;
            Graph gn = g.shirinkNEC();
            newOrder += gn.order;
            G.set(g.id, gn);
        }
        System.out.println(dataset + ":" + (double) (totalOrder - newOrder) /
                G.size());

        for (Graph g : G) {
            ArrayList<Integer> vertexIDs = new ArrayList<>();
            int start_vertice = rand.nextInt(g.order);
            code = impl.computeCanonicalCode_nec(g, start_vertice, limDepth, vertexIDs);
            root.addPath_nec(code, g, vertexIDs);
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
        root.getLeafGraph_nec(leafGraphs);
        inclusionCheck2(impl, leafGraphs);
        root.removeTree();
        treesize = root.size();

        System.out.println("tree size (new): " + treesize);

        bw.write("Tree size(new): " + treesize + "\n");
        index.write(
                treesize + "," + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000) + ",");

        System.out.println(
                "remove node time :" + String.format("%.6f", (double) (System.nanoTime() - time) / 1000 / 1000));

        root.addInfo();

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
            root.addIDtoTree_nec(g, impl, g.id, gLabels);

        }
    }

    private void inclusionCheck2(GraphCode impl, List<Graph> leafGraphs) {

        ArrayList<Integer> idList = new ArrayList<>();
        ArrayList<Integer> removeIDList = new ArrayList<>();

        for (Graph g : leafGraphs) {
            if (removeIDList.contains(g.id))
                continue;

            idList.add(g.id);
            root.pruningEquivalentNodes_nec(g, impl, g.id, idList, removeIDList);
        }
    }

    public BitSet subgraphSearch(Graph query, BufferedWriter bw, int size, String mode, String dataset,
            BufferedWriter bwout, BufferedWriter allbw, List<Graph> G, int qsize,
            HashMap<Integer, ArrayList<String>> gMaps, BufferedWriter br_whole)
            throws IOException, InterruptedException {
        return root.subsearch_nec(query, impl, size, bw, mode, dataset, bwout, allbw, G, "Query", qsize, gMaps, delta,
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