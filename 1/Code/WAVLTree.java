package WAVLTree;

import java.awt.HeadlessException;

import javax.print.attribute.standard.RequestingUserName;

/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree with
 * distinct integer keys and info
 *
 */

public class WAVLTree {
	private static final int RIGHT_DIRECTION = 0;
	private static final int LEFT_DIRECTION = 1;


	private IWAVLNode root;
	
	private WAVLNode virtualLeaf;
	
	private String minVal;
	private String maxVal;
	
	public WAVLTree() {
		root = null;
		virtualLeaf = new WAVLNode();
	}
	
	
  /**
   * public boolean empty()
   *
   * returns true if and only if the tree is empty
   *
   */
  public boolean empty() {
    return (root  == null); 
  }

 /**
   * public String search(int k)
   *
   * returns the info of an item with key k if it exists in the tree
   * otherwise, returns null
   */
  public String search(int k)
  {	

	  IWAVLNode x = root;
	  while ( x != null && x.isRealNode() ) {
		  if ( x.getKey() == k ) {
			  break;
		  } else if ( k < x.getKey() ) {
			  x = x.getLeft();
		  } else {
			  x = x.getRight();
		  }		  
	  }
	  
	  return ( x != null && x.isRealNode() && x.getKey() == k ) ? x.getValue() : null;  
  }
  
  
  

  /**
   * public int insert(int k, String i)
   *
   * inserts an item with key k and info i to the WAVL tree.
   * the tree must remain valid (keep its invariants).
   * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
   * returns -1 if an item with key k already exists in the tree.
   */
   public int insert(int k, String i) {
	   IWAVLNode z = new WAVLNode(k, i);
	   
//	   System.out.println("insert: " + k + ", " + i);
	   
	   int res = 0;
	   if (null == root)
	   {
		   root = z;
		   ((WAVLNode) root).setLeft(virtualLeaf);
		   ((WAVLNode) root).setRight(virtualLeaf);
//		   return 0;
	   } else {
		   res = treeInsert(root, z);	
	   }
	   
	   
	   recalculateMin();
	   recalculateMax();
	   return res;
   }

// z is the node to be inserted to the sub tree of x
private int treeInsert(IWAVLNode x, IWAVLNode z) {
	IWAVLNode y = treePosition(x, z.getKey());
	
	if (z.getKey() == y.getKey())
	{
		return -1;
	}
	
	((WAVLNode) z).setParent(y);
	
	if (z.getKey() < y.getKey()) {
		((WAVLNode) y).setLeft(z);
	} else {
		((WAVLNode) y).setRight(z);
	}
		
	int rebSteps = rebalanceInsert((WAVLNode)z);

	return rebSteps;
}


	private int rebalanceInsert(WAVLNode x) {
		int rebalanceSteps = 0;
		WAVLNode startNode = x;
		
		while (root != x)
		{
			
			if (isInsertCase1(x))
			{				
//				System.out.println("isInsertCase1 promote x:" + x.getKey());
				rebalanceSteps += insertCase1(x);	
				updateNodeSizeAndHeight(x);
				x = (WAVLNode) x.parent;
				
				// the update function will not update the root
				if (root == x) {
					updateNodeSizeAndHeight(x);
				}
				
				continue;
			}		
			else
			{
				int insertDirection = getInsert2or3Direction(x);
				
				if (insertDirection > -1)
				{
					int insertCase = getInsertCase2or3(x, insertDirection);
//					System.out.println("insertDirection: " + (insertDirection == RIGHT_DIRECTION? "right" : "left") );
//					System.out.println("insertCase: " + insertCase );
					
					if (2 == insertCase)
					{
//						System.out.println("isInsertCase2 x:" + x.getKey());
						rebalanceSteps += insertCase2(x, insertDirection);
					}
					else if (3 == insertCase)
					{
//						System.out.println("isInsertCase3 x:" + x.getKey());
						rebalanceSteps += insertCase3(x, insertDirection);
					}
				}
			}


			// only on end of promote 
			updateHeightFromNode(startNode);	
			updateSizeFromNode(startNode);
			
			break;
		}
	

		return rebalanceSteps;
}

	private int insertCase2(WAVLNode x, int insertDirection) {
		WAVLNode z = null;
//		System.out.println("-------------insertCase2 x: " + x.getKey() );
		
		// imbalance in right child of x parent
		if (RIGHT_DIRECTION == insertDirection)
		{
			z = rotateLeft(x);
		}
		// imbalance in left child of x parent
		else if (LEFT_DIRECTION == insertDirection)
		{
			z = rotateRight(x);
		}
		

		demote(z);
		if (x.getLeft().isRealNode()) {
			updateNodeSizeAndHeight( (WAVLNode)x.getLeft() );			
		}
		if (x.getRight().isRealNode()) {
			updateNodeSizeAndHeight( (WAVLNode)x.getRight() );			
		}

		updateHeightFromNode(x);	
		updateSizeFromNode(x);

		return 1; // no demotes
	}


