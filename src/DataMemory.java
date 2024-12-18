/**
 * Represents the data memory of a MIPS simulator. Provides methods to load and store 32-bit values,
 * as well as retrieve the memory's current state.
 */
public class DataMemory {
  private final int[] memory;

  /**
   * The base address of the memory stack (0xFFFFFFFF).
   */
  private static final int BASE_ADDRESS = 0xFFFFFFFF;

  /**
   * Initializes the data memory with a default size of 128 words (512 bytes).
   */
  public DataMemory() {
    this(128); // Default size: 128 words
  }

  /**
   * Initializes the data memory with the specified size.
   *
   * @param size The size of the memory in bytes. Must be a multiple of 4. Must not exceed 1 MB.
   */
  public DataMemory(int size) {
    if(size % 4 != 0){
      throw new IllegalArgumentException("Memory size must be a multiple of 4 bytes");
    }
    if(size > 1048576){
      throw new IllegalArgumentException("Memory size must not exceed 1 MB");
    }
    this.memory = new int[size / 4];
  }

  /**
   * Loads a 32-bit value from the specified memory address.
   *
   * @param address The memory address to load from.
   *
   * @return The 32-bit value stored at the specified address.
   *
   * @throws IndexOutOfBoundsException If the address is invalid or out of range.
   */
  public int load(int address) {
    int index = convertAddressToIndex(address);
    return memory[index];
  }

  /**
   * Stores a 32-bit value at the specified memory address.
   *
   * @param address The memory address to store the value at.
   * @param value The 32-bit value to store.
   *
   * @throws IndexOutOfBoundsException If the address is invalid or out of range.
   */
  public void store(int address, int value) {
    int index = convertAddressToIndex(address);
    memory[index] = value;
  }

  /**
   * Converts a memory address to an index in the memory array.
   *
   * @param address The memory address to convert.
   *
   * @return The corresponding index in the memory array.
   *
   * @throws IndexOutOfBoundsException If the address is invalid or out of range.
   */
  private int convertAddressToIndex(int address) {
    int offset = (BASE_ADDRESS - address) / 4; // Stack grows downward
    if(offset < 0 || offset >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + Integer.toHexString(address));
    }
    return offset;
  }

  /**
   * Retrieves the current state of the memory, showing only non-empty addresses.
   *
   * @return A formatted string showing the address, binary representation of each byte, and the
   * decimal value for each non-empty memory address.
   */
  public String getMemoryState() {
    StringBuilder state = new StringBuilder();

    // Header row
    state.append("Address     Byte 1   Byte 2   Byte 3   Byte 4   Decimal Value\n");

    for(int i = 0; i < memory.length; i++){
      int address = 0xFFFFFFFF - (i * 4); // Memory address
      int data = memory[i]; // Data stored in memory

      if(data != 0){ // Only non-empty addresses
        // Split 32-bit data into 8-bit parts
        String[] dataBytes = new String[4];
        for(int j = 0; j < 4; j++){
          dataBytes[j] = String.format("%8s", Integer.toBinaryString((data >> (24 - j * 8))&0xFF))
                               .replace(' ', '0'); // Format each byte in binary
        }

        // Append address, data bytes, and decimal value
        state.append(String.format("0x%08X: %s %s %s %s %d\n", address, dataBytes[0], dataBytes[1],
                                   dataBytes[2], dataBytes[3], data));
      }
    }

    return state.toString();
  }

}
