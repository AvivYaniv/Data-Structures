public class DHeap_Item {

		private String name ;
		private int  key ;
		private int  pos ; // Position in the heap (if inserted into a heap.)

		DHeap_Item(String name1,int key1)
		{
			name = name1 ;
			key  = key1 ;
			pos  = -1   ;
		}

		// Setters and Getters
		public void setKey(int key1) 
		{
			key = key1;
		}
		
		public void setPos(int pos1)
		{
			pos = pos1;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getKey()
		{
			return key;
		}
		
		public int getPos()
		{
			return pos;
		}
}