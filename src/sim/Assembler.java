/**
 * The sim.Assembler class converts MIPS assembly code into machine code. It handles label parsing,
 * binary conversion, and supports R, I, and J-type instructions.
 */
package sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assembler {
  public static class AssemblerException extends Exception {
    public AssemblerException(String message) {
      super(message);
    }
  }

  private static final String[] registerNames = {"$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2",
                                                 "$a3", "$t0", "$t1", "$t2", "$t3", "$t4", "$t5",
                                                 "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4",
                                                 "$s5", "$s6", "$s7", "$t8", "$t9", "$k0", "$k1",
                                                 "$gp", "$sp", "$fp", "$ra"};

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
  public List<String> assemble(String assemblyCode) throws AssemblerException {
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
  private static List<String> convertToBinary(List<String> assemblyLines) throws AssemblerException {
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

            if(shiftAmount > 31){
              throw new AssemblerException("Shift amount must be less than 32 in instruction: " + line);
            }

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

            if(immediate < -32768 || immediate > 32767){
              throw new AssemblerException("Immediate value out of range (-32768 to 32767): " + immediate + " in instruction: " + line);
            }

            binaryCode.add(binaryInstruction + rs + rt + toBinary(immediate, 16));
            break;
          }
          case "lw":
          case "sw":{
            String rt = registerToBinary(parts[1]);
            String offsetAndRs = parts[2];
            String[] offsetParts = offsetAndRs.split("[()]");
            int offset = Integer.parseInt(offsetParts[0]);

            if(offset < -32768 || offset > 32767){
              throw new AssemblerException("Offset value out of range (-32768 to 32767): " + offset + " in instruction: " + line);
            }

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
              throw new AssemblerException("Label not found: " + label + " in instruction: " + line);
            }
            // Calculate relative offset
            int currentPC = currentLine * 4 + 0x00400000;
            int relativeOffset = (targetAddress - (currentPC + 4)) / 4;
            binaryCode.add(binaryInstruction + rs + rt + toBinary(relativeOffset, 16));
            break;
          }
          // J-format instructions
          case "j":
          case "jal":{
            String label = parts[1];
            Integer address = labelMap.get(label); // Absolute label address
            if(address == null){
              throw new AssemblerException("Label not found: " + label + " in instruction: " + line);
            }
            // Compress 32 bits into 26 bits
            int targetAddress = (address >> 2)&0x03FFFFFF;
            binaryCode.add(binaryInstruction + toBinary(targetAddress, 26));
            break;
          }
          case "jr":{
            String rs = registerToBinary(parts[1]); // Register address
            String funct = functMap.get(instruction);
            binaryCode.add("000000" + rs + "00000" + "00000" + "00000" + funct);
            break;
          }
          default:{
            throw new AssemblerException("Unsupported instruction: " + instruction);
          }
        }
      } catch(NumberFormatException e){
        throw new AssemblerException("Invalid number format in instruction: " + line);
      } catch(ArrayIndexOutOfBoundsException e){
        throw new AssemblerException("Invalid instruction format: " + line);
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
    Map<String, String> registerMap = new HashMap<>();
    for(int i = 0; i < registerNames.length; i++){
      String binary = String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
      registerMap.put(registerNames[i], binary);
    }

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
