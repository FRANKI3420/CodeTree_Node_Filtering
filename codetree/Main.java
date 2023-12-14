package codetree;

import codetree.common.*;
import codetree.core.*;
import codetree.edgeBased.DfsCode;
import codetree.vertexBased.AcgmCode;
import codetree.vertexBased.XAcgmCode;
import java.io.*;
import java.nio.file.*;
import java.util.*;

class Main {
    static Random rand;

    private static String sdfFilename = "aido99sd.sdf";
    private static String resultFilename;
    private static String gfuFilename;
    private static String q_gfuFilename;
    private static String dataset;
    private static GraphCode graphCode;
    private static int graphCodeID = 1;
    private static int searchID = 1;
    private static int datasetID = 0;
    private static int datasetSize = 0;

    public static void main(String[] args) throws InterruptedException {
        if (searchID < 1 && searchID > 2) {
            System.out.println("無効なグラフコードです。");
            System.exit(0);
        }

        long max = Runtime.getRuntime().maxMemory();

        System.out.println("max: " + max / 1024 / 1024 / 1024);
        String allindex = String.format("result/all_index.csv", dataset);
        String wholeresult = String.format("result/all_result.csv", dataset);
        Path writeindex = Paths.get(allindex);
        Path writewhole = Paths.get(wholeresult);
        try (BufferedWriter allfind = Files.newBufferedWriter(writeindex);
                BufferedWriter br_whole = Files.newBufferedWriter(writewhole)) {
            allfind.write(
                    "dataset,depth,addPathtoTree(ms),Tree_size,Tree_size(new),remove_time(ms),addIDtoTree(ms),Build_tree(ms)\n");
            br_whole.write(
                    "dataset,query_set,A/C,(G-C)/(G-A),SP,filtering_time(ms),verification_time(ms),query_time(ms),tree1_search_time(ms),node_fil_time(ms),|In(Q)|,|A(Q)|,|Can(Q)|,|F(Q)|,Num deleted Vertices,total deleted edges Num,codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail,verify num,q_trav_num\n");

            for (datasetID = 0; datasetID <= 6; datasetID++) {

                boolean nec = false;
                if (datasetID < 0 || datasetID > 6) {
                    System.out.println("無効なデータセットIDです");
                    System.exit(0);
                }

                parseArgs(args);

                if (searchID == 1) {

                    List<ArrayList<Pair<Integer, Graph>>> Q = new ArrayList<>();
                    final int querysize = 100;
                    final int minedge = 4;
                    final int maxedge = 64;

                    List<Graph> G = SdfFileReader.readFile_gfu(Paths.get(gfuFilename));

                    for (int numOfEdge = minedge; numOfEdge <= maxedge; numOfEdge *= 2) {
                        ArrayList<Pair<Integer, Graph>> qset = new ArrayList<>();
                        for (int i = 0; i < querysize; i++) {
                            q_gfuFilename = String.format("Query/%s/randomwalk/%d/q%d.gfu", dataset,
                                    numOfEdge, i);
                            Graph q = SdfFileReader.readFileQuery_gfu(Paths.get(q_gfuFilename));
                            qset.add(new Pair<Integer, Graph>(i, q));
                        }
                        // query_search(qset, numOfEdge, "R");
                        Q.add(qset);
                    }

                    for (int numOfEdge = minedge; numOfEdge <= maxedge; numOfEdge *= 2) {
                        ArrayList<Pair<Integer, Graph>> qset = new ArrayList<>();
                        for (int i = 0; i < querysize; i++) {
                            q_gfuFilename = String.format("Query/%s/bfs/%d/q%d.gfu", dataset, numOfEdge,
                                    i);
                            Graph q = SdfFileReader.readFileQuery_gfu(Paths.get(q_gfuFilename));
                            qset.add(new Pair<Integer, Graph>(i, q));
                        }
                        // query_search(qset, numOfEdge, "B");
                        Q.add(qset);
                    }

                    System.out.println("G size: " + G.size());

                    System.out.println("Q size: " + Q.size() * querysize);

                    String output = String.format("result/%s_output.txt", dataset);
                    Path out = Paths.get(output);

                    String allresult = String.format("result/%s_result.csv",
                            dataset);
                    Path all = Paths.get(allresult);

                    try (BufferedWriter bw2 = Files.newBufferedWriter(out);
                            BufferedWriter allbw = Files.newBufferedWriter(all);) {
                        allbw.write(
                                // "dataset,query_set,FP_ratio,(G-C)/(G-A),SP,filtering_time(ms),verification_time(ms),query_time(ms),codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail\n");
                                // "dataset,query_set,FP_ratio,A/C,SP,filtering_time(ms),verification_time(ms),query_time(ms),tree1_search_time(ms),tree2_search_time(ms),edge_fil_time(ms),node_fil_time(ms),|A(Q)|,|Can(Q)|,Number
                                // of graphs to delete,Number of vertices removed,Num Deleted Edges,total number
                                // of deleted
                                // edges,codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail\n");
                                "dataset,query_set,A/C,(G-C)/(G-A),SP,filtering_time(ms),verification_time(ms),query_time(ms),tree1_search_time(ms),node_fil_time(ms),|In(Q)|,|A(Q)|,|Can(Q)|,|F(Q)|,Num deleted Vertices,total deleted edges Num,codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail,verify num,q_trav_num\n");

                        System.out.println(" ");
                        resultFilename = String.format("result/%s_result.txt",
                                dataset);

                        Path res = Paths.get(resultFilename);
                        try (BufferedWriter bw = Files.newBufferedWriter(res)) {

                            long start = System.nanoTime();

                            if (!nec) {

                                // for (int i = 0; i < 10; i++) {
                                System.out.println("tree1");
                                CodeTree tree = new CodeTree(graphCode, G, bw, dataset, allfind);

                                bw.write("Build tree(ms): "
                                        + String.format("%.6f", (double) (System.nanoTime() - start) / 1000 / 1000)
                                        +
                                        "\n");
                                allfind.write(","
                                        + String.format("%.6f", (double) (System.nanoTime() - start) / 1000 / 1000)
                                        + "\n");

                                // if (true)
                                // continue;

                                HashMap<Integer, ArrayList<String>> gMaps = makeGmaps(gfuFilename);

                                int index = minedge;
                                String mode = null;
                                String data_out = null;
                                int[] adjust = new int[Q.size() / 2];
                                int count = 0;
                                int count2 = 0;

                                for (ArrayList<Pair<Integer, Graph>> Q_set : Q) {

                                    // if (index <= maxedge) {
                                    // index *= 2;
                                    // continue;
                                    // }
                                    // if (index > maxedge) {
                                    // index *= 2;
                                    // continue;
                                    // }
                                    // if (datasetID == 6 && index != 32) {
                                    // index *= 2;
                                    // continue;
                                    // }

                                    if ((datasetID == 6 && index == 64)) {
                                        index *= 2;
                                        continue;
                                    }
                                    // if ((datasetID == 3 && index == 64) || (datasetID == 6 && index == 32)
                                    // || (datasetID == 6 && index == 64)) {
                                    // index *= 2;
                                    // continue;
                                    // }

                                    // if ((datasetID == 3 && index >= 32) || (datasetID == 6 && index >= 32)
                                    // || index >= 128) {
                                    // index *= 2;
                                    // continue;
                                    // }
                                    // if ((datasetID == 6 && index >= 32)) {
                                    // index *= 2;
                                    // continue;
                                    // }

                                    if (index <= maxedge) {
                                        adjust[count++] = index;
                                        System.out.println("\nQ" + index + "R");
                                        bw.write("Q" + index + "R\n");
                                        bw2.write("Q" + index + "R\n");
                                        allbw.write(dataset + ",Q" + index + "R,");
                                        br_whole.write(dataset + ",Q" + index + "R,");
                                        data_out = String.format("result/%s_%dR_data.csv", dataset,
                                                index);
                                        mode = "randomwalk";
                                    } else {
                                        int size = adjust[count2++];

                                        System.out.println("\nQ" + size + "B");
                                        bw.write("Q" + size + "B\n");
                                        bw2.write("Q" + size + "B\n");
                                        allbw.write(dataset + ",Q" + size + "B,");
                                        br_whole.write(dataset + ",Q" + size + "B,");
                                        data_out = String.format("result/%s_%dB_data.csv", dataset,
                                                size);
                                        mode = "bfs";
                                    }

                                    try (BufferedWriter bwout = new BufferedWriter(
                                            new OutputStreamWriter(new FileOutputStream(data_out), "UTF-8"));) {

                                        start = System.nanoTime();

                                        for (Pair<Integer, Graph> q : Q_set) {
                                            if (q.left == 0) {
                                                System.out.print("");
                                            } else if (q.left % 50 == 0) {
                                                System.out.print("*");
                                            } else if (q.left % 10 == 0) {
                                                System.out.print(".");
                                            }
                                            BitSet result = tree.subgraphSearch(q.right, bw, datasetSize, mode,
                                                    dataset,
                                                    bwout, allbw, G, q.right.size, gMaps, br_whole);

                                            bw2.write(
                                                    q.left.toString() + " " + result.cardinality() + "個"
                                                            + result.toString()
                                                            + "\n");
                                        }
                                        final long time = System.nanoTime() - start;
                                        bw.write("(A)*100+(C)+(D)+(E)+(α) 合計処理時間(ms): " + (time / 1000 / 1000) +
                                                "\n");
                                        index *= 2;
                                        Q_set = null;
                                    }
                                    bw.write("*********************************\n");
                                }
                                // }
                            }
                        }

                    } catch (IOException e) {
                        System.out.println(e);
                    }

                } else if (searchID == 2) {
                    datasetID = 0;
                    System.out.println("スーパーグラフ検索を開始します");

                    List<Graph> G = new ArrayList<Graph>();

                    for (int i = 0; i < datasetSize; i++) {
                        String filename = String.format("%s/g%d.gfu", dataset, i);
                        Graph g = SdfFileReader.readFileQuery_gfu(Paths.get(filename));
                        G.add(g);
                    }

                    // List<Graph> G = SdfFileReader.readFile(Paths.get(sdfFilename));

                    ArrayList<Pair<Integer, Graph>> Q = new ArrayList<>();
                    for (int i = 0; i < G.size(); ++i) {
                        Graph g = G.get(i);

                        final int size = g.size();

                        if (34 <= size && size <= 36 && g.isConnected()) {
                            Q.add(new Pair<Integer, Graph>(i, g));
                        }
                    }

                    System.out.println("G size: " + G.size());
                    System.out.println("Q size: " + Q.size());

                    long start = System.nanoTime();
                    CodeTree tree = new CodeTree(graphCode, G, 100);// コード木構築
                    System.out.println("Build tree: " + (System.nanoTime() - start) / 1000 / 1000
                            + "msec");

                    G = null;

                    Path out = Paths.get("output_supergraph.txt");
                    try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                        start = System.nanoTime();

                        for (Pair<Integer, Graph> q : Q) {
                            List<Integer> result = tree.supergraphSearch(q.right);
                            bw.write(q.left.toString() + result.toString() + "\n");
                        }

                        final long time = System.nanoTime() - start;
                        System.out.println((time) + " nano sec");
                        System.out.println((time / 1000 / 1000) + " msec");
                    } catch (IOException e) {
                        System.exit(1);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-code")) {
                if (args[++i].equals("acgm"))
                    graphCode = new AcgmCode();
                else if (args[i].equals("xacgm"))
                    graphCode = new XAcgmCode();
                else if (args[i].equals("dfs"))
                    graphCode = new DfsCode();
                else {
                    System.err.println("無効なグラフコード: " + args[i]);
                    System.exit(1);
                }
            } else if (sdfFilename == null) {
                sdfFilename = args[i];
            } else {
                System.err.println("無効な引数: " + args[i]);
                System.exit(1);
            }
        }

        if (searchID == 1)
            graphCodeID = 1;

        if (graphCodeID == 1) {
            graphCode = new AcgmCode();
            // System.out.println("AcGMコードで動作します");
        } else if (graphCodeID == 2) {
            graphCode = new XAcgmCode();
            System.out.println("XAcGMコードで動作します");
        } else {
            graphCode = new DfsCode();
            System.out.println("Dfsコードで動作します");
        }

        if (datasetID == 1) {
            gfuFilename = "pdbs.gfu";
            resultFilename = "PDBS_result.txt";
            dataset = "pdbs";
            datasetSize = 600;
            System.out.println("PDBS");
        } else if (datasetID == 2) {
            gfuFilename = "pcms.gfu";
            resultFilename = "PCM_result.txt";
            dataset = "pcms";
            datasetSize = 200;
            System.out.println("PCM");
        } else if (datasetID == 3) {
            gfuFilename = "ppigo.gfu";
            resultFilename = "PPI_result.txt";
            dataset = "ppigo";
            datasetSize = 20;
            System.out.println("PPI");
        } else if (datasetID == 4) {
            gfuFilename = "IMDB-MULTI.gfu";
            resultFilename = "IMDB_result.txt";
            dataset = "IMDB-MULTI";
            datasetSize = 1500;
            System.out.println("IMDB");
        } else if (datasetID == 5) {
            gfuFilename = "REDDIT-MULTI-5K.gfu";
            resultFilename = "REDDIT_result.txt";
            dataset = "REDDIT-MULTI-5K";
            datasetSize = 4999;
            System.out.println("REDDIT");
        } else if (datasetID == 6) {
            gfuFilename = "COLLAB.gfu";
            resultFilename = "COLLAB_result.txt";
            dataset = "COLLAB";
            datasetSize = 5000;
            System.out.println("COLLAB");
        } else if (datasetID == 0) {
            gfuFilename = "AIDS.gfu";
            resultFilename = "AIDS_result.txt";
            dataset = "AIDS";
            datasetSize = 40000;
            System.out.println("AIDS");
        }
    }

    static HashMap<Integer, ArrayList<String>> makeGmaps(String filePath) {
        HashMap<Integer, ArrayList<String>> gMaps = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = "a";
            while (line != null) {
                if (line.startsWith("#")) {
                    int id = Integer.parseInt(line.substring(1));
                    ArrayList<String> lines = new ArrayList<>();
                    lines.add(line);
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("#"))
                            break;
                        lines.add(line);
                    }
                    gMaps.put(id, lines);
                } else {
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gMaps;
    }

    private static void query_search(ArrayList<Pair<Integer, Graph>> Q, int numOfEdge, String mode) {

        double V = 0;
        double e = 0;
        double sigma = 0;

        for (Pair<Integer, Graph> q : Q) {
            Graph g = q.right;
            V += g.order();
            e += g.size();
            sigma += g.labels();
        }
        // System.out.println("Q_" + numOfEdge + mode + " |V| per q " + V / Q.size());
        // System.out.println("Q_" + numOfEdge + mode + " |E| per q " + e / Q.size());
        System.out.println("Q_" + numOfEdge + mode + " d per q " + String.format("%.2f", e * 2 / V));
        // System.out.println("Q_" + numOfEdge + mode + " |Σ| per q " + sigma /
        // Q.size());
        System.out.println();
    }

    private static void dataset_search(List<Graph> G) {

        double V = 0;
        double e = 0;
        double sigma = 0;
        double d = 0;

        for (int i = 0; i < G.size(); i++) {
            Graph g = G.get(i);
            V += g.order();
            e += g.size();
            d += g.degree();
            sigma += g.labels();
        }
        System.out.println("|G| " + G.size());
        System.out.println("labels " + Graph.numOflabels(G));
        System.out.println("|V| per g " + V / G.size());
        System.out.println("|E| per g " + e / G.size());
        System.out.println(" d per g " + String.format("%.2f", d / G.size()));
        System.out.println("|Σ| per g " + sigma / G.size());
        System.out.println("最大頂点数 " + Graph.max_vertice(G));
        System.out.println("最大辺数 " + Graph.max_size(G));
        System.out.println();
    }

    private static void print_q(List<ArrayList<Pair<Integer, Graph>>> Q) {
        Path out = Paths.get("queryput.txt");
        int index = 8;
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            for (ArrayList<Pair<Integer, Graph>> Q_set : Q) {
                if (index <= 32)
                    bw.write("Q_" + index + "R\n");
                else
                    bw.write("Q_" + index / 16 + "B\n");
                // bw.write("*\n");
                for (Pair<Integer, Graph> q : Q_set) {
                    bw.write(q.left + "\n");
                    for (int i = 0; i < q.right.vertices.length; i++) {
                        bw.write(q.right.vertices[i] + " ");
                    }
                    bw.write("\n");
                    for (int i = 0; i < q.right.vertices.length; i++) {
                        for (int j = 0; j < q.right.vertices.length; j++) {
                            bw.write(q.right.edges[i][j] + " ");
                        }
                        bw.write("\n");
                    }
                }
                index *= 2;
            }
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private static void print_g(List<Graph> G) {
        Path out = null;
        out = Paths.get("Gput.txt");

        int id = 0;

        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            for (Graph g : G) {
                bw.write(id + "\n");
                for (int i = 0; i < g.vertices.length; i++) {
                    bw.write(g.vertices[i] + " ");
                }
                bw.write("\n");
                for (int i = 0; i < g.vertices.length; i++) {
                    for (int j = 0; j < g.vertices.length; j++) {
                        bw.write(g.edges[i][j] + " ");
                    }
                    bw.write("\n");
                }
                id++;
            }
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private static ArrayList<Pair<Integer, Graph>> setQuery_RandomWalk(List<Graph> G, int querysize, int numOfEdge) {
        ArrayList<Pair<Integer, Graph>> Q = new ArrayList<>(querysize);
        int count = 0;
        double V = 0;
        double e = 0;
        double sigma = 0;
        rand = new Random(1);

        while (count < querysize) {
            Graph g;
            while (true) {
                int random = rand.nextInt(G.size());
                g = G.get(random);
                if (g.size() >= numOfEdge && g.isConnected())
                    break;
            }
            Graph q = g.set_ramQ(numOfEdge);
            V += q.order();
            e += q.size();
            sigma += q.labels();
            Q.add(new Pair<Integer, Graph>(count, q));
            count++;
        }
        // System.out.println("Q_"+ numOfEdge + "S |V| per q " + V/querysize );
        // System.out.println("Q_"+ numOfEdge +"S |E| per q " + e/querysize );
        // System.out.println("Q_"+ numOfEdge +"S d per q " +
        // String.format("%.2f",e*2/V));
        // System.out.println("Q_"+ numOfEdge +"S |Σ| per q " + sigma/querysize );
        // System.out.println();
        return Q;
    }

    private static ArrayList<Pair<Integer, Graph>> setQuery_BFWalk(List<Graph> G, int querysize, int numOfEdge) {
        ArrayList<Pair<Integer, Graph>> Q = new ArrayList<>(querysize);
        int count = 0;
        double V = 0;
        double e = 0;
        double sigma = 0;
        rand = new Random(2);

        while (count < querysize) {
            Graph g;
            Graph q;
            while (true) {
                int random = rand.nextInt(G.size());
                g = G.get(random);
                if (g.size() >= numOfEdge && g.isConnected()) {
                    q = Graph.createQueryByBFS(g, numOfEdge);
                    break;
                }
            }
            // Graph q = g.set_BFS(numOfEdge);
            V += q.order();
            e += q.size();
            sigma += q.labels();
            Q.add(new Pair<Integer, Graph>(count, q));
            count++;
        }
        // System.out.println("Q_"+ numOfEdge + "D |V| per q " + V/querysize );
        // System.out.println("Q_" + numOfEdge + "D |E| per q " + e / querysize);
        // System.out.println("Q_"+ numOfEdge +"D d per q " +
        // String.format("%.2f",e*2/V));
        // System.out.println("Q_"+ numOfEdge +"D |Σ| per q " + sigma/querysize );
        // System.out.println();
        return Q;
    }
}