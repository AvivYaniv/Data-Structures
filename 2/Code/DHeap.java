import java.util.Arrays;

public class DHeap
{
	
    private int size, max_size, d;
    private DHeap_Item[] array;

	// Constructor
	// m_d >= 2, m_size > 0
    DHeap(int m_d, int m_size) {
               max_size = m_size;
			   d = m_d;
               array = new DHeap_Item[max_size];
               size = 0;
    }
	
	/**
	 * public int getSize()
	 * Returns the number of elements in the heap.
	 */
	public int getSize() {
		return size;
	}
	
  /**
     * public int arrayToHeap()
     *
     * The function builds a new heap from the given array.
     * Previous data of the heap should be erased.
     * preconidtion: array1.length() <= max_size
     * postcondition: isHeap()
     * 				  size = array.length()
     * Returns number of comparisons along the function run. 
	 */
    public int arrayToHeap(DHeap_Item[] array1) 
    {
    	size = array1.length;
    	array = array1;
    	
    	// set pos, just in case...
    	for (int i = 0; i < size; ++i)
    	{
    		array1[i].setPos(i);
    	}
    	
    	int counter = 0;
    	for (int i = size-1; i >=0 ; i--)
    	{
    		int curr = Heapify_Down(i);
//    		System.out.println(i + " - " + curr);
//    		print();
    		counter += curr;
    	}
    	
        return counter; 
    }

    public void printKeysList() {
		String str = "[";
		for (int j = 0 ; j < size ; j++) {
			str += array[j].getKey() + (j == size-1 ? "" : ", ");			
		}
		str += "]";
		System.out.println(str);			
    }

    /**
     * public boolean isHeap()
     *
     * The function returns true if and only if the D-ary tree rooted at array[0]
     * satisfies the heap property or has size == 0.
     *   
     */
    public boolean isHeap() 
    {
    	for (int i = size - 1; i >= 0; i--)
    	{
    		DHeap_Item current = array[i];
    		DHeap_Item parent = array[parent(i, d)];
    		
//    		System.out.println("current: " + current.getKey() + " parent: " + parent.getKey());

    		if (parent.getKey() > current.getKey())
    		{
    			return false;
    		}
    	}
    	
        return true; 
    }
    


 /**
     * public static int parent(i,d), child(i,k,d)
     * (2 methods)
     *
     * precondition: i >= 0, d >= 2, 1 <= k <= d
     *
     * The methods compute the index of the parent and the k-th child of 
     * vertex i in a complete D-ary tree stored in an array. 
     * Note that indices of arrays in Java start from 0.
     */
    public static int parent(int i, int d) { 
//    	return (i-1)/d;
    	return (i-1)/d;
    } 

    public static int child(int i, int k, int d) { 
//    	return d*(i-1)+k;
    	return (d*i)+k;
    }

    public int Heapify_Up()
    {
    	return Heapify_Up(size - 1);
    }
    
    public int Heapify_Up(int i)
    {
    	int counter = 0;
		int parent_index = parent(i, d);
		DHeap_Item parent = array[parent_index];
		
    	if (parent.getKey() > array[i].getKey())
    	{
    		Swap(i, parent_index);
    		counter = Heapify_Up(parent_index);
    	}
  
		return 1+counter;
    }
    
    
    public int Heapify_Down()
    {
    	return Heapify_Down(0);
    }
    
    public int Heapify_Down(int i)
    {
    	int counter = 0;
    	int smallest = i;

    	for (int k = 1; k <= d; ++k)
    	{
    		if (isChildExist(i, k)) {
    			counter++;
        		int child_index = child(i, k, d);
        		DHeap_Item child = array[child_index];
        		if ((child_index <= size) && (child.getKey() < array[smallest].getKey()))
        		{
        			smallest = child_index;	
        		}    			
    		}
    	}
    	
    	if (smallest != i)
    	{
    		Swap(i, smallest);
    		counter += Heapify_Down(smallest);
    	}
    	
    	return counter;
    }
    
    
    private void Swap(int i, int j) {
    	DHeap_Item temp = array[j];
    	array[j] = array[i];
    	array[i] = temp;
    	
    	int temp_pos = array[j].getPos();
    	array[j].setPos(array[i].getPos());
    	array[i].setPos(temp_pos);;
	}

	/**
    * public int Insert(DHeap_Item item)
    *
	* Inserts the given item to the heap.
	* Returns number of comparisons during the insertion.
	*
    * precondition: item != null
    *               isHeap()
    *               size < max_size
    * 
    * postcondition: isHeap()
    */
    public int Insert(DHeap_Item item) 
    {        
    	if (size == max_size)
    	{
    		return 0;
    	}
    	
    	size++;
    	item.setPos(size - 1);
    	array[size - 1] = item;
    	return Heapify_Up();// should be replaced by student code
    }

