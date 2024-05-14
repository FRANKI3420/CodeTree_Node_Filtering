package codetree.core;

import java.util.*;

import codetree.common.Pair;

public interface GraphCode {
        abstract List<CodeFragment> computeCanonicalCode(Graph g, int b);

        abstract List<CodeFragment> computeCanonicalCode(Graph g, int start, int limDepth);

        abstract List<CodeFragment> computeCode(Graph g, int start, int limDepth);

        abstract List<Pair<IndexNode, SearchInfo>> beginSearch(Graph g, IndexNode root);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info,
                        HashSet<Byte> childrenVlabel);

        abstract List<ArrayList<CodeFragment>> computeCanonicalCode(int labels_length);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments(Graph g, SearchInfo info,
                        HashSet<Byte> adjLabels, BitSet childEdgeFrag);

        abstract List<Pair<CodeFragment, SearchInfo>> enumerateFollowableFragments_adj2(Graph g, SearchInfo info0,
                        HashSet<Byte> childrenVlabel, BitSet childEdgeFrag, int index);

}
