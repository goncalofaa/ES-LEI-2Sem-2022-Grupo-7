package org.jgrapht.util;


public class AVLTreeProduct {
	/**
	* Performs a right node rotation.
	 * @param <T>
	* @param node  a node to rotate
	* @return  a new parent of the  {@code  node}
	*/
	public static <T> TreeNode<T> rotateRight(TreeNode<T> node) {
		TreeNode<T> left = node.left;
		left.parent = null;
		node.setLeftChild(left.right);
		left.setRightChild(node);
		node.updateHeightAndSubtreeSize();
		left.updateHeightAndSubtreeSize();
		return left;
	}

	/**
	* Performs a left node rotation.
	 * @param <T>
	* @param node  a node to rotate
	* @return  a new parent of the  {@code  node}
	*/
	public static <T> TreeNode<T> rotateLeft(TreeNode<T> node) {
		TreeNode<T> right = node.right;
		right.parent = null;
		node.setRightChild(right.left);
		right.setLeftChild(node);
		node.updateHeightAndSubtreeSize();
		right.updateHeightAndSubtreeSize();
		return right;
	}

	/**
	* Checks whether the  {@code  node}  is unbalanced. If so, balances the  {@code  node}
	 * @param <T>
	* @param node  a node to balance
	* @return  a new parent of  {@code  node}  if the balancing occurs,  {@code  node}  otherwise
	*/
	public static <T> TreeNode<T> balanceNode(TreeNode<T> node) {
		node.updateHeightAndSubtreeSize();
		if (node.isLeftDoubleHeavy()) {
			if (node.left.isRightHeavy()) {
				node.setLeftChild(rotateLeft(node.left));
			}
			rotateRight(node);
			return node.parent;
		} else if (node.isRightDoubleHeavy()) {
			if (node.right.isLeftHeavy()) {
				node.setRightChild(rotateRight(node.right));
			}
			rotateLeft(node);
			return node.parent;
		}
		return node;
	}
}