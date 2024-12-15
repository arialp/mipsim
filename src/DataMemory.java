public class DataMemory {
  private int[] memory;

  public DataMemory(int size) {
    this.memory = new int[size];
  }

  public int load(int address) {
    if(address < 0 || address >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + address);
    }
    return memory[address];
  }

  public void store(int address, int value) {
    if(address < 0 || address >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + address);
    }
    memory[address] = value;
  }

  public String getMemoryState() {
    StringBuilder state = new StringBuilder();
    for(int i = 0; i < memory.length; i++){
      if(memory[i] != 0){
        state.append("[").append(i).append("]: ").append(memory[i])
             .append("\n");
      }
    }
    return state.toString();
  }
}
