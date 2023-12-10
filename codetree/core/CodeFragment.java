package codetree.core;

public interface CodeFragment {
    public abstract byte getVlabel();

    public abstract byte[] getelabel();

    public abstract int getParent();

    public abstract boolean contains(CodeFragment other);

    public abstract boolean bigger(CodeFragment other);

    public abstract boolean equals(Object other);

}
