import java.util.List;

public class Simulator {
  InstructionMemory instructionMemory;
  private DataMemory dataMemory;
  private RegisterFile registerFile;
  private int programCounter;

  public Simulator(String assemblyCode) {
    Assembler assembler = new Assembler();
    List<String> binaryInstructions = assembler.assemble(
            assemblyCode);

    this.instructionMemory = new InstructionMemory(
            binaryInstructions);
    this.dataMemory = new DataMemory(1024);
    this.registerFile = new RegisterFile();
    this.programCounter = 0;
  }

  public void executeNextStep() {
    if(programCounter >= instructionMemory.size()){
      System.out.println("Program tamamlandı.");
      return;
    }

    String instruction = instructionMemory.getInstruction(
            programCounter);
    decodeAndExecute(instruction);
    programCounter++;
  }

  private void decodeAndExecute(String instruction) {
    // Talimat çözme ve yürütme işlemleri
  }

  public String getRegisterState() {
    return registerFile.getRegisterState();
  }

  public String getMemoryState() {
    return dataMemory.getMemoryState();
  }

  public void reset() {
    this.programCounter = 0;
    this.dataMemory = new DataMemory(1024);
    this.registerFile = new RegisterFile();
  }

  // Yeni eklenen getProgramCounter() metodu
  public int getProgramCounter() {
    return programCounter;
  }
}