	private int insertCase3(WAVLNode x, int insertDirection) {
		WAVLNode z = null;
		WAVLNode b = null;
		int steps = 2; // do not consider demotes
		
		WAVLNode newSubTreeRoot = x;
		
		
		// imbalance is on right side
		if (RIGHT_DIRECTION == insertDirection)
		{
			b = (WAVLNode)x.getLeft();
			promote((WAVLNode)x.getLeft());
			z = rotateRight((WAVLNode)x.getLeft());
			demote(z);
			z = rotateLeft((WAVLNode)x.parent);
		}
		// imbalance is on left side
		else if (LEFT_DIRECTION == insertDirection)
		{
			b = (WAVLNode)x.getRight();
			promote((WAVLNode)x.getRight());
			z = rotateLeft((WAVLNode)x.getRight());
			demote(z);
			z = rotateRight((WAVLNode)x.parent);
		}

		
		updateNodeSizeAndHeight( (WAVLNode)b.getLeft() );			
		updateNodeSizeAndHeight( (WAVLNode)b.getRight() );			
		
		if (null != z && z.isRealNode())
		{
			demote(z);
//			steps++;
		}
		
		if ( newSubTreeRoot.getLeft().getRight() != null ) {
			updateHeightFromNode((WAVLNode)newSubTreeRoot.getLeft());	
			updateSizeFromNode((WAVLNode)newSubTreeRoot.getLeft());
		}

		if ( newSubTreeRoot.getRight().getLeft() != null ) {
			updateHeightFromNode((WAVLNode)newSubTreeRoot.getRight());	
			updateSizeFromNode((WAVLNode)newSubTreeRoot.getRight());
		}

		
		return steps;
	}


	private WAVLNode rotateRight(WAVLNode x) {
//		WAVLNode a = (WAVLNode) x.left;
		WAVLNode b = (WAVLNode) x.right;
		WAVLNode z = (WAVLNode) x.parent;
		WAVLNode zp = (WAVLNode) z.parent;

		if ( null != zp ) {
			if (z == zp.left) {
				zp.left = x;
			} else {
				zp.right = x;
			}
			x.parent = zp;
		} else {
			x.parent = null;
			if (root == z) {
				root = x;
			}
		}

		b.parent = z;
		z.left = b;
		
		x.right = z;
		z.parent = x;
		
		return z;
	}

	private WAVLNode rotateLeft(WAVLNode x) {
		WAVLNode a = (WAVLNode) x.left;
//		WAVLNode b = (WAVLNode) x.right;
		WAVLNode z = (WAVLNode) x.parent;
		WAVLNode zp = (WAVLNode) z.parent;

		if ( null != zp ) {
			if (z == zp.left) {
				zp.left = x;
			} else {
				zp.right = x;
			}
			x.parent = zp;
		} else {
			x.parent = null;
			if (root == z) {
				root = x;
			}
		}
		a.parent = z;
		z.right = a;

		x.left = z;
		z.parent = x;
		
		return z;
	}
	
	private void demote(WAVLNode x) {
		x.rank--;
	}

	private int getInsert2or3Direction(WAVLNode x) {
		WAVLNode xp = ((WAVLNode)x.parent);
	
		if ( (null == (WAVLNode)xp.right ) || (null == (WAVLNode)xp.left) )
		{
			return -1;
		}
		
		int nXpRightDiffrence = xp.getRank() - ((WAVLNode)xp.right).getRank();
		int nXpLeftDiffrence = xp.getRank() - ((WAVLNode)xp.left).getRank();
		
		if ( (2 == nXpLeftDiffrence) && (0 == nXpRightDiffrence) )
		{
			return RIGHT_DIRECTION;
		}
		else if ( (0 == nXpLeftDiffrence) && (2 == nXpRightDiffrence) )
		{
			return LEFT_DIRECTION;
		}
		
		return -1;
	}

	private int getInsertCase2or3(WAVLNode x, int dir) {
		int nCurrentRightDiff = x.getRank() - ((WAVLNode)x.right).getRank();
		int nCurrentLeftDiff = x.getRank() - ((WAVLNode)x.left).getRank();
		
		if ( ( LEFT_DIRECTION == dir && ((2 == nCurrentLeftDiff) && (1 == nCurrentRightDiff)) ) ||
			 ( RIGHT_DIRECTION == dir && ((1 == nCurrentLeftDiff) && (2 == nCurrentRightDiff)) ) )
		{
			return 3;
		}
		
		if ( LEFT_DIRECTION == dir && ((1 == nCurrentLeftDiff) && (2 == nCurrentRightDiff)) ||
			 RIGHT_DIRECTION == dir && ((2 == nCurrentLeftDiff) && (1 == nCurrentRightDiff)) )
		{
			return 2;
		}
		
		return -1;
	}


