public class RegisterFile {
  private final int[] registers;

  public RegisterFile() {
    this.registers = new int[32]; // 32 register, başlangıç değeri 0
  }

  public int read(int registerNumber) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException("Invalid register number: " + registerNumber);
    }
    return registers[registerNumber];
  }

  public void write(int registerNumber, int value) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException("Invalid register number: " + registerNumber);
    }
    registers[registerNumber] = value;
  }

  public String[][] getRegisterState() {
    String[] registerNames = {"$zero:", "$at:", "$v0:", "$v1:", "$a0:", "$a1:", "$a2:", "$a3:",
                              "$t0:", "$t1:", "$t2:", "$t3:", "$t4:", "$t5:", "$t6:", "$t7:",
                              "$s0:", "$s1:", "$s2:", "$s3:", "$s4:", "$s5:", "$s6:", "$s7:",
                              "$t8:", "$t9:", "$k0:", "$k1:", "$gp:", "$sp:", "$fp:", "$ra:"};
    String[][] state = new String[registers.length][2];
    for(int i = 0; i < registers.length; i++){
      state[i][0] = registerNames[i];
      state[i][1] = (registers.length - i <= 4) ?
                    String.format("0x%08X", registers[i]) :
                    String.valueOf(registers[i]);
    }
    return state;
  }
}
