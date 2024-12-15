import java.util.List;

public class InstructionMemory {
  private List<String> instructions;

  public InstructionMemory(List<String> instructions) {
    this.instructions = instructions;
  }

  public String getInstruction(int address) {
    if(address < 0 || address >= instructions.size()){
      throw new IndexOutOfBoundsException(
              "Invalid instruction address: " + address);
    }
    return instructions.get(address);
  }

  public int size() {
    return instructions.size();
  }
}