	private int insertCase1(WAVLNode x) {
		promote((WAVLNode)x.parent);
		return 1;
	}


	private void promote(WAVLNode x) {
		x.rank++;
	}


	private boolean isInsertCase1(WAVLNode x) {
	WAVLNode xp = ((WAVLNode)x.parent);
	
	int nRightDiffrence = xp.getRank() - ((WAVLNode)xp.right).getRank();
	int nLefttDiffrence = xp.getRank() - ((WAVLNode)xp.left).getRank();
	
	return (((0 == nRightDiffrence) && (1 == nLefttDiffrence)) || 
			((1 == nRightDiffrence) && (0 == nLefttDiffrence)));
}


	/*
	 * recalculate the height of each node of the path from the root to the given node n
	*/ 
	private void updateHeightFromNode(WAVLNode p) {
		while( null != p ) {
			if ( p.isRealNode() ) {
				int leftH = ((WAVLNode)p.getLeft()).getHeight();
				int rightH = ((WAVLNode)p.getRight()).getHeight();
				p.setHeight( (leftH > rightH ? leftH: rightH)  + 1);
			}
			p = (WAVLNode)p.getParent();
		}
	}

	/*
	 * recalculate the size of each node of the path from the root to the given node n
	*/ 
	private void updateSizeFromNode(WAVLNode p) {
		// increase ancestors size
		while( null != p ) {
			if ( p.isRealNode() ) {
				p.setSize(p.getLeft().getSubtreeSize() + p.getRight().getSubtreeSize() + 1);
//				System.out.println("p: " + p.getKey() + "   l: " + p.getLeft().getSubtreeSize() + "   r: " + p.getRight().getSubtreeSize());
			}
			p = (WAVLNode)p.getParent();
		}
	}
  
	
private void updateNodeSizeAndHeight(WAVLNode p) {
	if (!p.isRealNode()) {
		return;
	}
	int leftH = ((WAVLNode)p.getLeft()).getHeight();
	int rightH = ((WAVLNode)p.getRight()).getHeight();
	p.setHeight( (leftH > rightH ? leftH: rightH)  + 1);	
	p.setSize(p.getLeft().getSubtreeSize() + p.getRight().getSubtreeSize() + 1);
}
  

/*package*/ IWAVLNode treePosition(IWAVLNode x, int k) {
	IWAVLNode y = null;
	
	while ( null != x && x.isRealNode() )
	{
		y = x;
		
		if (x.getKey() == k)
		{
			return x;
		}
		else if (x.getKey() > k)
		{
			x = x.getLeft();
		}
		else
		{
			x = x.getRight();
		}
	}
	
	return y;
}




/**
   * public int delete(int k)
   *
   * deletes an item with key k from the binary tree, if it is there;
   * the tree must remain valid (keep its invariants).
   * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
   * returns -1 if an item with key k was not found in the tree.
   */
   public int delete(int k)
   {
//	   System.out.println("delete: " + k );

	   if (null == root)
	   {
		   return -1;
	   }
	   
	   WAVLNode y = (WAVLNode)treePosition(root, k);
	   WAVLNode rebalanceStartingNode = null;
		
		if (y.getKey() != k || !y.isRealNode() )
		{
			return -1;
		}
		
		
		WAVLNode p = (WAVLNode) y.getParent();
		
		// is the node to be deleted a leaf?
		if (!y.getLeft().isRealNode() && !y.getRight().isRealNode()) {
			
			if ( root == y && root.getSubtreeSize() == 1 ) {
				root = null;
			} else {
				// y is left child - remove it
				if ( p.getLeft() == y ) {
					p.setLeft(virtualLeaf);

					rebalanceStartingNode = ( p.getRight().isRealNode() ? (WAVLNode)p.getRight() : (WAVLNode)y );
					
				// y is right child - remove it
				} else {
					p.setRight(virtualLeaf);

					rebalanceStartingNode = ( p.getLeft().isRealNode() ? (WAVLNode)p.getLeft() : (WAVLNode)y );
				}
			}
			
		// is the node a unary to the right?
		} else if (!(y.getLeft().isRealNode())) {

			rebalanceStartingNode = (WAVLNode) y.getRight();
//			rebalanceStartingNode = (y.getRight().isRealNode() ? (WAVLNode) y.getRight() : (WAVLNode)y.parent);
			
			if ( root == y ) {
				root = y.getRight();
				y.setParent(null);
			} else {
				// y is left child - remove it
				if ( p.getLeft() == y ) {
					p.setLeft(y.getRight());

				// y is right child - remove it
				} else {
					p.setRight(y.getRight());
				}				
			}


		// is the node a unary to the left?
		} else if (!(y.getRight().isRealNode())) {
			
			rebalanceStartingNode = (WAVLNode) y.getLeft();

			if ( root == y ) {
				root = y.getLeft();
				y.setParent(null);
			} else {
				// y is left child - remove it
				if ( p.getLeft() == y ) {
					p.setLeft(y.getLeft());
	
				// y is right child - remove it
				} else {
					p.setRight(y.getLeft());
				}
			}

		// the node has two children - use the successor
		} else {
			WAVLNode s = successor(y);
			
			WAVLNode sp = (WAVLNode)s.parent;
			WAVLNode yp = (WAVLNode)y.parent;

			WAVLNode origSParent = sp;
			WAVLNode origSRight = (WAVLNode)sp.getRight();

			
			if ( origSParent == y ) {
				if (y.getLeft().isRealNode()) {
					rebalanceStartingNode = (WAVLNode) y.getLeft();
//					System.out.println("origSParent == y");
				} else {
					rebalanceStartingNode = (WAVLNode) s;
				}
			} else {
				
				if (s.getRight().isRealNode()) {
					rebalanceStartingNode = (WAVLNode) s.getRight();
				} else {
					
					if (s.parent.getRight().isRealNode()) {
						rebalanceStartingNode = (WAVLNode) s.parent.getRight();
					} else {
						rebalanceStartingNode = (WAVLNode) s.parent;
					}
				}
			
			} 
			
			// if y was the root, update root to s
			if (root == y) {
				root = s;
			} else {
				if (yp.getRight() == y)
				{
					yp.setRight(s);
				}
				else
				{
					yp.setLeft(s);
				}
			}
			s.setParent(yp);

			if (sp == y) {
			} else {
				sp.setLeft(s.getRight());
				((WAVLNode) s.getRight()).setParent(sp);
	
	//			replace(y, s); // commented as maybe other point to him
				s.setRight(y.right);
				((WAVLNode) y.getRight()).setParent(s);
			}

			s.setLeft(y.left);
			((WAVLNode) y.getLeft()).setParent(s);
			
			s.setRank(y.rank);
			
		}
		
		int res = rebalanceDelete(rebalanceStartingNode);	
	   recalculateMin();
	   recalculateMax();
//		System.out.println("rebalanceStartingNode: " + rebalanceStartingNode.getKey() );
	    return res;
   }
   
private int rebalanceDelete(WAVLNode x) {
	WAVLNode origStartingNode = x;
	
	int rebalanceSteps = 0;

	if (root == null) {
		return 0;
	}
	
	
	while (null != x)
	{
//		System.out.println("rebalanceDelete x: " + x.getKey() );
		
		if (is2_2Leaf((WAVLNode)x))
		{				
//			System.out.println("2-2 leaf delete: " + x.getKey() );
			demote(x);
			rebalanceSteps += 1;				
//			x = (WAVLNode) x.parent;
			continue;
		}	

		if (null != x.parent && is2_2Leaf((WAVLNode)x.parent))
		{				
//			System.out.println("2-2 parent leaf delete: " + x.parent.getKey() );
			demote((WAVLNode)x.parent);
			rebalanceSteps += 1;		
			updateNodeSizeAndHeight(x);
			x = (WAVLNode) x.parent;
			continue;
		}	

		if (isDeleteCase1(x))
		{				
//			System.out.println("deleteCase1 x: " + x.getKey() );
			rebalanceSteps += deleteCase1(x);				
			updateNodeSizeAndHeight(x);
			x = (WAVLNode) x.parent;
			continue;
		}		
		else
		{
			int deleteDirection = getDelete2or3or4Direction(x);

			if (deleteDirection > -1)
			{					
				int deleteCase = getDeleteCase2or3or4(x, deleteDirection);
//				System.out.println("deleteCase: " + deleteCase + "   x: " + x.getKey() );
//				System.out.println("deleteDirection: " + (deleteDirection == RIGHT_DIRECTION? "right" : "left") );
				
				if (2 == deleteCase)
				{
					rebalanceSteps += deleteCase2(x, deleteDirection);
					updateNodeSizeAndHeight(x);
					x = (WAVLNode) x.parent;
					continue;
				}
				else if (3 == deleteCase)
				{
					WAVLNode xp = (WAVLNode) x.parent;
					
					rebalanceSteps += deleteCase3(x, deleteDirection);					
					updateNodeSizeAndHeight(x);
					x = (WAVLNode) xp.parent;
					continue;
					
				}
				else if (4 == deleteCase)
				{
					origStartingNode = (WAVLNode)x.parent;
					rebalanceSteps += deleteCase4(x, deleteDirection);
					updateNodeSizeAndHeight((WAVLNode)x.parent.getLeft());
					updateNodeSizeAndHeight((WAVLNode)x.parent.getRight());
					break;
				}
			}
		}
		 
		break;
	}
	
	// update height and rank
	if (origStartingNode.getLeft().isRealNode()) {
		updateNodeSizeAndHeight((WAVLNode)origStartingNode.getLeft());
	}
	if (origStartingNode.getRight().isRealNode()) {
		updateNodeSizeAndHeight((WAVLNode)origStartingNode.getRight());
	}
	updateHeightFromNode(origStartingNode);	
	updateSizeFromNode(origStartingNode);


	return rebalanceSteps;

}

private boolean is2_2Leaf(WAVLNode x) {
	int rankDiffLeft = x.rank - ((WAVLNode)x.getLeft()).rank;
	int rankDiffRight = x.rank - ((WAVLNode)x.getRight()).rank;
	return (2 == rankDiffLeft) && (2 == rankDiffRight) && 
			(!x.getLeft().isRealNode() && (!x.getRight().isRealNode()) );
}




private int deleteCase4(WAVLNode x, int deleteDirection) {
	WAVLNode z = (WAVLNode) x.parent;
	WAVLNode y = null;
	WAVLNode a = null;

	// get a and y in order to check validity of double rotation
	// get y
	if (RIGHT_DIRECTION == deleteDirection) {
		if ( z.getLeft().isRealNode() ) {
			y = (WAVLNode)z.getLeft();
			
			// get a
			if ( y.getRight().isRealNode() ) {
				a = (WAVLNode)y.getRight();
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	} else if (LEFT_DIRECTION == deleteDirection) {
		if ( z.getRight().isRealNode() ) {
			y = (WAVLNode)z.getRight();
			
			// get a
			if ( y.getLeft().isRealNode() ) {
				a = (WAVLNode)y.getLeft();
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	if (RIGHT_DIRECTION == deleteDirection) {
		rotateLeft(a);
		rotateRight(a);
	} else if (LEFT_DIRECTION == deleteDirection) {
		rotateRight(a);
		rotateLeft(a);
	}

	// update ranks
	int oldZRank = z.getRank();
	z.setRank(a.getRank());
	y.setRank(a.getRank());
	a.setRank(oldZRank);

	return 2;
}

private void updateNodeRank(WAVLNode a) {
	WAVLNode al = (WAVLNode)a.getLeft();
	WAVLNode ar = (WAVLNode)a.getRight();
	a.rank = 1 + Math.max(al.rank, ar.rank);	
}


private int deleteCase3(WAVLNode x, int deleteDirection) {
	int steps = 1; // do not consider demotes
	WAVLNode z = (WAVLNode) x.parent;
	WAVLNode y = null;
//	WAVLNode z = null;
	
	if (RIGHT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) z.left;
		rotateRight(y);
	}
	else if (LEFT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) z.right;
		rotateLeft(y);
	}
	
	// update ranks
	int oldZRank = z.rank;
	z.rank = y.rank;
	y.rank = oldZRank;

//	z.rank = 1 + Math.max(((WAVLNode)z.left).rank, ((WAVLNode)z.right).rank);
//	y.rank = 1 + Math.max(((WAVLNode)y.left).rank, ((WAVLNode)y.right).rank);

	
	if ( z.isReal )
	{
		int nZRightDiffrence = z.getRank() - ((WAVLNode)z.right).getRank();
		int nZLeftDiffrence = z.getRank() - ((WAVLNode)z.left).getRank();
		
		if ( (2 == nZRightDiffrence) && (2 == nZLeftDiffrence) &&
			 !z.getLeft().isRealNode() && !z.getRight().isRealNode())
		{
			demote(z);
//			steps++;
		}
	}

	

	return steps;
}


private int deleteCase2(WAVLNode x, int deleteDirection) {
	WAVLNode z = (WAVLNode) x.parent;
	
	WAVLNode y = null;
	
	if (RIGHT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) z.left;		
	}
	else if (LEFT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) z.right;
	}
	
	demote(y);
	demote(z);
	
	return 2;
}


private int getDeleteCase2or3or4(WAVLNode x, int deleteDirection) {
	WAVLNode xp = (WAVLNode) x.parent; 

	if (null == xp) {
//		System.out.println("getDeleteCase2or3or4: xp is null");
		return -1;
	}
	
	WAVLNode y = null;
	
	if (RIGHT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) xp.left;		
	}
	else if (LEFT_DIRECTION == deleteDirection)
	{
		y = (WAVLNode) xp.right;
	} else {
		return -1;
	}
	
//	System.out.println("y.getKey(): " + y.getKey() );
//	printTree();
	
	int nYRightDiffrence = y.getRank() - ((WAVLNode)y.right).getRank();
	int nYLeftDiffrence = y.getRank() - ((WAVLNode)y.left).getRank();
	
	if ( (2 == nYLeftDiffrence ) && (2 == nYRightDiffrence) )
	{
		return 2;
	}
	
	if ( ( LEFT_DIRECTION == deleteDirection && ((1 == nYLeftDiffrence) || (2 == nYLeftDiffrence)) && (1 == nYRightDiffrence) ) ||
		 ( RIGHT_DIRECTION == deleteDirection && (1 == nYLeftDiffrence) && ((1 == nYRightDiffrence) || (2 == nYRightDiffrence)) ) )
	{
		return 3;
	}
	
	if ( ( LEFT_DIRECTION == deleteDirection && (1 == nYLeftDiffrence) && (2 == nYRightDiffrence) ) ||
		 ( RIGHT_DIRECTION == deleteDirection && (2 == nYLeftDiffrence) && (1 == nYRightDiffrence) ) )
	{
		return 4;
	}
	
	return -1;
}


private int getDelete2or3or4Direction(WAVLNode x) {
	WAVLNode xp = ((WAVLNode)x.parent);
	
	if ( (null == xp) || (null == (WAVLNode)xp.right) || (null == (WAVLNode)xp.left)) 
	{
//		System.out.println("getDelete2or3or4Direction: xp is null");
		return -1;
	}
	
	int nXpRightDiffrence = xp.getRank() - ((WAVLNode)xp.right).getRank();
	int nXpLeftDiffrence = xp.getRank() - ((WAVLNode)xp.left).getRank();
	
	if ((1 == nXpLeftDiffrence) && (3 == nXpRightDiffrence) )
	{
		return RIGHT_DIRECTION;
	}
	else if ((3 == nXpLeftDiffrence) && (1 == nXpRightDiffrence) )
//		else if (((1 == nXpRightDiffrence) || (2 == nXpRightDiffrence)) && (3 == nXpLefttDiffrence))
	{
		return LEFT_DIRECTION;
	}
	
	return -1;
}




private int deleteCase1(WAVLNode x) {
	demote((WAVLNode)x.parent);
	
	return 1;
}


private boolean isDeleteCase1(WAVLNode x) {
	WAVLNode xp = ((WAVLNode)x.parent);
	
	if (null == xp) {
//		System.out.println("isDeleteCase1: xp is null: ");
		return false;
	}
	
	int nRightDiffrence = xp.getRank() - ((WAVLNode)xp.right).getRank();
	int nLeftDiffrence = xp.getRank() - ((WAVLNode)xp.left).getRank();
	
	return ( (3 == nLeftDiffrence) && (2 == nRightDiffrence) ) || 
		   ( (2 == nLeftDiffrence) && (3 == nRightDiffrence) );
}


private void replace(WAVLNode y, WAVLNode s) {
	y.set(s);	
}


private WAVLNode successor(WAVLNode x) {
	   if (null != x.getRight() ) {
		   return (WAVLNode) minNode(x.right);
	   }
	   
	   WAVLNode y = (WAVLNode) x.parent;
	   
	   while ((null != y) && (y.getRight() == x))
	   {
		   x = y;
		   y = (WAVLNode) x.parent;
	   }
	   
	   return y;
   }
   
   
   
