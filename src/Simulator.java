import java.util.List;

public class Simulator {
  private final InstructionMemory instructionMemory;
  private DataMemory dataMemory;
  private RegisterFile registerFile;
  private int programCounter;
  private boolean isBranchOrJump;
  private boolean isFinished;

  public Simulator(String assemblyCode) {
    Assembler assembler = new Assembler();
    List<String> binaryInstructions = assembler.assemble(assemblyCode);

    this.instructionMemory = new InstructionMemory(binaryInstructions);
    this.dataMemory = new DataMemory();
    this.registerFile = new RegisterFile();
    this.programCounter = 0x00400000;
    registerFile.write(29, 0x0FFFFFFF); // stack pointer default value
    isFinished = false;
  }

  public void executeNextStep() {
    if(programCounter >= 0x00400000 + instructionMemory.size() * 4){
      System.out.println("Program tamamlandı.");
      isFinished = true;
      return;
    }

    isBranchOrJump = false; // Her adımda resetlenir
    String instruction = instructionMemory.getInstruction(programCounter);
    decodeAndExecute(instruction);

    // Eğer PC manuel güncellenmediyse, PC'yi +4 arttır
    if(!isBranchOrJump){
      programCounter += 4;
    }
  }

  private void decodeAndExecute(String instruction) {
    String opcode = instruction.substring(0, 6); // İlk 6 bit opcode

    switch(opcode){
      case "000000": // R-format talimatlar
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
        System.out.println("Desteklenmeyen opcode: " + opcode);
    }
  }

  private void executeRFormat(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2); // Kaynak register
    int rt = Integer.parseInt(instruction.substring(11, 16), 2); // Hedef register
    int rd = Integer.parseInt(instruction.substring(16, 21), 2); // Sonuç register
    int shamt = Integer.parseInt(instruction.substring(21, 26), 2); // Shift miktarı
    int funct = Integer.parseInt(instruction.substring(26, 32), 2); // Function kodu

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
        System.out.println("Desteklenmeyen R-format funct: " + funct);
    }
  }

  private void executeAddi(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int immediate = Integer.parseInt(instruction.substring(16), 2);

    registerFile.write(rt, registerFile.read(rs) + immediate);
  }

  private void executeSw(String instruction) {
    int baseRegister = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Offset'in işaretini kontrol ederek signed genişletme yap
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign extend
    }

    // Adres hesaplama: Stack pointer için offset çıkarılır
    int address = registerFile.read(baseRegister) - offset;

    // Veriyi belleğe kaydet
    dataMemory.store(address, registerFile.read(rt));
  }

  private void executeLw(String instruction) {
    int baseRegister = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // Offset'in işaretini kontrol ederek signed genişletme yap
    if((offset&0x8000) != 0){
      offset |= 0xFFFF0000; // Sign extend
    }

    // Adres hesaplama: Stack pointer için offset çıkarılır
    int address = registerFile.read(baseRegister) - offset;

    // Bellekten veriyi yükle
    registerFile.write(rt, dataMemory.load(address));
  }

  private void executeBeq(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // 16-bit signed offset'i doğru hesapla
    if((offset&0x8000) != 0){ // Eğer MSB 1 ise, negatif bir sayı
      offset |= 0xFFFF0000; // Sign-extend
    }

    if(registerFile.read(rs) == registerFile.read(rt)){
      programCounter = programCounter + 4 + (offset * 4); // PC + 4 + (offset << 2)
      isBranchOrJump = true; // PC manuel olarak güncellendi
    }
  }

  private void executeBne(String instruction) {
    int rs = Integer.parseInt(instruction.substring(6, 11), 2);
    int rt = Integer.parseInt(instruction.substring(11, 16), 2);
    int offset = Integer.parseInt(instruction.substring(16), 2);

    // 16-bit signed offset'i doğru hesapla
    if((offset&0x8000) != 0){ // Eğer MSB 1 ise, negatif bir sayı
      offset |= 0xFFFF0000; // Sign-extend
    }

    if(registerFile.read(rs) != registerFile.read(rt)){
      programCounter = programCounter + 4 + (offset * 4); // PC + 4 + (offset << 2)
      isBranchOrJump = true; // PC manuel olarak güncellendi
    }
  }

  private void executeJ(String instruction) {
    int targetAddress = Integer.parseInt(instruction.substring(6), 2);
    programCounter = (programCounter&0xF0000000)|targetAddress << 2;
    isBranchOrJump = true;
  }

  private void executeJal(String instruction) {
    int targetAddress = Integer.parseInt(instruction.substring(6), 2);
    registerFile.write(31, programCounter + 4); // Return address in $ra
    programCounter = (programCounter&0xF0000000)|targetAddress << 2;
    isBranchOrJump = true;
  }

  public String[][] getRegisterState() {
    return registerFile.getRegisterState();
  }

  public String getMemoryState() {
    return dataMemory.getMemoryState();
  }

  public String getInstructionMemoryState() {
    return instructionMemory.getInstructionMemoryState(programCounter);
  }

  public int getInstructionMemorySize() {
    return instructionMemory.size();
  }

  public String getInstruction(int address) {
    return instructionMemory.getInstruction(address);
  }

  public void reset() {
    this.programCounter = 0x00400000;
    this.dataMemory = new DataMemory();
    this.registerFile = new RegisterFile();
    registerFile.write(29, 0x0FFFFFFF); // stack pointer default value
    isFinished = false;
  }

  public boolean isFinished() {
    return isFinished;
  }

  public int getProgramCounter() {
    return programCounter;
  }
}
