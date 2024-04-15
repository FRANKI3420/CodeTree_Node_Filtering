package codetree.vertexBased;

import java.util.*;

import java.io.Serializable;

import codetree.core.*;

class AcgmCodeFragment
        implements CodeFragment, Serializable {
    final byte vLabel;
    final byte[] eLabels;

    AcgmCodeFragment(byte vLabel, int length) {
        this.vLabel = vLabel;
        eLabels = new byte[length];
    }

    AcgmCodeFragment(byte vLabel, byte[] eLabels) {
        this.vLabel = vLabel;
        this.eLabels = eLabels.clone();
    }

    AcgmCodeFragment(byte vLabel) {
        this.vLabel = vLabel;
        this.eLabels = null;
    }

    int isMoreCanonicalThan(AcgmCodeFragment other) {
        final int res = vLabel - other.vLabel;
        return res != 0 ? res : Arrays.compare(eLabels, other.eLabels);
    }

    @Override
    public boolean equals(Object other0) {
        AcgmCodeFragment other = (AcgmCodeFragment) other0;
        return vLabel == other.vLabel && Arrays.equals(eLabels, other.eLabels);
    }

    // @Override
    // public boolean equals(Object other0) {

    // AcgmCodeFragment other = (AcgmCodeFragment) other0;

    // if (vLabel != other.vLabel)
    // return false;

    // final int len = eLabels.length;
    // for (int i = 0; i < len; i++) {
    // if (eLabels[i] != other.eLabels[i])
    // return false;
    // }
    // return true;
    // }

    @Override
    public boolean contains(CodeFragment other0) {
        AcgmCodeFragment other = (AcgmCodeFragment) other0;

        final int len = eLabels.length;
        if (len != other.eLabels.length) {
            throw new IllegalArgumentException("Compareing incompatible fragments.");
        }

        if (vLabel != other.vLabel) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            if (other.eLabels[i] > 0 && eLabels[i] != other.eLabels[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean bigger(CodeFragment other0) {
        AcgmCodeFragment other = (AcgmCodeFragment) other0;

        final int len = eLabels.length;
        if (len != other.eLabels.length) {
            throw new IllegalArgumentException("Compareing incompatible fragments.");
        }

        if (vLabel != other.vLabel) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            if (eLabels[i] == 0 && other.eLabels[i] > 0) {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean contains_adj(CodeFragment other0) {
        AcgmCodeFragment other = (AcgmCodeFragment) other0;
        // final int len = eLabels.length;
        // if (len != other.eLabels.length) {
        // throw new IllegalArgumentException("Compareing incompatible fragments.");
        // }

        if (vLabel != other.vLabel) {
            return false;
        }

        return true;

    }

    // @Override
    // public boolean contains1(CodeFragment other0) {
    // AcgmCodeFragment other = (AcgmCodeFragment) other0;

    // final int len = eLabels.length;
    // if (len != other.eLabels.length) {
    // throw new IllegalArgumentException("Compareing incompatible fragments.");
    // }

    // if (vLabel != other.vLabel) {
    // return false;
    // }

    // for (int i = 0; i < len; ++i) {
    // if (eLabels[i] != other.eLabels[i]) {
    // return false;
    // }
    // }

    // return true;
    // }

    // @Override
    // public String toString()
    // {
    // String s = VertexLabel.id2string(vLabel);
    // for (int i = 0; i < eLabels.length; ++i) {
    // s += String.valueOf(eLabels[i]);
    // }

    // return s;
    // }

    @Override
    public byte getVlabel() {
        return this.vLabel;
    }

    @Override
    public byte[] getelabel() {
        return this.eLabels;
    }

}