   /**
    * public String min()
    *
    * Returns the info of the item with the smallest key in the tree,
    * or null if the tree is empty
    */
   public String min()
   {
	   return minVal;
   }
   
   public void recalculateMin()
   {
	   if ( null == root ) {
		   minVal = null;
		   return;
	   }
	   
	   IWAVLNode x = root;
	   while (x.getLeft().isRealNode())
	   {
		   x = x.getLeft();   
	   }
	   
	   minVal =  x.getValue();
   }

   
   public IWAVLNode minNode(IWAVLNode n)
   {
	   IWAVLNode x = n;
	   while (x.getLeft().isRealNode())
	   {
		   x = x.getLeft();   
	   }
	   return x;
   }

   /**
    * public String max()
    *
    * Returns the info of the item with the largest key in the tree,
    * or null if the tree is empty
    */
   public String max()
   {
	   return maxVal;
   }

   public void recalculateMax()
   {
	   if ( null == root ) {
		   maxVal = null;
		   return;
	   }
	   
	   IWAVLNode x = root;
	   while (x.getRight().isRealNode())
	   {
		   x = x.getRight();   
	   }
	   maxVal =  x.getValue();

   }

   
   

  /**
   * public int[] keysToArray()
   *
   * Returns a sorted array which contains all keys in the tree,
   * or an empty array if the tree is empty.
   */
  public int[] keysToArray()
  {
     return inorderKey();
  }
  
