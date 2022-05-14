/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.util;

import java.util.*;

/**
 * Implementation of the <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL tree</a> data
 * structure. <b>Note:</b> this tree doesn't use key comparisons, so this tree can't be used as a
 * binary search tree. This implies that the same key can be added to this tree multiple times.
 * <p>
 * AVL tree is a self-balancing binary tree data structure. In an AVL tree, the heights of two child
 * subtrees differ by at most one. This ensures that the height of the tree is $\mathcal{O}(\log n)$
 * where $n$ is the number of elements in the tree. Also this tree doesn't support key comparisons,
 * it does define an element order. As a result, this tree can be used to query node
 * successor/predecessor.
 * <p>
 * Subtree query means that the result is being computed only on the subtree nodes. This tree
 * supports the following operations:
 * <ul>
 * <li>Min/max insertion and deletion in $\mathcal{O}(\log n)$ time</li>
 * <li>Subtree min/max queries in $\mathcal{O}(1)$ time</li>
 * <li>Node successor/predecessor queries in $\mathcal{O}(1)$ time</li>
 * <li>Tree split in $\mathcal{O}(\log n)$ time</li>
 * <li>Tree merge in $\mathcal{O}(\log n)$ time</li>
 * </ul>
 * <p>
 * This implementation gives users access to the tree nodes which hold the inserted elements. The
 * user is able to store the tree nodes references but isn't able to modify them.
 *
 * @param <T> the key data type
 * @author Timofey Chudakov
 */
