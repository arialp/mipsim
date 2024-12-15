import java.util.*;

public class AssemblyToBinary {

  // Label ve adres haritası
  private static Map<String, Integer> labelMap = new HashMap<>();

  // Assembly kodlarını binary'e çevirme talimatları
  private static Map<String, String> instructionMap = new HashMap<>() {{
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
  private static Map<String, String> functMap = new HashMap<>() {{
    put("add", "100000");
    put("sub", "100010");
    put("and", "100100");
    put("or", "100101");
    put("xor", "100110");
    put("slt", "101010");
    put("sll", "000000");
    put("srl", "000010");
  }};

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    // Kullanıcıdan assembly kodlarını al
    System.out.println(
            "Assembly kodlarını girin (çıkmak için boş satır):");
    List<String> assemblyLines = new ArrayList<>();
    String line;
    while(!(line = scanner.nextLine()).isEmpty()){
      assemblyLines.add(line);
    }

    // İlk geçiş: Label adreslerini haritala
    parseLabels(assemblyLines);

    // İkinci geçiş: Kodları binary'e çevir
    List<String> binaryCode = convertToBinary(assemblyLines);

    // Sonuçları yazdır
    System.out.println("Binary Kod:");
    for(String binary : binaryCode){
      System.out.println(binary);
    }

    scanner.close();
  }

  // İlk geçiş: Label'ları adresle
  private static void parseLabels(List<String> assemblyLines) {
    int currentAddress = 0;
    for(String line : assemblyLines){
      line = line.trim();
      if(line.endsWith(":")){
        String label = line.substring(0, line.length() - 1)
                           .trim();  // Etiketi ayıklama
        labelMap.put(label,
                     currentAddress);  // Etiket adresini haritaya ekleme
      } else if(!line.isEmpty()){
        currentAddress++;  // Label değilse, satırdaki komutun adresini arttır
      }
    }
  }

  // İkinci geçiş: Assembly'yi binary'ye çevir
  public static List<String> convertToBinary(
          List<String> assemblyLines)
  {
    List<String> binaryCode = new ArrayList<>();
    int currentAddress = 0;

    for(String line : assemblyLines){
      line = line.trim();
      if(line.isEmpty() || line.endsWith(":")) continue;

      String[] parts = line.split("[ ,]+");
      String instruction = parts[0];
      String binaryInstruction = instructionMap.getOrDefault(
              instruction, "000000");

      try{
        switch(instruction){
          // R-format instructions
          case "add":
          case "sub":
          case "and":
          case "or":
          case "xor":
          case "slt":{
            String rd = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            String rt = registerToBinary(parts[3]);
            String funct = functMap.get(instruction);
            binaryCode.add(
                    binaryInstruction + rs + rt + rd + "00000" +
                    funct);
            break;
          }
          case "sll":
          case "srl":{
            String rd = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            int shiftAmount = Integer.parseInt(parts[3]);
            String sa = toBinary(shiftAmount, 5);
            String funct = functMap.get(instruction);
            binaryCode.add(
                    binaryInstruction + "00000" + rt + rd + sa +
                    funct);
            break;
          }

          // I-format instructions
          case "addi":{
            String rt = registerToBinary(parts[1]);
            String rs = registerToBinary(parts[2]);
            int immediate = Integer.parseInt(parts[3]);
            binaryCode.add(binaryInstruction + rs + rt +
                           toBinary(immediate, 16));
            break;
          }
          case "lw":
          case "sw":{
            String rt = registerToBinary(parts[1]);
            String offsetAndRs = parts[2];
            String[] offsetParts = offsetAndRs.split("[()]");
            int offset = Integer.parseInt(offsetParts[0]);
            String rs = registerToBinary(offsetParts[1]);
            binaryCode.add(binaryInstruction + rs + rt +
                           toBinary(offset, 16));
            break;
          }
          case "beq":
          case "bne":{
            String rs = registerToBinary(parts[1]);
            String rt = registerToBinary(parts[2]);
            String label = parts[3];
            Integer offset = labelMap.get(label);  // Etiket adresi
            if(offset == null){
              System.err.println("Etiket bulunamadı: " + label);
            } else {
              int branchOffset = offset - (currentAddress + 1);
              binaryCode.add(binaryInstruction + rs + rt +
                             toBinary(branchOffset, 16));
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
              binaryCode.add(binaryInstruction + toBinary(address,
                                                          26));  // Hedef adresi binary'ye çevir ve ekle
            }
            break;
          }

          case "jal":{
            String label = parts[1];
            Integer address = labelMap.get(label);  // Etiket adresi
            if(address == null){
              System.err.println("Etiket bulunamadı: " + label);
            } else {
              binaryCode.add(binaryInstruction + toBinary(address,
                                                          26));  // Hedef adresi binary'ye çevir ve ekle
            }
            break;
          }

          case "jr":{
            String rs = registerToBinary(
                    parts[1]);  // Register adresi
            binaryCode.add(
                    "000000" + rs + "00000" + "00000" + "00000" +
                    binaryInstruction);  // JR komutunun formatı
            break;
          }

          default:{
            System.err.println(
                    "Desteklenmeyen talimat: " + instruction);
          }
        }
      } catch(NumberFormatException|ArrayIndexOutOfBoundsException e){
        System.err.println(
                "Hata: Talimat işlenirken bir hata oluştu. Talimat: " +
                line);
      }

      currentAddress++;
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