  public int[] inorderKey()
  {
	  int[] arr = new int[size()]; 

	  if (0 != arr.length)
	  {
		  inorderKeyRec(root, arr, 0);
	  }
	  
	  return arr;
  }
  
  public int inorderKeyRec(IWAVLNode node, int[] arr, int i)
  {
	  IWAVLNode l = node.getLeft();
	  IWAVLNode r = node.getRight();
	  if (null != l && l.isRealNode())
	  {
		  i = inorderKeyRec(l, arr, i);
	  }
	  
	  arr[i] = node.getKey();
//	  System.out.println("i: " + i + "  key: " + node.getKey());
	  i++;
	  
	  if (null != r && r.isRealNode())
	  {
		  i = inorderKeyRec(r, arr, i);
	  }
	  return i;
  }
  
  public String[] inorderValue()
  {
	  String[] arr = new String[size()]; // to be replaced by student code
	  
	  if (0 != arr.length)
	  {
		  inorderValueRec(root, arr, 0);
	  }
	  
	  return arr;
  }
  
  public int inorderValueRec(IWAVLNode node, String[] arr, int i)
  {
	  IWAVLNode l = node.getLeft();
	  IWAVLNode r = node.getRight();
	  if (null != l && l.isRealNode())
	  {
		  i = inorderValueRec(l, arr, i);
	  }
	  
	  arr[i] = node.getValue();
//			  System.out.println("i: " + i + "  key: " + node.getValue());
	  i++;
	  
	  if (null != r && r.isRealNode())
	  {
		  i = inorderValueRec(r, arr, i);
	  }
	  return i;
			  
  }

