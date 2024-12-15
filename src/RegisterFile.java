public class RegisterFile {
  private int[] registers;

  public RegisterFile() {
    this.registers = new int[32]; // 32 register, başlangıç değeri 0
  }

  public int read(int registerNumber) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException(
              "Invalid register number: " + registerNumber);
    }
    return registers[registerNumber];
  }

  public void write(int registerNumber, int value) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException(
              "Invalid register number: " + registerNumber);
    }
    registers[registerNumber] = value;
  }

  public String getRegisterState() {
    StringBuilder state = new StringBuilder();
    for(int i = 0; i < registers.length; i++){
      state.append("$").append(i).append(": ").append(registers[i])
           .append("\n");
    }
    return state.toString();
  }
}
