import java.io.*;
import java.util.*;
import java.nio.file.*;

public class HuffmanMain implements Huffman {
	
	private final static int charArrLength = 256; 
	public static BinaryOut b = new BinaryOut();
	
    public static void main(String[] args) {
    	Scanner s = new Scanner(System.in);
    	String fileToEnc = "";
    	String encFile = "";
    	String freqFile = "";
    	String decFile = "";
    	
		      Huffman  huffman = new HuffmanMain();
		      
		      // user prompts
		      try {
		      System.out.println("Please enter the name of the file you'd like to encrypt: ");
		      fileToEnc = s.nextLine();
		      System.out.println("Please enter the name of the blank .enc file you want to encrypt to: ");
		      encFile = s.nextLine();
		      System.out.println("Please enter the name of a blank frequency file: ");
		      freqFile = s.nextLine();
		      System.out.println("Please enter the name of the blank decryption file: ");
		      decFile = s.nextLine();
		      }
		      catch (Exception e) {
		    	  
		    	  System.out.println("No input entered for one of the files");
		      }
		      
		      // encode and decode methods called
				huffman.encode(fileToEnc, encFile, freqFile);
				huffman.decode(encFile, decFile, freqFile);
		   }
    
    /* the encode method here works to first take input from the user file, store it all in a 
     * array of frequencies and respective character-indices. From there, the huffman tree is built
     * and the characters from the array are assigned respective huffman codes placed into an array.
     * Once this happens, this array (cast to characters) is traversed and the codes are written as
     * boolean bits to the .enc file.
     */
   public void encode(String inputFile, String outputFile, String freqFile){
	
	   int[] charAndFreqArr = null;
	   Node huffmanNode;
	   String[] huffmanCodeArrRaw;
	   String huffmanCodeStringRaw = "";
	   String huffmanCodeStringCounter = "";
	   String[] huffmanCodeArr;
	   char[] huffmanCodeChar;
	   char[] inputDataCharArr = null;
	   int numOfBits = 0;
	   
	   try {
		 // store user input into a character and freq array
		 Path fileName = Path.of(inputFile);
		 String actual = Files.readString(fileName);
		 inputDataCharArr = actual.toCharArray();
		 charAndFreqArr = getFrequencyData(inputDataCharArr);
	} 
	   catch (IOException e1) {
	   System.out.println("File not found");
	}
	 
	   // build huffman tree and store respective codes into String array
	   huffmanNode = buildHuffmanTree(charAndFreqArr);	
	   huffmanCodeArrRaw = buildCode(huffmanNode);

	   BinaryOut out = new BinaryOut(outputFile);
	   
	   // takes the length of the bit sequence so the 0 bit padding of the flush() 
	   // method doesn't mess up the decoding. Used later.
	   for (int i = 0; i < inputDataCharArr.length; i++) { 
		   
		   huffmanCodeStringCounter = huffmanCodeArrRaw[inputDataCharArr[i]];
		   numOfBits += huffmanCodeStringCounter.length();
	   }
	   
	   out.write(numOfBits);
	   
	// go through huffman code array to select the character sequences and output them to file
	   for (int i = 0; i < inputDataCharArr.length; i++) {
		   
		   huffmanCodeStringRaw = huffmanCodeArrRaw[inputDataCharArr[i]];
		   huffmanCodeChar = huffmanCodeStringRaw.toCharArray();
		   
		   // convert string huffman code elements into mini char arrays and write each one
		   // to the output stream as a boolean value
		   for (int j = 0; j < huffmanCodeChar.length; j++) {
			   
			if (huffmanCodeChar[j] == '1') {
				
				out.write(true);
			}
			
			else {
				
				out.write(false);
			}
		   }
	   }
	   out.flush();
	   
	   // output data of all characters to frequency file
		    try {
		        FileWriter myWriter = new FileWriter(freqFile);
		        for (int i = 0; i < charAndFreqArr.length; i++) {
		        	if(charAndFreqArr[i] > 0) {
		        		
		        		myWriter.write(Integer.toBinaryString(i) + ":" + charAndFreqArr[i]+"\n");
		        	}
		        }
		        myWriter.flush();
		      } 
		    catch (IOException e) {
		        System.out.println("File Not Found");
		      }
   }

   /* this method works to decode the encrypted file by first taking in the info from the frequency file 
    * into a frequency character array (aka rebuilding the one in the encoding method.) The decrypt method is then 
    * called below to finish the process
    */
   public void decode(String inputFile, String outputFile, String freqFile){
	   
	   // reconstructing char/freq arr for huffman tree reconstruction
		BinaryIn in = new BinaryIn(inputFile);
		Scanner s;
		Scanner sc;
		int rawArrLength = 0;
		int[] binaryArr;
		int[] freqArrRaw;
		int[] frequencyArr = new int[charArrLength];
		String[] lineArr = new String[2];
		String currLine = "";
		int charVal;
		int freqVal;
		try {
			File freq = new File(freqFile);
			s = new Scanner(freq);
			sc = new Scanner(freq);
			
			while(s.hasNextLine() == true) {
				rawArrLength++;
				s.nextLine();
			}
			
			binaryArr = new int[rawArrLength];
			freqArrRaw = new int[rawArrLength];
			
			// takes input from freq.txt and stores it into the frequency array
			while(sc.hasNextLine() == true) {
				
				currLine = sc.nextLine();
				lineArr = currLine.split(":");
				charVal = Integer.parseInt(lineArr[0], 2);
				freqVal = Integer.parseInt(lineArr[1]);		
		        frequencyArr[charVal] = freqVal;
			}
			// writes decrypted message to file
			decrypt(in, frequencyArr, outputFile);
		} catch (IOException e) {
			System.out.println("file not found");
		}
		
   }