  /**
   * public String[] infoToArray()
   *
   * Returns an array which contains all info in the tree,
   * sorted by their respective keys,
   * or an empty array if the tree is empty.
   */
  public String[] infoToArray()
  {
	  return inorderValue();
  }

   /**
    * public int size()
    *
    * Returns the number of nodes in the tree.
    *
    * precondition: none
    * postcondition: none
    */
   public int size()
   {
	   return (null == root) ? 0 : root.getSubtreeSize(); 
   }
   
     /**
    * public int getRoot()
    *
    * Returns the root WAVL node, or null if the tree is empty
    *
    * precondition: none
    * postcondition: none
    */
   public IWAVLNode getRoot()
   {
	   return root;
   }
     /**
    * public int select(int i)
    *
    * Returns the value of the i'th smallest key (return -1 if tree is empty)
    * Example 1: select(1) returns the value of the node with minimal key 
	* Example 2: select(size()) returns the value of the node with maximal key 
	* Example 3: select(2) returns the value 2nd smallest minimal node, i.e the value of the node minimal node's successor 	
    *
	* precondition: size() >= i > 0
    * postcondition: none
    */   
   public String select(int i)
   {
	   if ( 0 > i || i >= size() ) {
		   return "-1";
	   }
	   return selectRec(root, i).getValue(); 
   }
   
