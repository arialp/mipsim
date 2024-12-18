import java.util.List;

/**
 * This class represents the instruction memory of a MIPS simulator. It stores the binary
 * instructions and provides methods to access them by address.
 */
public class InstructionMemory {
  private final String[] instructions;

  /**
   * Constructs an InstructionMemory instance with the given list of binary instructions.
   *
   * @param instructionList A list of binary instructions to be stored.
   *
   * @throws IllegalArgumentException If the instruction list exceeds 128 lines (512 bytes).
   */
  public InstructionMemory(List<String> instructionList) {
    this(instructionList, 128);
  }

  /**
   * Constructs an InstructionMemory instance with the given list of binary instructions and size.
   *
   * @param instructionList A list of binary instructions to be stored.
   * @param size The maximum size in bytes that can be stored in the memory.
   *
   * @throws IllegalArgumentException If the instruction list exceeds the specified size.
   */
  public InstructionMemory(List<String> instructionList, int size) {
    if(instructionList.size() > size / 4){
      throw new IllegalArgumentException("Instruction list exceeds " + size + " lines (" + (size * 4) + " bytes limit)");
    }

    instructions = new String[instructionList.size()];

    for(int i = 0; i < instructionList.size(); i++){
      instructions[i] = instructionList.get(i);
    }
  }

  /**
   * Retrieves the instruction at the specified address.
   *
   * @param address The memory address of the instruction (must be aligned to 4 bytes).
   *
   * @return The binary instruction at the specified address.
   *
   * @throws IndexOutOfBoundsException If the address is out of bounds.
   */
  public String getInstruction(int address) {
    int index = convertAddressToIndex(address);
    if(index < 0 || index >= instructions.length){
      throw new IndexOutOfBoundsException(
              "Invalid instruction address: " + Integer.toHexString(address));
    }
    return instructions[index];
  }

  /**
   * Converts a memory address to an index in the instruction array.
   *
   * @param address The memory address to convert.
   *
   * @return The corresponding index in the instruction array.
   */
  static private int convertAddressToIndex(int address) {
    int baseAddress = 0x00400000;
    return (address - baseAddress) / 4;
  }

  /**
   * Converts an index in the instruction array to a memory address.
   *
   * @param index The index in the instruction array.
   *
   * @return The corresponding memory address.
   */
  static private int convertIndexToAddress(int index) {
    int baseAddress = 0x00400000;
    return baseAddress + (index * 4);
  }

  /**
   * Retrieves the size of the instruction memory (number of instructions).
   *
   * @return The number of instructions stored in the memory.
   */
  public int size() {
    return instructions.length;
  }

  /**
   * Generates a formatted string representing the current state of the instruction memory.
   *
   * @return A formatted string showing the instructions, their addresses, and the current PC.
   */
  public String[][] getInstructionMemoryState() {
    String[][] state = new String[instructions.length][2];

    for(int i = 0; i < instructions.length; i++){
      String address = String.format("0x%08X", convertIndexToAddress(i));
      String instruction = instructions[i];

      state[i][0] = address;
      state[i][1] = instruction;
    }

    return state;
  }
}