public class AVLTree<T>
    implements
    Iterable<T>
{
    private AVLTreeProduct aVLTreeProduct = new AVLTreeProduct();
	/**
     * An auxiliary node which's always present in a tree and doesn't contain any data.
     */
    private TreeNode<T> virtualRoot = new TreeNode<>(null);
    /**
     * Modification tracker
     */
    private int modCount = 0;

    /**
     * Constructs an empty tree
     */
    public AVLTree()
    {
    }

    /**
     * Constructor for internal usage
     *
     * @param root the root of the newly create tree
     */
    private AVLTree(TreeNode<T> root)
    {
        makeRoot(root);
    }

    /**
     * Adds {@code value} as a maximum element to this tree. The running time of this method is
     * $\mathcal{O}(\log n)$
     *
     * @param value a value to add as a tree max
     * @return a tree node holding the {@code value}
     */
    public TreeNode<T> addMax(T value)
    {
        TreeNode<T> newMax = new TreeNode<>(value);
        addMaxNode(newMax);
        return newMax;
    }

    /**
     * Adds the {@code newMax} as a maximum node to this tree.
     *
     * @param newMax a node to add as a tree max
     */
    public void addMaxNode(TreeNode<T> newMax)
    {
        registerModification();

        if (isEmpty()) {
            virtualRoot.left = newMax;
            newMax.parent = virtualRoot;
        } else {
            TreeNode<T> max = getMax();
            max.setRightChild(newMax);
            balance(max);
        }
    }

    /**
     * Adds the {@code value} as a minimum element to this tree
     *
     * @param value a value to add as a tree min
     * @return a tree node holding the {@code value}
     */
    public TreeNode<T> addMin(T value)
    {
        TreeNode<T> newMin = new TreeNode<>(value);
        addMinNode(newMin);
        return newMin;
    }

    /**
     * Adds the {@code newMin} as a minimum node to this tree
     *
     * @param newMin a node to add as a tree min
     */
    public void addMinNode(TreeNode<T> newMin)
    {
        registerModification();
        if (isEmpty()) {
            virtualRoot.left = newMin;
            newMin.parent = virtualRoot;
        } else {
            TreeNode<T> min = getMin();
            min.setLeftChild(newMin);
            balance(min);
        }
    }

    /**
     * Splits the tree into two parts.
     * <p>
     * The first part contains the nodes which are smaller than or equal to the {@code node}. The
     * first part stays in this tree. The second part contains the nodes which are strictly greater
     * than the {@code node}. The second part is returned as a tree.
     *
     * @param node a separating node
     * @return a tree containing the nodes which are strictly greater than the {@code node}
     */
    public AVLTree<T> splitAfter(TreeNode<T> node)
    {
        registerModification();

        TreeNode<T> parent = node.parent;
        boolean nextMove = node.isLeftChild();
        TreeNode<T> left = node.left;
        TreeNode<T> right = node.right;

        node.parent.substituteChild(node, null);

        node.reset();

        if (left != null) {
            left.parent = null;
        }
        if (right != null) {
            right.parent = null;
        }

        if (left == null) {
            left = node;
        } else {
            // insert node as a left subtree max
            TreeNode<T> t = left;
            while (t.right != null) {
                t = t.right;
            }
            t.setRightChild(node);

            while (t != left) {
                TreeNode<T> p = t.parent;
                p.substituteChild(t, AVLTreeProduct.balanceNode(t));
                t = p;
            }
            left = AVLTreeProduct.balanceNode(left);

        }
        return split(left, right, parent, nextMove);
    }

    /**
     * Splits the tree into two parts.
     * <p>
     * The first part contains the nodes which are smaller than the {@code node}. The first part
     * stays in this tree. The second part contains the nodes which are greater than or equal to the
     * {@code node}. The second part is returned as a tree.
     *
     * @param node a separating node
     * @return a tree containing the nodes which are greater than or equal to the {@code node}
     */
    public AVLTree<T> splitBefore(TreeNode<T> node)
    {
        registerModification();

        TreeNode<T> predecessor = predecessor(node);
        if (predecessor == null) {
            // node is a minimum node
            AVLTree<T> tree = new AVLTree<>();
            swap(tree);
            return tree;
        }
        return splitAfter(predecessor);
    }

    /**
     * Append the nodes in the {@code tree} after the nodes in this tree.
     * <p>
     * The result of this operation is stored in this tree.
     *
     * @param tree a tree to append
     */
    public void mergeAfter(AVLTree<T> tree)
    {
        registerModification();
        if (tree.isEmpty()) {
            return;
        } else if (tree.getSize() == 1) {
            addMaxNode(tree.removeMin());
            return;
        }

        TreeNode<T> junctionNode = tree.removeMin();
        TreeNode<T> treeRoot = tree.getRoot();
        tree.clear();

        makeRoot(merge(junctionNode, getRoot(), treeRoot));
    }

    /**
     * Prepends the nodes in the {@code tree} before the nodes in this tree.
     * <p>
     * The result of this operation is stored in this tree.
     *
     * @param tree a tree to prepend
     */
    public void mergeBefore(AVLTree<T> tree)
    {
        registerModification();

        tree.mergeAfter(this);

        swap(tree);
    }

    /**
     * Removes the minimum node in this tree. Returns {@code null} if this tree is empty
     *
     * @return the removed node or {@code null} if this tree is empty
     */
    public TreeNode<T> removeMin()
    {
        registerModification();

        if (isEmpty()) {
            return null;
        }
        TreeNode<T> min = getMin();
        // min.parent != null
        if (min.parent == virtualRoot) {
            makeRoot(min.right);
        } else {
            min.parent.setLeftChild(min.right);
        }

        balance(min.parent);

        return min;
    }

    /**
     * Removes the maximum node in this tree. Returns {@code null} if this tree is empty
     *
     * @return the removed node or {@code null} if this tree is empty
     */
    public TreeNode<T> removeMax()
    {
        registerModification();
        if (isEmpty()) {
            return null;
        }
        TreeNode<T> max = getMax();
        if (max.parent == virtualRoot) {
            makeRoot(max.left);
        } else {
            max.parent.setRightChild(max.left);
        }
        balance(max.parent);
        return max;
    }

    /**
     * Returns the root of this tree or null if this tree is empty.
     *
     * @return the root of this tree or null if this tree is empty.
     */
    public TreeNode<T> getRoot()
    {
        return virtualRoot.left;
    }

    /**
     * Returns the node following the {@code node} in the order defined by this tree. Returns null
     * if the {@code node} is the maximum node in the tree.
     *
     * @param node a node to compute successor of
     * @return the successor of the {@code node}
     */
    public TreeNode<T> successor(TreeNode<T> node)
    {
        return node.successor;
    }

    /**
     * Returns the node, which is before the {@code node} in the order defined by this tree. Returns
     * null if the {@code node} is the minimum node in the tree.
     *
     * @param node a node to compute predecessor of
     * @return the predecessor of the {@code node}
     */
    public TreeNode<T> predecessor(TreeNode<T> node)
    {
        return node.predecessor;
    }

    /**
     * Returns the minimum node in this tree or null if the tree is empty.
     *
     * @return the minimum node in this tree or null if the tree is empty.
     */
    public TreeNode<T> getMin()
    {
        return getRoot() == null ? null : getRoot().getSubtreeMin();
    }

    /**
     * Returns the maximum node in this tree or null if the tree is empty.
     *
     * @return the maximum node in this tree or null if the tree is empty.
     */
    public TreeNode<T> getMax()
    {
        return getRoot() == null ? null : getRoot().getSubtreeMax();
    }

    /**
     * Check if this tree is empty
     *
     * @return {@code true} if this tree is empty, {@code false otherwise}
     */
    public boolean isEmpty()
    {
        return getRoot() == null;
    }

    /**
     * Removes all nodes from this tree.
     * <p>
     * <b>Note:</b> the memory allocated for the tree structure won't be deallocated until there are
     * active external referenced to the nodes of this tree.
     */
    public void clear()
    {
        registerModification();

        virtualRoot.left = null;
    }

    /**
     * Returns the size of this tree
     *
     * @return the size of this tree
     */
    public int getSize()
    {
        return virtualRoot.left == null ? 0 : virtualRoot.left.subtreeSize;
    }

    /**
     * Makes the {@code node} the root of this tree
     *
     * @param node a new root of this tree
     */
    private void makeRoot(TreeNode<T> node)
    {
        virtualRoot.left = node;
        if (node != null) {
            node.subtreeMax.successor = null;
            node.subtreeMin.predecessor = null;
            node.parent = virtualRoot;
        }
    }

    /**
     * Traverses the tree up until the virtual root and splits it into two parts.
     * <p>
     * The algorithm is described in <i>Donald E. Knuth. The art of computer programming. Second
     * Edition. Volume 3 / Sorting and Searching, p. 474</i>.
     *
     * @param left a left subtree
     * @param right a right subtree
     * @param p next parent node
     * @param leftMove {@code true} if we're moving from the left child, {@code false} otherwise.
     * @return the resulting right tree
     */
    private AVLTree<T> split(TreeNode<T> left, TreeNode<T> right, TreeNode<T> p, boolean leftMove)
    {
        while (p != virtualRoot) {
            boolean nextMove = p.isLeftChild();
            TreeNode<T> nextP = p.parent;

            p.parent.substituteChild(p, null);
            p.parent = null;

            if (leftMove) {
                right = merge(p, right, p.right);
            } else {
                left = merge(p, p.left, left);
            }
            p = nextP;
            leftMove = nextMove;
        }

        makeRoot(left);

        return new AVLTree<>(right);
    }

    /**
     * Merges the {@code left} and {@code right} subtrees using the {@code junctionNode}.
     * <p>
     * The algorithm is described in <i>Donald E. Knuth. The art of computer programming. Second
     * Edition. Volume 3 / Sorting and Searching, p. 474</i>.
     *
     * @param junctionNode a node between left and right subtrees
     * @param left a left subtree
     * @param right a right subtree
     * @return the root of the resulting tree
     */
    private TreeNode<T> merge(TreeNode<T> junctionNode, TreeNode<T> left, TreeNode<T> right)
    {
        if (left == null && right == null) {
            junctionNode.reset();
            return junctionNode;
        } else if (left == null) {
            right.setLeftChild(merge(junctionNode, left, right.left));
            return AVLTreeProduct.balanceNode(right);
        } else if (right == null) {
            left.setRightChild(merge(junctionNode, left.right, right));
            return AVLTreeProduct.balanceNode(left);
        } else if (left.getHeight() > right.getHeight() + 1) {
            left.setRightChild(merge(junctionNode, left.right, right));
            return AVLTreeProduct.balanceNode(left);
        } else if (right.getHeight() > left.getHeight() + 1) {
            right.setLeftChild(merge(junctionNode, left, right.left));
            return AVLTreeProduct.balanceNode(right);
        } else {
            junctionNode.setLeftChild(left);
            junctionNode.setRightChild(right);
            return AVLTreeProduct.balanceNode(junctionNode);
        }
    }

    /**
     * Swaps the contents of this tree and the {@code tree}
     *
     * @param tree a tree to swap content of
     */
    private void swap(AVLTree<T> tree)
    {
        TreeNode<T> t = virtualRoot.left;
        makeRoot(tree.virtualRoot.left);
        tree.makeRoot(t);
    }

    /**
     * Performs a node balancing on the path from {@code node} up until the root
     *
     * @param node a node to start tree balancing from
     */
    private void balance(TreeNode<T> node)
    {
        balance(node, virtualRoot);
    }

    /**
     * Performs a node balancing on the path from {@code node} up until the {@code stop} node
     *
     * @param node a node to start tree balancing from
     * @param stop a node to stop balancing at (this node is not being balanced)
     */
    private void balance(TreeNode<T> node, TreeNode<T> stop)
    {
        if (node == stop) {
            return;
        }
        TreeNode<T> p = node.parent;
        if (p == virtualRoot) {
            makeRoot(AVLTreeProduct.balanceNode(node));
        } else {
            p.substituteChild(node, AVLTreeProduct.balanceNode(node));
        }

        balance(p, stop);
    }

    /**
     * Registers a modifying operation
     */
    private void registerModification()
    {
        ++modCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (Iterator<TreeNode<T>> i = nodeIterator(); i.hasNext();) {
            TreeNode<T> node = i.next();
            builder.append(node.toString()).append("\n");
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator()
    {
        return new TreeValuesIterator();
    }

    /**
     * Returns an iterator over the tree nodes rather than the node values. The tree are returned in
     * the same order as the tree values.
     *
     * @return an iterator over the tree nodes
     */
    public Iterator<TreeNode<T>> nodeIterator()
    {
        return new TreeNodeIterator();
    }

    /**
     * Iterator over the values stored in this tree. This implementation uses the
     * {@code TreeNodeIterator} to iterator over the values.
     */
    private class TreeValuesIterator
        implements
        Iterator<T>
    {
        /**
         * Internally used {@code TreeNodeIterator}
         */
        private TreeNodeIterator iterator;

        /**
         * Constructs a new {@code TreeValuesIterator}
         */
        public TreeValuesIterator()
        {
            iterator = new TreeNodeIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T next()
        {
            return iterator.next().getValue();
        }
    }

    /**
     * Iterator over the tree nodes. The nodes are returned according to the in order tree
     * traversal.
     */
    private class TreeNodeIterator
        implements
        Iterator<TreeNode<T>>
    {
        /**
         * A node that is returned next or {@code null} if all nodes are traversed
         */
        private TreeNode<T> nextNode;
        /**
         * Number of modifications of the tree at the time this iterator is created.
         */
        private final int expectedModCount;

        /**
         * Constructs a new {@code TreeNodeIterator}
         */
        public TreeNodeIterator()
        {
            nextNode = getMin();
            expectedModCount = modCount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            checkForComodification();
            return nextNode != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TreeNode<T> next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            TreeNode<T> result = nextNode;
            nextNode = successor(nextNode);
            return result;
        }

        /**
         * Checks if the tree has been modified during the iteration process
         */
        private void checkForComodification()
        {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Container holding the values stored in the tree.
     *
     * @param <T> a tree node value type
     */

}