	/* Method rebuilds huffman tree to decode based on frequency array. Then takes input from 
	 * encrypted file to traverse tree and output characters to the decrypted file.
	 * (method partly inspired from Algorithms 4th edition textbook)
	 */
 
   public static void decrypt(BinaryIn in, int[]frequencyArr, String outputFile) throws IOException {
	   
		Node huffmanNode;
		Node currRootNode;
		int count = 0;
		int bufferCounter = 0;
		int numOfBits = in.readInt();
		FileWriter myWriter = new FileWriter(outputFile);

		// reconstruct huffman tree
		huffmanNode = buildHuffmanTree(frequencyArr);

		// will keep traversing huffman tree until number of bits in the code array is exceeded 
		while((in.isEmpty() == false) && bufferCounter < numOfBits) {
			
			currRootNode = huffmanNode;
			
			while((currRootNode.isLeaf() != true) && in.isEmpty() == false) {
				
				if (in.readBoolean() == true) {
				currRootNode = currRootNode.right;
				}
				
				else {
					
				currRootNode = currRootNode.left;
				}
				
			}
			//write characters from each huffman tree leaf to dec file
			myWriter.write(currRootNode.character);
			bufferCounter++;
		}
		
		myWriter.flush();
   }
	
   // takes the character array from input and copies it to a frequency array (respective char at each freq index)
   public static int[] getFrequencyData(char[] inputDataCharArr) {
	   
	   int[] frequency = new int[charArrLength];
	   
	   for (char i : inputDataCharArr) {
	    frequency[i]++;
	   }
	   
	   return frequency;
   }

   /* this method builds the huffman tree from the frequency info using a max priority queue so that
    * when the merging happens to build the tree, simply need to poll() elements to combine (rather than
    * searching for the min frequencies).
    */
	private static Node buildHuffmanTree(int[] frequency) {
		
		PriorityQueue<Node> nodeQueue = new PriorityQueue<>();
		// works with getFrequencyData to create nodes for each character (with respective frequencies/chars stored)
		for (char i = 0; i < charArrLength; i++) {
			
			if (frequency[i] > 0) {
				// places nodes of chars with frequency values into priority queue (min frequency has priority)
				nodeQueue.add(new Node(frequency[i], i, null, null));
			}
		}
	
		while (nodeQueue.size() > 1) {
			
			// take two min nodes and merge them into new node.
			// since priority queue takes advantage of comparable type of node, simply poll the queue to
			// take smallest frequency in each node
			Node firstFreqNode = nodeQueue.poll();
			Node secondFreqNode = nodeQueue.poll();
			int newFreq = firstFreqNode.frequency + secondFreqNode.frequency;
			
			Node mergedNode = new Node(newFreq, '\0', firstFreqNode, secondFreqNode);
			nodeQueue.add(mergedNode);
		}

		// returns final node after merging done (aka parent node to all others)
		return nodeQueue.poll();
	}
	
   // buildCode inspired from Algorithms, 4th edition by Sedgewick and Wayne
   // method works to traverse the huffman tree and recursively assign binary values for each respective character
	
   private static String[] buildCode(Node root) {
	   
	   String[] st = new String [charArrLength];
	   buildCode(st, root, "");
	   return st;
   }
   
   private static void buildCode(String[] st, Node n, String s) {
	   
	   // once a leaf is reached, the string of the specific code is assigned to that character's index in 
	   // the string array
	   if (n.isLeaf()) {
		   
		   st[n.character] = s;
		   return;
	   }
	   // continue traversing the tree through children and marking 0 and 1 for left/right children respectively
	   buildCode(st, n.left, s + '0');
	   buildCode(st, n.right, s + '1');
   }
   
   /* recursive implementation of the node used in the huffman tree
    */
   static class Node implements Comparable<Node>{
	   
	   private final int frequency;
	   private final char character;
	   private Node left;
	   private Node right;
	   
	   Node (int frequency, char character, Node left, Node right) {
		   
		   this.frequency = frequency;
		   this.character = character;
		   this.left = left;
		   this.right = right;
	   }
	   
	   // check if specific node is a leaf based on children
	   public boolean isLeaf() {
		   
		   return (this.left == null) && (this.right == null);
	   }
	   @Override
	   // very important for the huffman priority queue which accesses this information indirectly to
	   // sort nodes based on frequency values 
	   public int compareTo(Node n) {
		
		  return Integer.compare(frequency, n.frequency);
	}
	   
   }

}
