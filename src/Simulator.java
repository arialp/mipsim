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
  private boolean isBranchOrJump;
  private boolean isFinished;
  private final int stackPointerDefaultValue = 0xFFFFFFFF;

  private String opcode, instruction;
  private int rs, rt, rd, shamt, funct, immediate, targetAddress;

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
  }

  /**
   * Executes the next instruction in the program. Updates the program counter unless a branch or
   * jump instruction modifies it.
   */
  public void step() {
    isBranchOrJump = false;
    isFinished = false;
    opcode = "";
    instruction = "";
    rs = rt = rd = shamt = funct = immediate = targetAddress = 0;

    if(programCounter >= 0x00400000 + instructionMemory.size() * 4){
      System.out.println("Program Finished.");
      isFinished = true;
      return;
    }

    fetch();
    decode();
    execute();

    // PC increment is handled by branch and jump instructions
    if(!isBranchOrJump){
      programCounter += 4;
    }
  }

  /**
   * Fetches the instruction at the current program counter from instruction memory.
   *
   * @throws IllegalStateException If the program counter is out of bounds.
   */
  private void fetch() {
    // Check if the program counter is valid
    if(programCounter < 0x00400000 || programCounter >= 0x00400000 + instructionMemory.size() * 4){
      throw new IllegalStateException(
              String.format("Program counter out of bounds: 0x%08X", programCounter));
    }

    // Fetch the instruction from instruction memory
    instruction = instructionMemory.load(programCounter);
  }

  /**
   * Decodes the given instruction into its components and stores them in global variables.
   */
  private void decode() {
    opcode = instruction.substring(0, 6);

    // Decode fields common to most instructions
    rs = Integer.parseInt(instruction.substring(6, 11), 2);
    rt = Integer.parseInt(instruction.substring(11, 16), 2);

    // Handle specific fields based on instruction type
    switch(opcode){
      case "000000": // R-Type instructions
        rd = Integer.parseInt(instruction.substring(16, 21), 2);
        shamt = Integer.parseInt(instruction.substring(21, 26), 2);
        funct = Integer.parseInt(instruction.substring(26, 32), 2);
        break;

      case "001000": // I-Type (addi)
      case "100011": // I-Type (lw)
      case "101011": // I-Type (sw)
      case "000100": // I-Type (beq)
      case "000101": // I-Type (bne)
        immediate = Integer.parseInt(instruction.substring(16), 2);
        // Sign-extend immediate if necessary
        if((immediate&0x8000) != 0){
          immediate |= 0xFFFF0000;
        }
        break;

      case "000010": // J-Type (j)
      case "000011": // J-Type (jal)
        targetAddress = Integer.parseInt(instruction.substring(6), 2);
        break;

      default:
        System.out.println("Unsupported Opcode: " + opcode);
    }
  }

  /**
   * Executes the previously decoded instruction based on the global variables.
   */
  private void execute() {
    switch(opcode){
      case "000000": // R-Type instructions
        execR();
        break;
      case "001000": // addi
        addi();
        break;
      case "100011": // lw
        lw();
        break;
      case "101011": // sw
        sw();
        break;
      case "000100": // beq
        beq();
        break;
      case "000101": // bne
        bne();
        break;
      case "000010": // j
        jump();
        break;
      case "000011": // jal
        jal();
        break;
      default:
        System.out.println("Unsupported Opcode: " + opcode);
    }
  }

  /**
   * Executes the R-Type instructions. Uses global variables `rs`, `rt`, `rd`, `shamt`, and `funct`
   * to determine the operation and registers involved. Updates the register file accordingly.
   */
  private void execR() {
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
        System.out.println("Unsupported R-Type Function Code: " + funct);
    }
  }

  /**
   * Executes the addi instruction. Uses global variables `rs`, `rt`, and `immediate` to perform the
   * operation and updates the target register.
   */
  private void addi() {
    registerFile.write(rt, registerFile.read(rs) + immediate);
  }

  /**
   * Executes the lw (load word) instruction. Uses global variables `rs`, `rt`, and `immediate` to
   * calculate the memory address and load the value into the target register.
   */
  private void lw() {
    int address = registerFile.read(rs) - immediate; // Calculate the effective address
    registerFile.write(rt, dataMemory.load(address)); // Load value from memory into the register
  }

  /**
   * Executes the sw (store word) instruction. Uses global variables `rs`, `rt`, and `immediate` to
   * calculate the memory address and store the value from the target register into memory.
   */
  private void sw() {
    int address = registerFile.read(rs) - immediate; // Calculate the effective address
    dataMemory.store(address, registerFile.read(rt)); // Store the value into memory
  }

  /**
   * Executes the beq (branch if equal) instruction. Uses global variables `rs`, `rt`, and
   * `immediate` to determine whether to branch and updates the program counter if the condition is
   * met.
   */
  private void beq() {
    if(registerFile.read(rs) == registerFile.read(rt)){
      programCounter = programCounter + 4 + (immediate * 4); // Branch to target address
      isBranchOrJump = true;
    }
  }

  /**
   * Executes the bne (branch if not equal) instruction. Uses global variables `rs`, `rt`, and
   * `immediate` to determine whether to branch and updates the program counter if the condition is
   * met.
   */
  private void bne() {
    if(registerFile.read(rs) != registerFile.read(rt)){
      programCounter = programCounter + 4 + (immediate * 4); // Branch to target address
      isBranchOrJump = true;
    }
  }

  /**
   * Executes the j (jump) instruction. Uses global variable `targetAddress` to calculate and update
   * the program counter to the target address.
   */
  private void jump() {
    programCounter =
            (programCounter&0xF0000000)|(targetAddress << 2); // Compute absolute jump address
    isBranchOrJump = true;
  }

  /**
   * Executes the jal (jump and link) instruction. Uses global variable `targetAddress` to calculate
   * the target address. Saves the return address in the $ra register.
   */
  private void jal() {
    registerFile.write(31, programCounter + 4); // Save return address in $ra
    programCounter =
            (programCounter&0xF0000000)|(targetAddress << 2); // Compute absolute jump address
    isBranchOrJump = true;
  }

  /**
   * Resets the simulator to its initial state. Resets the program counter, data memory, and
   * register file. Sets the stack pointer to its default value and marks the simulation as not
   * finished.
   */
  public void reset() {
    this.isBranchOrJump = false;
    this.isFinished = false;
    this.opcode = "";
    this.instruction = "";
    this.rs = this.rt = this.rd = this.shamt = this.funct = this.immediate = this.targetAddress = 0;
    this.programCounter = 0x00400000;
    this.dataMemory = new DataMemory();
    this.registerFile = new RegisterFile();
    registerFile.write(29, stackPointerDefaultValue); // stack pointer default value
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
    return instructionMemory.load(address);
  }

  public boolean isFinished() {
    return isFinished;
  }

  public int getProgramCounter() {
    return programCounter;
  }
}
