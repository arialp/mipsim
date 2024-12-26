package sim;

import java.util.List;

/**
 * This class represents the instruction memory of a MIPS simulator. It stores the binary
 * instructions and provides methods to access them by address.
 */
public class InstructionMemory {
  private static final int BASE_ADDRESS = 0x00400000;
  private final String[] memory;

  /**
   * Constructs an sim.InstructionMemory instance with the given list of binary instructions.
   *
   * @param instructionList A list of binary instructions to be stored.
   *
   * @throws IllegalArgumentException If the instruction list exceeds 128 lines (512 bytes).
   */
  public InstructionMemory(List<String> instructionList) {
    this(instructionList, 128);
  }

  /**
   * Constructs an sim.InstructionMemory instance with the given list of binary instructions and size.
   *
   * @param instructionList A list of binary instructions to be stored.
   * @param size The maximum size in bytes that can be stored in the memory. Must be a multiple of
   * 4. Must not exceed 1 MB.
   *
   * @throws IllegalArgumentException If the instruction list exceeds the specified size, the size is not a multiple of 4, or the size exceeds 1 MB.
   */
  public InstructionMemory(List<String> instructionList, int size) {
    if(instructionList.size() > size / 4){
      throw new IllegalArgumentException(
              "Instruction list exceeds " + size + " lines (" + (size * 4) + " bytes limit)");
    }
    if(size > 1048576){
      throw new IllegalArgumentException("Memory size must not exceed 1 MB");
    }
    if(size % 4 != 0){
      throw new IllegalArgumentException("Memory size must be a multiple of 4 bytes");
    }

    memory = new String[instructionList.size()];

    for(int i = 0; i < instructionList.size(); i++){
      memory[i] = instructionList.get(i);
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
  public String load(int address) {
    int index = convertAddressToIndex(address);
    if(index < 0 || index >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid instruction address: " + Integer.toHexString(address));
    }
    return memory[index];
  }

  /**
   * Converts a memory address to an index in the instruction array.
   *
   * @param address The memory address to convert.
   *
   * @return The corresponding index in the instruction array.
   */
  private static int convertAddressToIndex(int address) {
    return (address - BASE_ADDRESS) / 4;
  }

  /**
   * Converts an index in the instruction array to a memory address.
   *
   * @param index The index in the instruction array.
   *
   * @return The corresponding memory address.
   */
  private static int convertIndexToAddress(int index) {
    return BASE_ADDRESS + (index * 4);
  }

  /**
   * Retrieves the size of the instruction memory (number of instructions).
   *
   * @return The number of instructions stored in the memory.
   */
  public int size() {
    return memory.length;
  }

  /**
   * Generates a formatted string representing the current state of the instruction memory.
   *
   * @return A formatted string showing the instructions, their addresses, and the current PC.
   */
  public String[][] getInstructionMemoryState() {
    String[][] state = new String[memory.length][2];

    for(int i = 0; i < memory.length; i++){
      String address = String.format("0x%08X", convertIndexToAddress(i));
      String instruction = memory[i];

      state[i][0] = address;
      state[i][1] = instruction;
    }

    return state;
  }
}
