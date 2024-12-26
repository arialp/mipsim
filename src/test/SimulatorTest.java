package test;

import org.junit.jupiter.api.Test;
import sim.Simulator;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {

  @Test
  void testBasicInstructions() {
    String testCode = """
                      addi $t1, $zero, 20
                      addi $t0, $zero, 5
                      test1:
                      add $t0, $t0, $t0
                      bne $t0, $t1, test1
                      add $s0, $t0, $zero
                      """;
    Simulator simulator = new Simulator(testCode);

    while(!simulator.isFinished()){
      simulator.step();
    }

    String[][] registers = simulator.getRegisterState();
    assertEquals(20, Integer.parseInt(registers[9][1])); // $t1
    assertEquals(20, Integer.parseInt(registers[8][1])); // $t0
  }

  @Test
  void testMemoryOperations() {
    String testCode = """
                      addi $t0, $zero, 170
                      addi $t1, $zero, 204
                      and $t2, $t0, $t1
                      sw $t2, 0($sp)
                      lw $t3, 0($sp)
                      """;
    Simulator simulator = new Simulator(testCode);

    while(!simulator.isFinished()){
      simulator.step();
    }

    String[][] memory = simulator.getDataMemoryState();
    assertEquals("136", memory[0][1]); // Memory location 0($sp)
  }

  @Test
  void testControlFlow() {
    String testCode = """
                      addi $t1, $zero, -32768
                      addi $s0, $zero, 1
                      loop:
                      sub $t0, $t0, $s0
                      bne $t0, $t1, loop
                      """;
    Simulator simulator = new Simulator(testCode);

    while(!simulator.isFinished()){
      simulator.step();
    }

    String[][] registers = simulator.getRegisterState();
    assertEquals(-32768, Integer.parseInt(registers[8][1])); // $t0
  }
}

