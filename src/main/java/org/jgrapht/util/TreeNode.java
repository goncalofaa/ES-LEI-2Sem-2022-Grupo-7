package org.jgrapht.util;



public class TreeNode<T>
{
    /**
     * A value stored in this tree node
     */
    T value;

    /**
     * Parent of this node
     */
    TreeNode<T> parent;
    /**
     * Left child of this node
     */
    TreeNode<T> left;
    /**
     * Right child of this node
     */
    TreeNode<T> right;
    /**
     * Next node in the tree according to the in order traversal
     */
    TreeNode<T> successor;
    /**
     * Previous node in the tree according to the in order traversal
     */
    TreeNode<T> predecessor;
    /**
     * A minimum node in the subtree rooted at this node
     */
    TreeNode<T> subtreeMin;
    /**
     * A maximum node in the subtree rooted at this node
     */
    TreeNode<T> subtreeMax;
    /**
     * Height of the node
     */
    int height;
    /**
     * Size of the subtree rooted at this node
     */
    int subtreeSize;

    /**
     * Constructs a new node with the {@code value} stored in it
     *
     * @param value a value to store in this node
     */
    TreeNode(T value)
    {
        this.value = value;
        reset();
    }

    /**
     * Returns a value stored in this node
     *
     * @return a value stored in this node
     */
    public T getValue()
    {
        return value;
    }

    /**
     * Returns a root of the tree this node is stored in
     *
     * @return a root of the tree this node is stored in
     */
    public TreeNode<T> getRoot()
    {
        TreeNode<T> current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current.left;
    }

    /**
     * Returns a minimum node stored in the subtree rooted at this node
     *
     * @return a minimum node stored in the subtree rooted at this node
     */
    public TreeNode<T> getSubtreeMin()
    {
        return subtreeMin;
    }

    /**
     * Returns a maximum node stored in the subtree rooted at this node
     *
     * @return a maximum node stored in the subtree rooted at this node
     */
    public TreeNode<T> getSubtreeMax()
    {
        return subtreeMax;
    }

    /**
     * Returns a minimum node stored in the tree
     *
     * @return a minimum node stored in the tree
     */
    public TreeNode<T> getTreeMin()
    {
        return getRoot().getSubtreeMin();
    }

    /**
     * Returns a maximum node stored in the tree
     *
     * @return a maximum node stored in the tree
     */
    public TreeNode<T> getTreeMax()
    {
        return getRoot().getSubtreeMax();
    }

    /**
     * Returns a parent of this node
     *
     * @return a parent of this node
     */
    public TreeNode<T> getParent()
    {
        return parent;
    }

    /**
     * Returns a left child of this node
     *
     * @return a left child of this node
     */
    public TreeNode<T> getLeft()
    {
        return left;
    }

    /**
     * Returns a right child of this node
     *
     * @return a right child of this node
     */
    public TreeNode<T> getRight()
    {
        return right;
    }

    /**
     * Returns a height of this node
     *
     * @return a height of this node
     */
    int getHeight()
    {
        return height;
    }

    /**
     * Returns a subtree size of the tree rooted at this node
     *
     * @return a subtree size of the tree rooted at this node
     */
    int getSubtreeSize()
    {
        return subtreeSize;
    }

    /**
     * Resets this node to the default state
     */
    void reset()
    {
        this.height = 1;
        this.subtreeSize = 1;
        this.subtreeMin = this;
        this.subtreeMax = this;
        this.left = this.right = this.parent = this.predecessor = this.successor = null;
    }

    /**
     * Returns a height of the right subtree
     *
     * @return a height of the right subtree
     */
    int getRightHeight()
    {
        return right == null ? 0 : right.height;
    }

    /**
     * Returns a height of the left subtree
     *
     * @return a height of the right subtree
     */
    int getLeftHeight()
    {
        return left == null ? 0 : left.height;
    }

    /**
     * Returns a size of the left subtree
     *
     * @return a size of the left subtree
     */
    int getLeftSubtreeSize()
    {
        return left == null ? 0 : left.subtreeSize;
    }

    /**
     * Returns a size of the right subtree
     *
     * @return a size of the right subtree
     */
    int getRightSubtreeSize()
    {
        return right == null ? 0 : right.subtreeSize;
    }

    /**
     * Updates the height and subtree size of this node according to the values of the left and
     * right children
     */
    void updateHeightAndSubtreeSize()
    {
        height = Math.max(getLeftHeight(), getRightHeight()) + 1;
        subtreeSize = getLeftSubtreeSize() + getRightSubtreeSize() + 1;
    }

    /**
     * Returns {@code true} if this node is unbalanced and the left child's height is greater,
     * {@code false otherwise}
     *
     * @return {@code true} if this node is unbalanced and the left child's height is greater,
     *         {@code false otherwise}
     */
    boolean isLeftDoubleHeavy()
    {
        return getLeftHeight() > getRightHeight() + 1;
    }