   public IWAVLNode selectRec(IWAVLNode x, int i)
   {
	   int r  = 0;

	   if (x == null) 
	   {
	   return null;
	   }
	   
	   IWAVLNode l = x.getLeft();
	   
	   if (null != l)
	   {
		   	r = l.getSubtreeSize();
	   }
	  
	   if (i == r)
	   {
		   return x;
	   }
	   else if (i < r)
	   {
		   return selectRec(x.getLeft(), i);
	   }
	   else
	   {
		   return selectRec(x.getRight(), i - r - 1);
	   }
	   
   }
   
   
   public void printTree() {
	   if (null == root) {
		   System.out.println("------------------ size: " + size());
		   System.out.println("null == root");
		   return;
	   }
	   System.out.println("------------------ size: " + size() + "   height: " + ((WAVLNode)root).height);
	   ((WAVLNode)root).print("", true);
	   System.out.println("----------------------");
   }
   
   
	/**
	   * public interface IWAVLNode
	   * ! Do not delete or modify this - otherwise all tests will fail !
	   */
	public interface IWAVLNode{	
		public int getKey(); //returns node's key (for virtuval node return -1)
		public String getValue(); //returns node's value [info] (for virtuval node return null)
		public IWAVLNode getLeft(); //returns left child (if there is no left child return null)
		public IWAVLNode getRight(); //returns right child (if there is no right child return null)
		public boolean isRealNode(); // Returns True if this is a non-virtual WAVL node (i.e not a virtual leaf or a sentinal)
		public int getSubtreeSize(); // Returns the number of real nodes in this node's subtree (Should be implemented in O(1))
//		public void setRight(IWAVLNode r);
//		public void setLeft(IWAVLNode l);
//		public void setParent(IWAVLNode p);
	}

