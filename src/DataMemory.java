public class DataMemory {
  private final int[] memory;

  // Belleğin başlangıç adresi: 0x0FFFFFFF
  private static final int BASE_ADDRESS = 0x0FFFFFFF;

  public DataMemory() {
    this(128); // Default size: 128 word (512 byte)
  }

  public DataMemory(int size) {
    this.memory = new int[size / 4];
  }

  // Bellekten veri okuma
  public int load(int address) {
    int index = convertAddressToIndex(address);
    return memory[index];
  }

  // Belleğe veri yazma
  public void store(int address, int value) {
    int index = convertAddressToIndex(address);
    memory[index] = value;
  }

  // Adresi indekse çevirme
  private int convertAddressToIndex(int address) {
    int offset = (BASE_ADDRESS - address) / 4; // Stack aşağı doğru büyür
    if(offset < 0 || offset >= memory.length){
      throw new IndexOutOfBoundsException(
              "Invalid memory address: " + Integer.toHexString(address));
    }
    return offset;
  }

  public String getMemoryState() {
    StringBuilder state = new StringBuilder();

    // Başlık satırı
    state.append("Address     Byte 1   Byte 2   Byte 3   Byte 4   Decimal Value\n");

    for(int i = 0; i < memory.length; i++){
      int address = 0x0FFFFFFF - (i * 4); // Bellek adresi
      int data = memory[i]; // Bellekteki veri

      if(data != 0){ // Yalnızca dolu adresler
        // 32 biti 8'er bitlik parçalar halinde ayır
        String[] dataBytes = new String[4];
        for(int j = 0; j < 4; j++){
          dataBytes[j] = String.format("%8s", Integer.toBinaryString((data >> (24 - j * 8))&0xFF))
                               .replace(' ', '0'); // Her byte'ı binary formatında al
        }

        // Adres, veriler ve decimal değer formatlı şekilde eklenir
        state.append(
                String.format("0x%08X: %s %s %s %s %d\n", address, dataBytes[0], dataBytes[1],
                              dataBytes[2], dataBytes[3], data));
      }
    }

    return state.toString();
  }

}
