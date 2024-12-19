import java.util.List;

/**
 * Simulator class that executes MIPS assembly instructions. Manages the program counter, register
 * file, data memory, and instruction memory. Provides methods to execute instructions step-by-step
 * or reset the simulation.
 */
public class Simulator {
  private final InstructionMemory instructionMemory;
  private DataMemory dataMemory;
  private RegisterFile registerFile;
  private int programCounter;
  private int stackPointerDefaultValue = 0xFFFFFFFF;
  private boolean isBranchOrJump;
  private boolean isFinished;

  /**
   * Constructor: Assembles the given MIPS assembly code, Loads assembled instructions into
   * instruction memory, Initializes data memory and register file.
   *
   * @param assemblyCode MIPS assembly code to be executed
   */
  public Simulator(String assemblyCode) {
    Assembler assembler = new Assembler();
    List<String> binaryInstructions = assembler.assemble(assemblyCode);

    this.instructionMemory = new InstructionMemory(binaryInstructions);
    this.dataMemory = new DataMemory();
    this.registerFile = new RegisterFile();
    this.programCounter = 0x00400000; // Program counter starts at 0x00400000

    registerFile.write(29, stackPointerDefaultValue); // Stack starts at 0xFFFFFFFF
    isFinished = false;
  }

  /**
   * Executes the next instruction in the program. Updates the program counter unless a branch or
   * jump instruction modifies it.
   */
  public void executeNextStep() {
    if(programCounter >= 0x00400000 + instructionMemory.size() * 4){
      System.out.println("Program Finished.");
      isFinished = true;
      return;
    }

    isBranchOrJump = false;

    String instruction = instructionMemory.getInstruction(programCounter);
    decodeAndExecute(instruction);

    // PC increment is handled by branch and jump instructions
    if(!isBranchOrJump){
      programCounter += 4;
    }
  }

  /**
   * Decodes the given MIPS instruction and executes it. Determines the instruction type based on
   * the opcode and calls the appropriate execution method.
   *
   * @param instruction The binary string representation of the MIPS instruction to be executed
   */
  private void decodeAndExecute(String instruction) {
    String opcode = instruction.substring(0, 6); // 31-26 bits are opcode

    switch(opcode){
      case "000000": // R-Type instructions (add, sub, and, or, slt, sll, srl) and jr
        executeRFormat(instruction);
        break;
      case "001000": // addi
        executeAddi(instruction);
        break;
      case "100011": // lw
        executeLw(instruction);
        break;
      case "101011": // sw
        executeSw(instruction);
        break;
      case "000100": // beq
        executeBeq(instruction);
        break;
      case "000101": // bne
        executeBne(instruction);
        break;
      case "000010": // j
        executeJ(instruction);
        break;
      case "000011": // jal
        executeJal(instruction);
        break;
      default:
        System.out.println("Unsupported Opcode: " + opcode);
    }
  }

