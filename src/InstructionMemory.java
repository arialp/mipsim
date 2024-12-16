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
    if(instructionList.size() > 128){
      throw new IllegalArgumentException("Instruction list exceeds 128 lines (512 bytes limit)");
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
    return instructions[index];
  }

  /**
   * Converts a memory address to an index in the instruction array.
   *
   * @param address The memory address to convert.
   *
   * @return The corresponding index in the instruction array.
   *
   * @throws IndexOutOfBoundsException If the address is invalid or out of range.
   */
  private int convertAddressToIndex(int address) {
    int baseAddress = 0x00400000;
    int offset = (address - baseAddress) / 4;
    if(offset < 0 || offset >= instructions.length){
      throw new IndexOutOfBoundsException(
              "Invalid instruction address: " + Integer.toHexString(address));
    }
    return offset;
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
   * @param programCounter The current program counter value.
   *
   * @return A formatted string showing the instructions, their addresses, and the current PC.
   */
  public String getInstructionMemoryState(int programCounter) {
    StringBuilder state = new StringBuilder();

    state.append(String.format("Address     Byte 1   Byte 2   Byte 3   Byte 4   PC = 0x%08X\n",
                               programCounter));

    for(int i = 0; i < instructions.length; i++){
      int address = 0x00400000 + (i * 4); // Instruction address
      String instruction = instructions[i];

      // Split 32-bit instruction into 8-bit segments
      String[] instructionParts = new String[4];
      for(int j = 0; j < 4; j++){
        instructionParts[j] = instruction.substring(j * 8, (j + 1) * 8);
      }

      // Append address and instruction parts
      state.append(String.format("0x%08X: %s %s %s %s", address, instructionParts[0],
                                 instructionParts[1], instructionParts[2], instructionParts[3]));

      // Highlight the current PC address
      if(address == programCounter){
        state.append(" <- PC");
      }

      state.append("\n");
    }

    return state.toString();
  }

}
