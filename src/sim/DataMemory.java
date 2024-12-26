package sim;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data memory of a MIPS simulator. Provides methods to load and store 32-bit values,
 * as well as retrieve the memory's current state.
 */
public class DataMemory {
  private static final int BASE_ADDRESS = 0xFFFFFFFF;
  private final int[] memory;

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
    if(index < 0 || index >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + Integer.toHexString(address));
    }
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
    if(index < 0 || index >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + Integer.toHexString(address));
    }
    memory[index] = value;
  }

  /**
   * Converts a memory address to an index in the memory array.
   *
   * @param address The memory address to convert.
   *
   * @return The corresponding index in the memory array.
   */
  private static int convertAddressToIndex(int address) {
    return (BASE_ADDRESS - address) / 4;
  }

  /**
   * Converts an index in the memory array to a memory address.
   *
   * @param index The index in the memory array.
   *
   * @return The corresponding memory address.
   */
  private static int convertIndexToAddress(int index) {
    return BASE_ADDRESS - (index * 4);
  }

  /**
   * Retrieves the current state of the memory, showing only non-empty addresses.
   *
   * @return A 2D array containing the address and its corresponding data value as a string.
   */
  public String[][] getMemoryState() {
    List<String[]> stateList = new ArrayList<>();

    for(int i = 0; i < memory.length; i++){
      if(memory[i] != 0){
        String address = String.format("0x%08X", convertIndexToAddress(i));
        String value = String.valueOf(memory[i]);
        stateList.add(new String[]{address, value});
      }
    }

    return stateList.toArray(new String[0][0]);
  }

}
