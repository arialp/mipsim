import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssemblySimulatorGUI {
  private JTextArea assemblyInput;
  private JTextArea binaryOutput;
  private JTextArea instructionMemoryOutput;
  private JTextArea registerOutput;
  private JTextArea memoryOutput;

  private Simulator simulator;

  public AssemblySimulatorGUI() {
    JFrame frame = new JFrame("Assembly Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 600);

    JPanel mainPanel = new JPanel(new BorderLayout());

    // Assembly kod girişi
    assemblyInput = new JTextArea(10, 50);
    JScrollPane assemblyScroll = new JScrollPane(assemblyInput);
    mainPanel.add(assemblyScroll, BorderLayout.NORTH);

    // Çıktı alanları
    JPanel outputPanel = new JPanel(new GridLayout(1, 4));
    binaryOutput = new JTextArea();
    binaryOutput.setEditable(false);
    outputPanel.add(new JScrollPane(binaryOutput));

    instructionMemoryOutput = new JTextArea();
    instructionMemoryOutput.setEditable(false);
    outputPanel.add(new JScrollPane(instructionMemoryOutput));

    registerOutput = new JTextArea();
    registerOutput.setEditable(false);
    outputPanel.add(new JScrollPane(registerOutput));

    memoryOutput = new JTextArea();
    memoryOutput.setEditable(false);
    outputPanel.add(new JScrollPane(memoryOutput));
    mainPanel.add(outputPanel, BorderLayout.CENTER);

    // Kontrol butonları
    JPanel buttonPanel = new JPanel();
    JButton assembleButton = new JButton("Assemble");
    JButton stepButton = new JButton("Next Step");
    JButton resetButton = new JButton("Reset");

    assembleButton.addActionListener(new AssembleListener());
    stepButton.addActionListener(new StepListener());
    resetButton.addActionListener(new ResetListener());

    buttonPanel.add(assembleButton);
    buttonPanel.add(stepButton);
    buttonPanel.add(resetButton);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    frame.add(mainPanel);
    frame.setVisible(true);
  }

  private class AssembleListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String assemblyCode = assemblyInput.getText();
      simulator = new Simulator(assemblyCode);

      // Binary kodu ve instruction memory'yi göster
      StringBuilder binaryOutputBuilder = new StringBuilder();
      StringBuilder instructionMemoryBuilder = new StringBuilder();

      for(int i = 0; i < simulator.instructionMemory.size(); i++){
        String instruction = simulator.instructionMemory.getInstruction(
                i);
        binaryOutputBuilder.append(instruction).append("\n");
        instructionMemoryBuilder.append(i).append(": ")
                                .append(instruction).append("\n");
      }

      binaryOutput.setText(binaryOutputBuilder.toString());
      instructionMemoryOutput.setText(
              instructionMemoryBuilder.toString());
      registerOutput.setText(simulator.getRegisterState());
      memoryOutput.setText(simulator.getMemoryState());
    }
  }

  private class StepListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(simulator != null){
        simulator.executeNextStep();

        // Instruction Memory'deki çalıştırılan talimatı vurgula
        highlightCurrentInstruction(simulator.getProgramCounter());

        registerOutput.setText(simulator.getRegisterState());
        memoryOutput.setText(simulator.getMemoryState());
      }
    }
  }

  private class ResetListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(simulator != null){
        simulator.reset();
        binaryOutput.setText("");
        instructionMemoryOutput.setText("");
        registerOutput.setText("");
        memoryOutput.setText("");
      }
    }
  }

  private void highlightCurrentInstruction(int programCounter) {
    try{
      instructionMemoryOutput.getHighlighter().removeAllHighlights();
      int start = instructionMemoryOutput.getLineStartOffset(
              programCounter);
      int end = instructionMemoryOutput.getLineEndOffset(
              programCounter);
      instructionMemoryOutput.getHighlighter()
                             .addHighlight(start, end,
                                           new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(
                                                   Color.YELLOW));
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new AssemblySimulatorGUI();
  }
}
