package codetree.common;

import java.util.*;

// import codetree.core.Graph;

public class VertexLabel {
    static List<String> atoms = Arrays.asList(new String[] { "H", "C", "O", "N", "S", "Cl", "F", "P", "Br", "I", "Si",
            "Na", "B", "Cu", "Sn", "Co", "Fe", "Se", "Ni",
            "Pt", "Ru", "Mo", "Pd", "As", "Mn", "Rh", "Zn", "Ge", "K", "Cr", "Ir", "Ga", "Hg", "W", "Re", "Pb", "Bi",
            "Te", "Au",
            "Sb", "Li", "Tl", "Mg", "Ti", "Ag", "Al", "Zr", "U", "Ca", "V", "Gd", "Ac", "Nb", "Er", "Yb", "Sm", "Nd",
            "Pr", "Os",
            "Cd", "Cs", "Tb", "Ho" });

    // static List<String> atoms = Arrays.asList(new String[] { "Ho", "Tb", "Cs",
    // "Cd", "Os", "Pr", "Nd", "Sm", "Yb", "Er",
    // "Nb", "Ac", "Gd", "V", "Ca", "U", "Zr", "Al",
    // "Ag", "Ti", "Mg", "Tl", "Li", "Sb", "Au", "Te", "Bi", "Pb", "Re", "W", "Hg",
    // "Ga", "Ir", "Cr", "K", "Ge",
    // "Zn", "Rh",
    // "Mn", "As", "Pd", "Mo", "Ru", "Pt", "Ni", "Se", "Fe", "Co", "Sn", "Cu", "B",
    // "Na", "Si", "I", "Br", "P",
    // "F",
    // "Cl", "S", "N", "O", "C", "H" });

    public static int[] labels = new int[1];
    static int[] orderlabels = new int[1];
    static int numOflabels;
    static int[] reverse = new int[128];

    public static String id2string(int id) {
        if (id >= atoms.size()) {
            throw new IllegalArgumentException("Undefined label id.");
        }

        return atoms.get(id);
    }

    // sが何文字目か？を返す
    public static byte string2id(String s) {
        return (byte) atoms.indexOf(s);
    }

    // 頻出ラベルに大きな頂点ラベルを割り当てなおす
    // public static void orderLabel(List<Graph> G) {
    // numOflabels = Graph.numOflabels(G);
    // labels = new int[numOflabels];
    // orderlabels = new int[numOflabels];

    // for (int i = 0; i < G.size(); i++) {
    // Graph g = G.get(i);
    // for (int j = 0; j < g.order; j++) {
    // labels[g.vertices[j]]++;
    // }
    // }

    // // for(int i=0;i<labels.length;i++){
    // // System.out.println(i+ " "+labels[i]);
    // // }

    // System.arraycopy(labels, 0, orderlabels, 0, numOflabels);
    // Arrays.sort(orderlabels);// 昇順

    // }

    // public static void relabel(Graph g) {
    // int vlabel = 127 - numOflabels;

    // for (int i = 0; i < numOflabels; i++, vlabel++) {
    // int num = orderlabels[i];
    // for (int j = 0; j < numOflabels; j++) {
    // if (num == labels[j]) {
    // reverse[vlabel] = j;
    // for (int l = 0; l < g.order(); l++) {
    // if (g.vertices[l] == j) {
    // g.vertices[l] = (byte) vlabel;
    // }
    // }
    // }
    // }
    // }
    // }

    // public static int reverse(byte vlabel) {
    // return reverse[vlabel];
    // }
}