    /**
     * Returns {@code true} if this node is unbalanced and the right child's height is greater,
     * {@code false otherwise}
     *
     * @return {@code true} if this node is unbalanced and the right child's height is greater,
     *         {@code false otherwise}
     */
    boolean isRightDoubleHeavy()
    {
        return getRightHeight() > getLeftHeight() + 1;
    }

    /**
     * Returns {@code true} if the height of the left child is greater than the height of the
     * right child
     *
     * @return {@code true} if the height of the left child is greater than the height of the
     *         right child
     */
    boolean isLeftHeavy()
    {
        return getLeftHeight() > getRightHeight();
    }

    /**
     * Returns {@code true} if the height of the right child is greater than the height of the
     * left child
     *
     * @return {@code true} if the height of the right child is greater than the height of the
     *         left child
     */
    boolean isRightHeavy()
    {
        return getRightHeight() > getLeftHeight();
    }

    /**
     * Returns {@code true} if this node is a left child of its parent, {@code false} otherwise
     *
     * @return {@code true} if this node is a left child of its parent, {@code false} otherwise
     */
    boolean isLeftChild()
    {
        return this == parent.left;
    }

    /**
     * Returns {@code true} if this node is a right child of its parent, {@code false} otherwise
     *
     * @return {@code true} if this node is a right child of its parent, {@code false} otherwise
     */
    boolean isRightChild()
    {
        return this == parent.right;
    }

    /**
     * Returns a successor of this node according to the tree in order traversal, or
     * {@code null} if this node is a maximum node in the tree
     *
     * @return successor of this node, or {@code} null if this node in a maximum node in the
     *         tree
     */
    public TreeNode<T> getSuccessor()
    {
        return successor;
    }

    /**
     * Returns a predecessor of this node according to the tree in order traversal, or
     * {@code null} if this node is a minimum node in the tree
     *
     * @return predecessor of this node, or {@code} null if this node in a minimum node in the
     *         tree
     */
    public TreeNode<T> getPredecessor()
    {
        return predecessor;
    }

    /**
     * Updates the successor reference of this node. If the {@code node} is not {@code null},
     * updates its predecessor reference as well
     *
     * @param node new successor
     */
    void setSuccessor(TreeNode<T> node)
    {
        successor = node;
        if (node != null) {
            node.predecessor = this;
        }
    }

    /**
     * Updates the predecessor reference of this node. If the {@code node} is not {@code null},
     * updates its successor reference as well
     *
     * @param node new predecessor
     */
    void setPredecessor(TreeNode<T> node)
    {
        predecessor = node;
        if (node != null) {
            node.successor = this;
        }
    }

    /**
     * Sets the left child reference of this node to {@code node}. If the {@code node} is not
     * {@code null}, updates its parent reference as well.
     *
     * @param node a new left child
     */
    void setLeftChild(TreeNode<T> node)
    {
        left = node;
        if (node != null) {
            node.parent = this;
            setPredecessor(node.subtreeMax);
            subtreeMin = node.subtreeMin;
        } else {
            subtreeMin = this;
            predecessor = null;
        }
    }

    /**
     * Sets the right child reference of this node to {@code node}. If the {@code node} is not
     * {@code null}, updates its parent reference as well.
     *
     * @param node a new right child
     */
    void setRightChild(TreeNode<T> node)
    {
        right = node;
        if (node != null) {
            node.parent = this;
            setSuccessor(node.subtreeMin);
            subtreeMax = node.subtreeMax;
        } else {
            successor = null;
            subtreeMax = this;
        }
    }

    /**
     * Substitutes the {@code prevChild} with the {@code newChild}. If the {@code newChild} is
     * not {@code null}, updates its parent reference as well
     *
     * @param prevChild either left or right child of this node
     * @param newChild a new child of this node
     */
    void substituteChild(TreeNode<T> prevChild, TreeNode<T> newChild)
    {
        assert left == prevChild || right == prevChild;
        assert !(left == prevChild && right == prevChild);
        if (left == prevChild) {
            setLeftChild(newChild);
        } else {
            setRightChild(newChild);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String
            .format(
                "{%s}: [parent = %s, left = %s, right = %s], [subtreeMin = %s, subtreeMax = %s], [predecessor = %s, successor = %s], [height = %d, subtreeSize = %d]",
                value, parent == null ? "null" : parent.value,
                left == null ? "null" : left.value, right == null ? "null" : right.value,
                subtreeMin == null ? "null" : subtreeMin.value,
                subtreeMax == null ? "null" : subtreeMax.value,
                predecessor == null ? "null" : predecessor.value,
                successor == null ? "null" : successor.value, height, subtreeSize);
    }
}

