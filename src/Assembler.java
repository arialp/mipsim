/**
 * The Assembler class converts MIPS assembly code into machine code. It handles label parsing,
 * binary conversion, and supports R, I, and J-type instructions.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assembler {

  /**
   * Map of labels and their corresponding memory addresses.
   */
  private static final Map<String, Integer> labelMap = new HashMap<>();

  /**
   * Map of assembly instructions to their binary opcodes.
   */
  private static final Map<String, String> instructionMap = new HashMap<>() {{
    // R-Format instructions
    put("add", "000000");
    put("sub", "000000");
    put("and", "000000");
    put("or", "000000");
    put("slt", "000000");
    put("sll", "000000");
    put("srl", "000000");

    // I-Format instructions
    put("addi", "001000");
    put("lw", "100011");
    put("sw", "101011");
    put("beq", "000100");
    put("bne", "000101");

    // J-Format instructions
    put("j", "000010");
    put("jal", "000011");
    put("jr", "000000"); // special case, handled separately
  }};

  /**
   * Map of R-type instructions to their binary function codes.
   */
  private static final Map<String, String> functMap = new HashMap<>() {{
    put("add", "100000");
    put("sub", "100010");
    put("and", "100100");
    put("or", "100101");
    put("slt", "101010");
    put("sll", "000000");
    put("srl", "000010");
    put("jr", "001000");
  }};

  /**
   * Assembles the given MIPS assembly code into machine code.
   *
   * @param assemblyCode The MIPS assembly code as a string.
   *
   * @return A list of binary machine code instructions.
   */
  public List<String> assemble(String assemblyCode) {
    List<String> binaryInstructions;
    String[] lines = assemblyCode.split("\n");
    List<String> assemblyLines = new ArrayList<>();

    // Remove comments and clean up assembly lines
    for(String line : lines){
      line = line.split("#")[0].trim(); // Remove comments
      if(!line.isEmpty()){
        assemblyLines.add(line);
      }
    }

    parseLabels(assemblyLines);
    binaryInstructions = convertToBinary(assemblyLines);

    return binaryInstructions;
  }

  /**
   * Parses labels in the assembly code and maps them to their corresponding memory addresses.
   *
   * @param assemblyLines A list of assembly code lines.
   */
  private static void parseLabels(List<String> assemblyLines) {
    int currentAddress = 0;
    for(String line : assemblyLines){
      line = line.trim();
      if(line.endsWith(":")){
        String label = line.substring(0, line.length() - 1).trim(); // Extract the label
        labelMap.put(label, currentAddress * 4 + 0x00400000); // Map label to memory address
      } else if(!line.isEmpty()){
        currentAddress++; // Increment address for non-label lines
      }
    }
  }

  /**
   * Converts assembly instructions to binary machine code.
   *
   * @param assemblyLines A list of assembly code lines.
   *
   * @return A list of binary machine code instructions.
   */
  public static List<String> convertToBinary(List<String> assemblyLines) {
    List<String> binaryCode = new ArrayList<>();
    int currentLine = 0;

    for(String line : assemblyLines){
      line = line.trim();
      if(line.isEmpty() || line.endsWith(":")) continue;

      String[] parts = line.split("[ ,]+");
      String instruction = parts[0];
      String binaryInstruction = instructionMap.getOrDefault(instruction, "000000");

      try{
        switch(instruction){
          // R-format instructions
          case "add":
          case "sub":
          case "and":
          case "or":
          case "slt":{
            String rd = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            String rt = registerToBinary(parts[3]);
            String funct = functMap.get(instruction);
            binaryCode.add(binaryInstruction + rs + rt + rd + "00000" + funct);
            break;
          }
          case "sll":
          case "srl":{
            String rd = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            int shiftAmount = Integer.parseInt(parts[3]);
            String sa = toBinary(shiftAmount, 5);
            String funct = functMap.get(instruction);
            binaryCode.add(binaryInstruction + "00000" + rt + rd + sa + funct);
            break;
          }

          // I-format instructions
          case "addi":{
            String rt = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            int immediate = Integer.parseInt(parts[3]);
            binaryCode.add(binaryInstruction + rs + rt + toBinary(immediate, 16));
            break;
          }
          case "lw":
          case "sw":{
            String rt = registerToBinary(parts[1]);
            String offsetAndRs = parts[2];
            String[] offsetParts = offsetAndRs.split("[()]");
            int offset = Integer.parseInt(offsetParts[0]);
            String rs = registerToBinary(offsetParts[1]);
            binaryCode.add(binaryInstruction + rs + rt + toBinary(offset, 16));
            break;
          }
          case "beq":
          case "bne":{
            String rs = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            String label = parts[3];
            Integer targetAddress = labelMap.get(label); // Label address

            if(targetAddress == null){
              System.err.println("Label not found: " + label);
            } else {
              // Calculate relative offset
              int currentPC = currentLine * 4 + 0x00400000;
              int relativeOffset = (targetAddress - (currentPC + 4)) / 4;
              binaryCode.add(binaryInstruction + rs + rt + toBinary(relativeOffset, 16));
            }
            break;
          }

          // J-format instructions
          case "j":
          case "jal":{
            String label = parts[1];
            Integer address = labelMap.get(label); // Absolute label address
            if(address == null){
              System.err.println("Label not found: " + label);
            } else {
              // Convert to 26-bit word address (remove bottom 2 bits and top 4 bits)
              int targetAddress = (address >> 2)&0x03FFFFFF;
              binaryCode.add(binaryInstruction + toBinary(targetAddress, 26));
            }
            break;
          }
          case "jr":{
            String rs = registerToBinary(parts[1]); // Register address
            String funct = functMap.get(instruction);
            binaryCode.add("000000" + rs + "00000" + "00000" + "00000" + funct);
            break;
          }

          default:{
            System.err.println("Unsupported instruction: " + instruction);
          }
        }
      } catch(NumberFormatException|ArrayIndexOutOfBoundsException e){
        System.err.println("Error processing instruction: " + line);
      }

      currentLine++;
    }
    return binaryCode;
  }

  /**
   * Converts a register name to its binary representation.
   *
   * @param register The name of the register (e.g., $t0, $a0).
   *
   * @return The binary representation of the register.
   */
  private static String registerToBinary(String register) {
    Map<String, String> registerMap = new HashMap<>() {{
      put("$zero", "00000");
      put("$at", "00001");
      put("$v0", "00010");
      put("$v1", "00011");
      put("$a0", "00100");
      put("$a1", "00101");
      put("$a2", "00110");
      put("$a3", "00111");
      put("$t0", "01000");
      put("$t1", "01001");
      put("$t2", "01010");
      put("$t3", "01011");
      put("$t4", "01100");
      put("$t5", "01101");
      put("$t6", "01110");
      put("$t7", "01111");
      put("$s0", "10000");
      put("$s1", "10001");
      put("$s2", "10010");
      put("$s3", "10011");
      put("$s4", "10100");
      put("$s5", "10101");
      put("$s6", "10110");
      put("$s7", "10111");
      put("$t8", "11000");
      put("$t9", "11001");
      put("$k0", "11010");
      put("$k1", "11011");
      put("$gp", "11100");
      put("$sp", "11101");
      put("$fp", "11110");
      put("$ra", "11111");
    }};
    return registerMap.getOrDefault(register, "00000");
  }

  /**
   * Converts an integer value to its binary representation with a specified number of bits.
   *
   * @param value The integer value to convert.
   * @param bits The number of bits for the binary representation.
   *
   * @return The binary representation of the value as a string.
   */
  private static String toBinary(int value, int bits) {
    StringBuilder binary = new StringBuilder(Integer.toBinaryString(value&((1 << bits) - 1)));
    while(binary.length() < bits){
      binary.insert(0, "0");
    }
    return binary.toString();
  }
}
