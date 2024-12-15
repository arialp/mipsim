import java.util.List;

public class InstructionMemory {
  private final String[] instructions;

  // Constructor: Assembly talimatlarını alır ve 128 adres kapasitesinde kontrol eder
  public InstructionMemory(List<String> instructionList) {
    if(instructionList.size() > 128){
      throw new IllegalArgumentException("Instruction list exceeds 128 lines (512 bytes limit)");
    }

    instructions = new String[instructionList.size()];

    for(int i = 0; i < instructionList.size(); i++){
      instructions[i] = instructionList.get(i);
    }
  }

  // Adrese göre instruction'ı döner
  public String getInstruction(int address) {
    int index = convertAddressToIndex(address);
    return instructions[index];
  }

  // Adresi indekse çevirir: 0x00400000 -> index 0, 0x00400004 -> index 1
  private int convertAddressToIndex(int address) {
    int baseAddress = 0x00400000;
    int offset = (address - baseAddress) / 4;
    if(offset < 0 || offset >= instructions.length){
      throw new IndexOutOfBoundsException(
              "Invalid instruction address: " + Integer.toHexString(address));
    }
    return offset;
  }

  public int size() {
    return instructions.length;
  }

  public String getInstructionMemoryState(int programCounter) {
    StringBuilder state = new StringBuilder();

    // Başlık satırı
    state.append(String.format("Address     Byte 1   Byte 2   Byte 3   Byte 4   PC = 0x%08X\n",
                               programCounter));

    for(int i = 0; i < instructions.length; i++){
      int address = 0x00400000 + (i * 4); // Instruction adresi
      String instruction = instructions[i];

      // 32 biti 8'er bitlik parçalara böl
      String[] instructionParts = new String[4];
      for(int j = 0; j < 4; j++){
        instructionParts[j] = instruction.substring(j * 8, (j + 1) * 8);
      }

      // Adres ve instruction parçalarını yazdır
      state.append(String.format("0x%08X: %s %s %s %s", address, instructionParts[0],
                                 instructionParts[1], instructionParts[2], instructionParts[3]));

      // Program Counter ile eşleşen adresin yanına " <- PC" ekle
      if(address == programCounter){
        state.append(" <- PC");
      }

      state.append("\n");
    }

    return state.toString();
  }

}
