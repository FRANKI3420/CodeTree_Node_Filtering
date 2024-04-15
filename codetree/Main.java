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
    private static String gfuFilename;
    private static String q_gfuFilename;
    private static String dataset;
    private static GraphCode graphCode;
    private static int graphCodeID = 1;
    private static int searchID = 1;
    private static int datasetID = 0;

    public static void main(String[] args) throws InterruptedException {
        if (searchID < 1 && searchID > 2) {
            System.out.println("無効なグラフコードです。");
            System.exit(0);
        }

        if (searchID == 1) {
            long max = Runtime.getRuntime().maxMemory();

            System.out.println("max: " + max / 1024 / 1024 / 1024);
            String allindex = String.format("result/all_index.csv", dataset);
            String wholeresult = String.format("result/all_result.csv", dataset);
            Path writeindex = Paths.get(allindex);
            Path writewhole = Paths.get(wholeresult);
            try (BufferedWriter allfind = Files.newBufferedWriter(writeindex);
                    BufferedWriter br_whole = Files.newBufferedWriter(writewhole)) {
                allfind.write(
                        "dataset,depth,addPathtoTree(s),Tree_size,Tree_size(new),removeTime(s),addIDtoTree(s),Build_tree(s),memory cost\n");

                for (datasetID = 4; datasetID <= 5; datasetID++) {
                    if (datasetID == 2 || datasetID == 3)
                        continue;
                    parseArgs(args);

                    br_whole.write(
                            "dataset,query_set,A/C,(G-C)/(G-A),SP,filtering_time(ms),verification_time(ms),query_time(ms),tree1_search_time(ms),node_fil_time(ms),|In(Q)|,|A(Q)|,|Can(Q)|,|F(Q)|,Num deleted Vertices,total deleted edges Num,codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail,verify num,q_trav_num\n");

                    if (datasetID < 0 || datasetID > 6) {
                        System.out.println("無効なデータセットIDです");
                        System.exit(0);
                    }
                    int times = 5;// 分割回数
                    int starts = 0;// 0にすると無ラベルグラフ化
                    for (int t = starts; t <= 0; t++) {// <=で従来のデータセットまで行う
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

                        int numOflabels = Graph.numOflabels(G);
                        if (t == starts) {
                            HashMap<Integer, Integer> labelDistribution = Graph.labelDistribution(G, numOflabels);
                            System.out.println("頂点ラベルの分布" + labelDistribution.toString());
                            System.out.println("before");
                            dataset_search(G);
                        }

                        String resultDirectory;
                        int numLabel = 0;
                        String queryDirectory;
                        String datasetFileName;
                        if (t == times) {
                            numLabel = numOflabels;
                            resultDirectory = String.format("%s_result/%d", dataset, numLabel);
                            queryDirectory = "Query";
                            datasetFileName = gfuFilename;
                        } else {
                            numLabel = numOflabels / times * t;
                            labelDecrease(G, Q, numLabel);

                            System.out.println("aftter");
                            dataset_search(G);

                            writeDataset2File(G);
                            writeQuery2Files(Q);
                            queryDirectory = "Query_labelchange";

                            resultDirectory = String.format("%s_result/%d", dataset, numLabel);
                            datasetFileName = dataset + "labelchange.gfu";
                        }
                        Path resultDirectories = Paths.get(resultDirectory);
                        Files.createDirectories(resultDirectories);

                        String output = String.format("%s/%s_output.txt", resultDirectory, dataset);
                        Path out = Paths.get(output);

                        String allresult = String.format("%s/%s_result.csv",
                                resultDirectory, dataset);
                        Path all = Paths.get(allresult);

                        String allindex2 = String.format("%s/%s_index.csv",
                                resultDirectory, dataset);
                        Path writeindex2 = Paths.get(allindex2);

                        try (BufferedWriter bw2 = Files.newBufferedWriter(out);
                                BufferedWriter allbw = Files.newBufferedWriter(all);
                                BufferedWriter allfind2 = Files.newBufferedWriter(writeindex2)) {
                            allfind2.write(
                                    "numOflavels,dataset,depth,addPathtoTree(s),Tree_size,Tree_size(new),removeTime(s),addIDtoTree(s),Build_tree(s),memory cost\n");
                            allfind2.write(numLabel + ",");
                            allbw.write(
                                    "dataset,query_set,A/C,(G-C)/(G-A),SP,filtering_time(ms),verification_time(ms),query_time(ms),tree1_search_time(ms),node_fil_time(ms),|In(Q)|,|A(Q)|,|Can(Q)|,|F(Q)|,Num deleted Vertices,total deleted edges Num,codetree_filtime/fil_num,codetree_fil_num,allfil_num/allfil_time,allfil_num,nonfail,verify num,q_trav_num\n");

                            System.out.println(" ");
                            String resultFilename = String.format("%s/%s_result.txt",
                                    resultDirectory, dataset);

                            Path res = Paths.get(resultFilename);
                            try (BufferedWriter bw = Files.newBufferedWriter(res)) {

                                long start = System.nanoTime();

                                System.out.println("tree");
                                CodeTree tree = new CodeTree(graphCode, G, bw, dataset, allfind2);

                                String codetree = String.format("data_structure/%s.ser",
                                        dataset);
                                File file = new File(codetree);
                                long fileSize = file.length();
                                System.out.println(
                                        "File size: " + String.format("%.2f", (double) fileSize / 1024 / 1024) + " MB");
                                allfind2.write(String.format("%.2f", (double) fileSize / 1024 / 1024) + "\n");

                                allfind2.flush();

                                HashMap<Integer, ArrayList<String>> gMaps = makeGmaps(datasetFileName);

                                int index = minedge;
                                String mode = null;
                                String data_out = null;
                                int[] adjust = new int[Q.size()];
                                int count = 0;
                                int count2 = 0;

                                for (ArrayList<Pair<Integer, Graph>> Q_set : Q) {

                                    adjust[count++] = index;

                                    if (index <= maxedge) {
                                        System.out.println("\nQ" + index + "R");
                                        bw.write("Q" + index + "R\n");
                                        bw2.write("Q" + index + "R\n");
                                        allbw.write(dataset + ",Q" + index + "R,");
                                        br_whole.write(dataset + ",Q" + index + "R,");
                                        data_out = String.format("%s/%s_%dR_data.csv", resultDirectory, dataset,
                                                index);
                                        mode = "randomwalk";
                                    } else {
                                        int size = adjust[count2++];

                                        System.out.println("\nQ" + size + "B");
                                        bw.write("Q" + size + "B\n");
                                        bw2.write("Q" + size + "B\n");
                                        allbw.write(dataset + ",Q" + size + "B,");
                                        br_whole.write(dataset + ",Q" + size + "B,");
                                        data_out = String.format("%s/%s_%dB_data.csv", resultDirectory, dataset,
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
                                            BitSet result = tree.subgraphSearch(q.right, bw, mode,
                                                    dataset,
                                                    bwout, allbw, G, gMaps, br_whole, queryDirectory);

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
                            }

                        } catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }

        } else if (searchID == 2) {
            System.out.println("スーパーグラフ検索を開始します");

            List<Graph> G = SdfFileReader.readFile(Paths.get(sdfFilename));

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

    // 非頻出ラベルからdecrease_num種類になるまで統合
    private static void labelDecrease(List<Graph> G, List<ArrayList<Pair<Integer, Graph>>> Q, int decrease_num) {
        if (decrease_num == 0) {
            for (Graph g : G) {
                for (int j = 0; j < g.vertices.length; j++) {
                    g.vertices[j] = 0;

                }
            }
            for (ArrayList<Pair<Integer, Graph>> Qset : Q) {
                for (Pair<Integer, Graph> p : Qset) {
                    for (int j = 0; j < p.right.vertices.length; j++) {
                        p.right.vertices[j] = 0;
                    }
                }
            }
        } else {
            int targetLabel = Graph.numOflabels(G);
            while (targetLabel != decrease_num) {
                targetLabel--;
                for (Graph g : G) {
                    for (int j = 0; j < g.vertices.length; j++) {
                        if (g.vertices[j] == targetLabel) {
                            g.vertices[j] = (byte) (targetLabel - 1);
                        }
                    }
                }
                for (ArrayList<Pair<Integer, Graph>> Qset : Q) {
                    for (Pair<Integer, Graph> p : Qset) {
                        for (int j = 0; j < p.right.vertices.length; j++) {
                            if (p.right.vertices[j] == targetLabel) {
                                p.right.vertices[j] = (byte) (targetLabel - 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void writeDataset2File(List<Graph> G) throws IOException {
        String filename = dataset + "labelchange.gfu";
        Path write = Paths.get(filename);
        try (BufferedWriter bw = Files.newBufferedWriter(write)) {
            for (Graph g : G) {
                g.writeGraph2Gfu(bw);
            }
        }
    }

    private static void writeQuery2Files(List<ArrayList<Pair<Integer, Graph>>> Q) throws IOException {

        int count = 0;
        String mode;
        for (ArrayList<Pair<Integer, Graph>> Qset : Q) {
            if (count < 5) {
                mode = "randomwalk";
            } else {
                mode = "bfs";
            }
            for (Pair<Integer, Graph> p : Qset) {
                Path directory = Paths
                        .get(String.format("Query_labelchange/%s/%s/%d", dataset, mode, p.right.size));
                Files.createDirectories(directory);
                String path = String.format("Query_labelchange/%s/%s/%d/q%d.gfu", dataset, mode, p.right.size,
                        p.right.id);
                Path write = Paths.get(path);
                try (BufferedWriter bw = Files.newBufferedWriter(write)) {
                    p.right.writeGraph2Gfu(bw);
                }
            }
            count++;
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
            dataset = "pdbs";
            System.out.println("PDBS");
        } else if (datasetID == 2) {
            gfuFilename = "pcms.gfu";
            dataset = "pcms";
            System.out.println("PCM");
        } else if (datasetID == 3) {
            gfuFilename = "ppigo.gfu";
            dataset = "ppigo";
            System.out.println("PPI");
        } else if (datasetID == 4) {
            gfuFilename = "IMDB-MULTI.gfu";
            dataset = "IMDB-MULTI";
            System.out.println("IMDB");
        } else if (datasetID == 5) {
            gfuFilename = "REDDIT-MULTI-5K.gfu";
            dataset = "REDDIT-MULTI-5K";
            System.out.println("REDDIT");
        } else if (datasetID == 6) {
            gfuFilename = "COLLAB.gfu";
            dataset = "COLLAB";
            System.out.println("COLLAB");
        } else if (datasetID == 0) {
            gfuFilename = "AIDS.gfu";
            dataset = "AIDS";
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
        double degree = 0;

        for (Pair<Integer, Graph> q : Q) {
            Graph g = q.right;
            V += g.order();
            e += g.size();
            sigma += g.labels();
            double deg = 0;
            for (int i = 0; i < g.order; i++) {
                deg += g.adjList[i].length;
            }
            deg /= g.order;
            degree += deg;
        }
        // System.out.println("Q_" + numOfEdge + mode + " |V| per q " + V / Q.size());
        // System.out.println("Q_" + numOfEdge + mode + " |E| per q " + e / Q.size());
        System.out.println("Q_" + numOfEdge + mode + " d per q " + String.format("%.2f", degree / Q.size()));
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
        // System.out.println("|G| " + G.size());
        System.out.println("labels " + Graph.numOflabels(G));
        // System.out.println("|V| per g " + V / G.size());
        // System.out.println("|E| per g " + e / G.size());
        // System.out.println(" d per g " + String.format("%.2f", d / G.size()));
        System.out.println("|Σ| per g " + sigma / G.size());
        // System.out.println("最大頂点数 " + Graph.max_vertice(G));
        // System.out.println("最大辺数 " + Graph.max_size(G));
        System.out.println();
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