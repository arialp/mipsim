import java.util.*;

public class Assembler {
  public List<String> assemble(String assemblyCode) {
    List<String> binaryInstructions = new ArrayList<>();
    AssemblyToBinary converter = new AssemblyToBinary();
    String[] lines = assemblyCode.split("\n");
    List<String> assemblyLines = Arrays.asList(lines);

    binaryInstructions = AssemblyToBinary.convertToBinary(
            assemblyLines);

    return binaryInstructions;
  }
}
