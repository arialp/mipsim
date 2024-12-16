import java.util.*;

public class Assembler {

  // Label ve adres haritası
  private static final Map<String, Integer> labelMap = new HashMap<>();

  // Assembly kodlarını binary'e çevirme talimatları
  private static final Map<String, String> instructionMap = new HashMap<>() {{
    // R-Format instructions
    put("add", "000000");
    put("sub", "000000");
    put("and", "000000");
    put("or", "000000");
    put("slt", "000000");
    put("sll", "000000");
    put("srl", "000000");

    // I-Format instructions
    put("addi", "001000");
    put("lw", "100011");
    put("sw", "101011");
    put("beq", "000100");
    put("bne", "000101");

    // J-Format instructions
    put("j", "000010");
    put("jal", "000011");
    put("jr", "000000"); // special case, handled separately
  }};

  // Function codes for R-type instructions
  private static final Map<String, String> functMap = new HashMap<>() {{
    put("add", "100000");
    put("sub", "100010");
    put("and", "100100");
    put("or", "100101");
    put("slt", "101010");
    put("sll", "000000");
    put("srl", "000010");
    put("jr", "001000");
  }};

  public List<String> assemble(String assemblyCode) {
    List<String> binaryInstructions;
    String[] lines = assemblyCode.split("\n");
    List<String> assemblyLines = new ArrayList<>();

    // Yorumları kaldır ve temiz assembly satırlarını listeye ekle
    for(String line : lines){
      line = line.split("#")[0].trim(); // Yorumları çıkar ve temizle
      if(!line.isEmpty()){
        assemblyLines.add(line);
      }
    }

    parseLabels(assemblyLines);
    binaryInstructions = convertToBinary(assemblyLines);

    return binaryInstructions;
  }

  private static void parseLabels(List<String> assemblyLines) {
    int currentAddress = 0;
    for(String line : assemblyLines){
      line = line.trim();
      if(line.endsWith(":")){
        String label = line.substring(0, line.length() - 1).trim();  // Etiketi ayıklama
        labelMap.put(label, currentAddress * 4 + 0x00400000);  // Etiket adresini haritaya ekleme
      } else if(!line.isEmpty()){
        currentAddress++;  // Label değilse, satırdaki komutun adresini arttır
      }
    }
  }

  // İkinci geçiş: Assembly'yi binary'ye çevir
  public static List<String> convertToBinary(List<String> assemblyLines)
  {
    List<String> binaryCode = new ArrayList<>();
    int currentLine = 0;

    for(String line : assemblyLines){
      line = line.trim();
      if(line.isEmpty() || line.endsWith(":")) continue;

      String[] parts = line.split("[ ,]+");
      String instruction = parts[0];
      String binaryInstruction = instructionMap.getOrDefault(instruction, "000000");

      try{
        switch(instruction){
          // R-format instructions
          case "add":
          case "sub":
          case "and":
          case "or":
          case "slt":{
            String rd = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            String rt = registerToBinary(parts[3]);
            String funct = functMap.get(instruction);
            binaryCode.add(binaryInstruction + rs + rt + rd + "00000" + funct);
            break;
          }
          case "sll":
          case "srl":{
            String rd = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            int shiftAmount = Integer.parseInt(parts[3]);
            String sa = toBinary(shiftAmount, 5);
            String funct = functMap.get(instruction);
            binaryCode.add(binaryInstruction + "00000" + rt + rd + sa + funct);
            break;
          }

          // I-format instructions
          case "addi":{
            String rt = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            int immediate = Integer.parseInt(parts[3]);
            binaryCode.add(binaryInstruction + rs + rt + toBinary(immediate, 16));
            break;
          }
          case "lw":
          case "sw":{
            String rt = registerToBinary(parts[1]);
            String offsetAndRs = parts[2];
            String[] offsetParts = offsetAndRs.split("[()]");
            int offset = Integer.parseInt(offsetParts[0]);
            String rs = registerToBinary(offsetParts[1]);
            binaryCode.add(binaryInstruction + rs + rt + toBinary(offset, 16));
            break;
          }
          case "beq":
          case "bne":{
            String rs = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            String label = parts[3];
            Integer targetAddress = labelMap.get(label); // Etiket adresi

            if(targetAddress == null){
              System.err.println("Etiket bulunamadı: " + label);
            } else {
              // Hedef adres ile mevcut talimat adresi arasındaki fark
              int currentPC = currentLine * 4 + 0x00400000;
              int relativeOffset = (targetAddress - (currentPC + 4)) / 4;

              // Relative offset 16-bit signed olacak
              binaryCode.add(binaryInstruction + rs + rt + toBinary(relativeOffset, 16));
            }
            break;
          }

          // J-format instructions
          case "j":{
            String label = parts[1];
            Integer address = labelMap.get(label);  // Etiket adresi
            if(address == null){
              System.err.println("Etiket bulunamadı: " + label);
            } else {
              int targetAddress = address / 4;
              binaryCode.add(binaryInstruction + toBinary(targetAddress, 26));
            }
            break;
          }

          case "jal":{
            String label = parts[1];
            Integer address = labelMap.get(label);  // Etiket adresi
            if(address == null){
              System.err.println("Etiket bulunamadı: " + label);
            } else {
              int targetAddress = address / 4;
              binaryCode.add(binaryInstruction + toBinary(targetAddress, 26));
            }
            break;
          }

          case "jr":{
            String rs = registerToBinary(parts[1]);  // Register adresi
            String funct = functMap.get(instruction);
            binaryCode.add("000000" + rs + "00000" + "00000" + "00000" + funct);
            break;
          }

          default:{
            System.err.println("Desteklenmeyen talimat: " + instruction);
          }
        }
      } catch(NumberFormatException|ArrayIndexOutOfBoundsException e){
        System.err.println("Hata: Talimat işlenirken bir hata oluştu. Talimat: " + line);
      }

      currentLine++;
    }
    return binaryCode;
  }

  // Register'ı binary'ye çevir
  private static String registerToBinary(String register) {
    Map<String, String> registerMap = new HashMap<>() {{
      put("$zero", "00000");
      put("$at", "00001");
      put("$v0", "00010");
      put("$v1", "00011");
      put("$a0", "00100");
      put("$a1", "00101");
      put("$a2", "00110");
      put("$a3", "00111");
      put("$t0", "01000");
      put("$t1", "01001");
      put("$t2", "01010");
      put("$t3", "01011");
      put("$t4", "01100");
      put("$t5", "01101");
      put("$t6", "01110");
      put("$t7", "01111");
      put("$s0", "10000");
      put("$s1", "10001");
      put("$s2", "10010");
      put("$s3", "10011");
      put("$s4", "10100");
      put("$s5", "10101");
      put("$s6", "10110");
      put("$s7", "10111");
      put("$t8", "11000");
      put("$t9", "11001");
      put("$k0", "11010");
      put("$k1", "11011");
      put("$gp", "11100");
      put("$sp", "11101");
      put("$fp", "11110");
      put("$ra", "11111");
    }};
    return registerMap.getOrDefault(register, "00000");
  }

  // Değeri binary stringe çevir (işaretli/signed)
  private static String toBinary(int value, int bits) {
    String binary = Integer.toBinaryString(value&((1 << bits) - 1));
    while(binary.length() < bits){
      binary = "0" + binary;
    }
    return binary;
  }
}
