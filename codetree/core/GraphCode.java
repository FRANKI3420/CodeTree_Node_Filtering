package codetree.core;

import java.util.*;

import codetree.common.Pair;

public interface GraphCode {
        abstract List<CodeFragment> computeCanonicalCode(Graph g, int b);

        abstract List<CodeFragment> computeCanonicalCode(Graph g, int start, int limDepth);

        abstract List<CodeFragment> computeCanonicalCode_adj(Graph g, int start, int limDepth);

        abstract List<Pair<IndexNode, SearchInfo>> beginSearch(Graph g, IndexNode root);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info,
                        HashSet<Byte> childrenVlabel);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments_adj(Graph g, SearchInfo info,
                        HashSet<Byte> childrenVlabel);

        abstract List<ArrayList<CodeFragment>> computeCanonicalCode(int labels_length);

        abstract List<ArrayList<CodeFragment>> computeCanonicalCode_adj(int labels_length);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info,
                        HashSet<Byte> adjLabels, BitSet childEdgeFrag);

        abstract List<CodeFragment> computeCanonicalCode(Graph g, int b, boolean[] degreeOne);

}