  /**
   * Executes the R-Type instructions. Decodes the instruction to determine the source, target, and
   * destination registers, the shift amount, and the function code. Executes the appropriate
   * operation based on the function code.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeRFormat(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2); // Source register
    int rt = Integer.parseInt(instruction.substring(11, 16), 2); // Target register
    int rd = Integer.parseInt(instruction.substring(16, 21), 2); // Destination register
    int shamt = Integer.parseInt(instruction.substring(21, 26), 2); // Shift amount
    int funct = Integer.parseInt(instruction.substring(26, 32), 2); // Function code

    switch(funct){
      case 32: // add
        registerFile.write(rd, registerFile.read(rs) + registerFile.read(rt));
        break;
      case 34: // sub
        registerFile.write(rd, registerFile.read(rs) - registerFile.read(rt));
        break;
      case 36: // and
        registerFile.write(rd, registerFile.read(rs)&registerFile.read(rt));
        break;
      case 37: // or
        registerFile.write(rd, registerFile.read(rs)|registerFile.read(rt));
        break;
      case 42: // slt
        registerFile.write(rd, (registerFile.read(rs) < registerFile.read(rt)) ? 1 : 0);
        break;
      case 0: // sll
        registerFile.write(rd, registerFile.read(rt) << shamt);
        break;
      case 2: // srl
        registerFile.write(rd, registerFile.read(rt) >>> shamt);
        break;
      case 8: // jr
        programCounter = registerFile.read(rs);
        isBranchOrJump = true;
        break;
      default:
        System.out.println("Unsupported R-Type Function Code " + funct);
    }
  }

  /**
   * Executes the addi instruction. Decodes the instruction to determine the source register, target
   * register, and immediate value. Adds the immediate value to the value in the source register and
   * stores the result in the target register.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeAddi(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int immediate = Integer.parseInt(instruction.substring(16), 2);

    registerFile.write(rt, registerFile.read(rs) + immediate);
  }

  /**
   * Executes the lw (load word) instruction. Decodes the instruction to determine the source
   * register, target register, and offset. Loads the word from memory at the address calculated by
   * subtracting the offset to the value in the source register, and stores it in the target
   * register.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeLw(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Check the sign of the offset and sign-extend it, 16 -> 32
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign extend
    }

    // Determine the address: Subtract the offset from the stack pointer, stack grows downwards
    int address = registerFile.read(rs) - offset;

    // $rt = Memory[address]
    registerFile.write(rt, dataMemory.load(address));
  }

  /**
   * Executes the sw (store word) instruction. Decodes the instruction to determine the source
   * register, target register, and offset. Stores the word from the target register into memory at
   * the address calculated by subtracting the offset from the value in the source register.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeSw(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Check the sign of the offset and sign-extend it, 16 -> 32
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign extend
    }

    // Determine the address: Subtract the offset from the stack pointer, stack grows downwards
    int address = registerFile.read(rs) - offset;

    // Memory[address] = $rt
    dataMemory.store(address, registerFile.read(rt));
  }

  /**
   * Executes the beq (branch if equal) instruction. Decodes the instruction to determine the source
   * register, target register, and offset. If the values in the source and target registers are
   * equal, updates the program counter to the address calculated by adding the offset to the
   * current program counter.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeBeq(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Check the sign of the offset and sign-extend it, 16 -> 32
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign-extend
    }

    if(registerFile.read(rs) == registerFile.read(rt)){
      programCounter = programCounter + 4 + (offset * 4); // PC + 4 + (offset << 2)
      isBranchOrJump = true;
    }
  }

  /**
   * Executes the (branch if not equal) instruction. Decodes the instruction to determine the source
   * register, target register, and offset. If the values in the source and target registers are not
   * equal, updates the program counter to the address calculated by adding the offset to the
   * current program counter.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeBne(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Check the sign of the offset and sign-extend it, 16 -> 32
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign-extend
    }

    if(registerFile.read(rs) != registerFile.read(rt)){
      programCounter = programCounter + 4 + (offset * 4); // PC + 4 + (offset << 2)
      isBranchOrJump = true;
    }
  }

  /**
   * Executes the j (jump) instruction. Decodes the instruction to determine the target address.
   * Updates the program counter to the target address.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeJ(String instruction) {
    int targetAddress = Integer.parseInt(instruction.substring(6), 2);

    // Extend targetAddress to 32 bits:
    // 1. Shift left by 2 to make it 28 bits (restore word alignment)
    // 2. Combine with PC[31:28] (current top 4 bits of PC)
    programCounter = (programCounter&0xF0000000)|targetAddress << 2;
    isBranchOrJump = true;
  }

  /**
   * Executes the jal (jump and link) instruction. Decodes the instruction to determine the target
   * address. Saves the return address (current PC + 4) in the $ra register. Updates the program
   * counter to the target address.
   *
   * @param instruction The binary string representation of the instruction to be executed
   */
  private void executeJal(String instruction) {
    int targetAddress = Integer.parseInt(instruction.substring(6), 2);
    registerFile.write(31, programCounter + 4); // Return address in $ra

    // Extend targetAddress to 32 bits:
    // 1. Shift left by 2 to make it 28 bits (restore word alignment)
    // 2. Combine with PC[31:28] (current top 4 bits of PC)
    programCounter = (programCounter&0xF0000000)|targetAddress << 2;
    isBranchOrJump = true;
  }

  /**
   * Resets the simulator to its initial state. Resets the program counter, data memory, and
   * register file. Sets the stack pointer to its default value and marks the simulation as not
   * finished.
   */
  public void reset() {
    this.programCounter = 0x00400000;
    this.dataMemory = new DataMemory();
    this.registerFile = new RegisterFile();
    registerFile.write(29, stackPointerDefaultValue); // stack pointer default value
    isFinished = false;
  }

  // Getters
  public String[][] getRegisterState() {
    return registerFile.getRegisterState();
  }

  public String[][] getDataMemoryState() {
    return dataMemory.getMemoryState();
  }

  public String[][] getInstructionMemoryState() {
    return instructionMemory.getInstructionMemoryState();
  }

  public int getInstructionMemorySize() {
    return instructionMemory.size();
  }

  public String getInstruction(int address) {
    return instructionMemory.getInstruction(address);
  }

  public boolean isFinished() {
    return isFinished;
  }

  public int getProgramCounter() {
    return programCounter;
  }
}
