// general Huffman encoding/decoding interface
public interface Huffman {

   public void encode(String inputFile, String outputFile, String freqFile);
   
   public void decode(String inputFile, String outputFile, String freqFile);
}