   /**
   * public class WAVLNode
   *
   * If you wish to implement classes other than WAVLTree
   * (for example WAVLNode), do it in this file, not in 
   * another file.
   * This class can and must be modified.
   * (It must implement IWAVLNode)
   */
  public class WAVLNode implements IWAVLNode{

	  	// unique key for each Node
	  	private int key;
	  	private String info;
	  	private int height; 
	  	private int size; 
	  	
	  	private int rank;
	  	

		private IWAVLNode right;
	  	private IWAVLNode left;
	  	private IWAVLNode parent;
	  	
	  	private boolean isReal;
	  	
	  	// create actual Node
	  	public WAVLNode (int k, String i) {
	  		key = k;
	  		info = i;
	  		size = 1;
	  		isReal = true;
	  		right = virtualLeaf;
	  		left = virtualLeaf;
	  		height = 0;
	  		rank = 0;
	  	}
	  	
		// create virtual Node
		private WAVLNode() {
	  		size = 0;
	  		isReal = false;
	  		info = "virtual";
	  		height = -1;
	  		rank = -1;
		}

		private void set(WAVLNode s) {
	  		key = s.key;
	  		info = s.info;
	  		height = s.height; 
		  	size = s.size; 
		  	rank = s.rank;
		  	isReal = s.isReal;
		  	
		  	right = s.right;
		  	left = s.left;
		  	parent = s.parent;	
		}

	  	public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}


		public int getHeight()
	  	{
	  		return height;
	  	}

		public void setHeight(int h)
	  	{
			height = h;
	  	}

		
		public void setSize(int s) {
			size = s;
		}

		public IWAVLNode getParent()
	  	{
	  		return parent;
	  	}

		public void setParent(IWAVLNode p)
	  	{
//			if (isReal) {
				parent = p;
//			}
	  	}

	  	public void setRight(IWAVLNode r)
	  	{
	  		right = r;	
			if ( isRealNode() ) {
				((WAVLNode) r).setParent(this);
			}
	  	}
	  	
		public void setLeft(IWAVLNode l)
		{
			left = l;
			if ( isRealNode() ) {
				((WAVLNode) l).setParent(this);
			}
		}
    
		public int getKey()
		{
			return key; // to be replaced by student code
		}
		public String getValue()
		{
			return info; // to be replaced by student code
		}
		public IWAVLNode getLeft()
		{
			return left; // to be replaced by student code
		}
		public IWAVLNode getRight()
		{
			return right; // to be replaced by student code
		}
		// Returns True if this is a non-virtual WAVL node (i.e not a virtual leaf or a sentinal)
		public boolean isRealNode()
		{
			return isReal; // to be replaced by student code
		}

		public int getSubtreeSize()
		{
			return size; // to be replaced by student code
		}
		
		
	   private void print(String prefix, boolean isTail) {
		   String printData = "";
//		   printData = info + "-" + height + " (" + size + ")"; 
//		   printData = info + "-" + (parent==null ? "null": parent.getKey() ); // check correct parent

		   if (isRealNode()) {
			   printData = info + "-" + rank + " (" + (rank - ((WAVLNode)getLeft()).rank) + "-" + (rank - ((WAVLNode)getRight()).rank) + ")";
		   } else {
			   printData = info + "-" + rank;
		   }
		   
		   // is root node
		   if ( prefix.equals("") ) {
		       System.out.println("    " + printData );
		   } else {
		       System.out.println(prefix + (isTail ? "└── " : "├── ") + printData );			   
		   }
	       
	       // right sub tree exists
	       if ( null != right ) {
		       // left sub tree exists
		       if ( null != left ) {
		    	   ((WAVLNode)getRight()).print(prefix + (isTail ?"    " : "│   "), false);
		    	   ((WAVLNode)getLeft()).print(prefix + (isTail ? "    " : "│   "), true);
		       } else {
		    	   ((WAVLNode)getRight()).print(prefix + (isTail ?"    " : "│   "), true);
		       }
	       } else {
		       // right sub tree exists
		       if ( null != left) {
		    	   ((WAVLNode)getLeft()).print(prefix + (isTail  ?"    " : "│   "), true);
		       }
	       }
	       
	       
	   }
		
		
  }

}
  

