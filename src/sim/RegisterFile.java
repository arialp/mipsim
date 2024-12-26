package sim;

/**
 * This class represents a MIPS register file, which contains 32 registers. Provides methods to read
 * and write register values and retrieve the current state of the registers.
 */
public class RegisterFile {
  private final int[] registers;
  private static final String[] registerNames = {"$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2",
                                                 "$a3", "$t0", "$t1", "$t2", "$t3", "$t4", "$t5",
                                                 "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4",
                                                 "$s5", "$s6", "$s7", "$t8", "$t9", "$k0", "$k1",
                                                 "$gp", "$sp", "$fp", "$ra"};

  /**
   * Initializes the register file with 32 registers, all set to 0 by default.
   */
  public RegisterFile() {
    this.registers = new int[32];
  }

  /**
   * Reads the value of a specific register.
   *
   * @param registerNumber The number of the register to read (0-31).
   *
   * @return The value stored in the specified register.
   *
   * @throws IndexOutOfBoundsException If the register number is out of range (not between 0 and
   * 31).
   */
  public int read(int registerNumber) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException("Invalid register number: " + registerNumber);
    }
    return registers[registerNumber];
  }

  /**
   * Writes a value to a specific register.
   *
   * @param registerNumber The number of the register to write to (0-31).
   *
   * @param value The value to store in the specified register.
   *
   * @throws IndexOutOfBoundsException If the register number is either out of range or is 0.
   */
  public void write(int registerNumber, int value) {
    if(registerNumber < 0 || registerNumber >= registers.length){
      throw new IndexOutOfBoundsException("Invalid register number: " + registerNumber);
    }
    if(registerNumber == 0) {
      throw new IndexOutOfBoundsException("Register $zero is read-only");
    }
    registers[registerNumber] = value;
  }

  /**
   * Retrieves the current state of all registers.
   *
   * @return A 2D array where each element contains the register name and its value. The value is
   * formatted in hexadecimal for the last four registers ($gp, $sp, $fp, $ra).
   */
  public String[][] getRegisterState() {
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
