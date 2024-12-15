import java.util.HashMap;
import java.util.Map;

public class MIPSAssembler {

  // OpCode and Function Code mapping
  private static final Map<String, String> opcodeMap = new HashMap<>();
  private static final Map<String, String> funcMap = new HashMap<>();

  static {
    // R-type functions
    funcMap.put("srl", "000010");
    funcMap.put("slt", "101010");
    funcMap.put("jr", "001000");

    // I-type opcodes
    opcodeMap.put("addi", "001000");
    opcodeMap.put("beq", "000100");
    opcodeMap.put("bne", "000101");
    opcodeMap.put("jal", "000011");
  }

  // Register mapping
  private static final Map<String, String> registerMap = new HashMap<>();

  static {
    registerMap.put("$zero", "00000");
    registerMap.put("$t0", "01000");
    registerMap.put("$t1", "01001");
    registerMap.put("$t2", "01010");
    registerMap.put("$s0", "10000");
    registerMap.put("$ra", "11111");
  }

  public static String assemble(String mipsCode) {
    StringBuilder machineCode = new StringBuilder();
    String[] lines = mipsCode.split("\n");
    Map<String, Integer> labels = new HashMap<>();

    // First pass: Collect labels
    int pc = 0;
    for(String line : lines){
      line = line.trim();
      if(line.isEmpty()){
        continue;
      }
      if(line.endsWith(":")){
        String label = line.substring(0, line.length() - 1);
        labels.put(label, pc);
      } else {
        pc++;
      }
    }

    // Second pass: Translate instructions
    pc = 0;
    for(String line : lines){
      line = line.trim();
      if(line.isEmpty() || line.endsWith(":")){
        continue;
      }

      String[] parts = line.split("[ ,]+");
      String instruction = parts[0];

      if(opcodeMap.containsKey(instruction)){
        String opcode = opcodeMap.get(instruction);
        if(instruction.equals("jal")){
          int address = labels.getOrDefault(parts[1], 0);
          machineCode.append(opcode)
                     .append(String.format("%026d", address))
                     .append("\n");
        } else if(instruction.equals("beq") ||
                  instruction.equals("bne")){
          String rs = registerMap.get(parts[1]);
          String rt = registerMap.get(parts[2]);
          int target = labels.getOrDefault(parts[3], 0);
          int offset = target - (pc + 1);
          String immediate = String.format("%016d", offset&0xFFFF);
          machineCode.append(opcode).append(rs).append(rt)
                     .append(immediate).append("\n");
        } else {
          String rt = registerMap.get(parts[1]);
          String rs = registerMap.get(parts[2]);
          String immediate = String.format("%016d", Integer.parseInt(
                  parts[3]));
          machineCode.append(opcode).append(rs).append(rt)
                     .append(immediate).append("\n");
        }
      } else if(funcMap.containsKey(instruction)){
        String func = funcMap.get(instruction);
        String rd = registerMap.get(parts[1]);
        String rs = registerMap.get(parts[2]);
        String rt = (parts.length > 3 &&
                     registerMap.containsKey(parts[3])) ?
                    registerMap.get(parts[3]) :
                    "00000";
        String shamt = "00000";
        machineCode.append("000000").append(rs).append(rt).append(rd)
                   .append(shamt).append(func).append("\n");
      } else {
        System.err.println("Unknown instruction: " + instruction);
      }

      pc++;
    }

    return machineCode.toString();
  }

  public static void main(String[] args) {
    String mipsCode = """
                          addi $t0, $zero, 10
                          addi $t1, $zero, 4
                          addi $t2, $zero, 1
                          test:
                          bne $s0, $zero, test
                          srl $t0, $t0, $t2
                          slt $s0, $t0, $t1
                          beq $s0, $zero, test
                          jr $ra
                      """;

    String machineCode = assemble(mipsCode);
    System.out.println(machineCode);
  }
}