 /**
    * public int Delete_Min()
    *
	* Deletes the minimum item in the heap.
	* Returns the number of comparisons made during the deletion.
    * 
	* precondition: size > 0
    *               isHeap()
    * 
    * postcondition: isHeap()
    */
    public int Delete_Min()
    {
    	Swap(0, size-1);
    	size--;
    	return Heapify_Down();
    }


    /**
     * public DHeap_Item Get_Min()
     *
	 * Returns the minimum item in the heap.
	 *
     * precondition: heapsize > 0
     *               isHeap()
     *		size > 0
     * 
     * postcondition: isHeap()
     */
    public DHeap_Item Get_Min()
    {
	return (0 == size) ? null : this.array[0];// should be replaced by student code
    }
	
  /**
     * public int Decrease_Key(DHeap_Item item, int delta)
     *
	 * Decerases the key of the given item by delta.
	 * Returns number of comparisons made as a result of the decrease.
	 *
     * precondition: item.pos < size;
     *               item != null
     *               isHeap()
     * 
     * postcondition: isHeap()
     */
    public int Decrease_Key(DHeap_Item item, int delta)
    {
    	item.setKey(item.getKey() - delta);
    	return Heapify_Up(item.getPos());
	}
	
	  /**
     * public int Delete(DHeap_Item item)
     *
	 * Deletes the given item from the heap.
	 * Returns number of comparisons during the deletion.
	 *
     * precondition: item.pos < size;
     *               item != null
     *               isHeap()
     * 
     * postcondition: isHeap()
     */
    public int Delete(DHeap_Item item)
    {
    	int compCount = 0;
    	compCount += Decrease_Key(item, item.getKey() - Get_Min().getKey() + 1);
    	compCount += Delete_Min();
    	return compCount;
	}
    
    public static int arrayToHeap(int[] array, DHeap heap)
    {
    	int compCounter = 0;
    	
    	for (int i = 0; i < array.length; ++i)
    	{
    		int compsInInsert = heap.Insert(new DHeap_Item("", array[i]));
//    		System.out.println("comp for " + array[i]+" in insert: " + compsInInsert);
    		compCounter += compsInInsert;
    	}
    	
    	return compCounter;
    }
	
	/**
	* Sort the input array using heap-sort (build a heap, and 
	* perform n times: get-min, del-min).
	* Sorting should be done using the DHeap, name of the items is irrelevant.
	* 
	* Returns the number of comparisons performed.
	* 
	* postcondition: array1 is sorted 
	*/
	public static int DHeapSort(int[] array1, int d) {
    	DHeap heap = new DHeap(d, array1.length);

    	int compCounter = arrayToHeap(array1, heap);
		
//		System.out.println("DHeapSort()");		
//		System.out.println(Arrays.toString(heap.getKeyArray()));		
//		heap.print();
		
		for (int i = 0; i < array1.length; ++i) {
			array1[i] = heap.Get_Min().getKey();
			int delMinComps = heap.Delete_Min();
//    		System.out.println("comp for " + array1[i]+" in delete min: " + delMinComps);
			compCounter += delMinComps;
		}
		
		return compCounter;
	}

	
   public void print() {
	   
       System.out.println("--------------------- size: " + getSize() );			   
       
       String prefix = "";
//	   String printData = Integer.toString(array[0].getKey());
//	   System.out.println(prefix + printData );			   
	   
	   printChildRec(0, prefix, true);
       
   }
   
   public void printChildRec(int i, String prefix, boolean isLast) {
	   String indent = "    ";
	   String pipe = "|   ";
	   
//	   String printData = Integer.toString(array[i].getKey());
	   String printData = array[i].getKey() +" ("+array[i].getPos()+")";
//	   String printData = Integer.toString(array[i].getPos());

       System.out.println(prefix + (isLast ? "└── " : "├── ") + printData );			   
	   
	   String p = prefix + (isLast ? indent : pipe);
	   
	   for (int k = 1 ; k <= d ; k++) {
    	   if (isChildExist(i, k)) {
    		   printChildRec(child(i, k, d), p, (k==d || !isChildExist(i, k+1)) );
    	   }
	   }
   }
   
   
   private boolean isChildExist(int i, int k) {
	   return (child(i, k, d) < size);
   } 
   
   public int getD() {
	   return d;
   } 

   
   public int[] getKeyArray() {
	   int[] intArr = new int[getSize()];
	   for (int i = 0 ; i < getSize() ; i++ ) {
		   intArr[i] = array[i].getKey();
	   }
	   return intArr;
   }

   public DHeap_Item getItem(int i) {
	   return array[i];
   }

}
