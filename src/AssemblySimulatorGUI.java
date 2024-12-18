/**
 * This class represents a GUI for simulating a MIPS assembly program. It provides components for
 * inputting assembly code, displaying machine code, and monitoring instruction memory, data memory,
 * and register state.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssemblySimulatorGUI {
  private static final Font TEXT_FONT = new Font("Cascadia Mono", Font.PLAIN, 16);
  private final JTextArea assemblyInput;
  private final JTextArea machineCodeOutput;
  private final JTextArea registerFileLeft;
  private final JTextArea registerFileRight;
  private final JTextArea instructionMemoryOutput;
  private final JTextArea dataMemoryOutput;
  private final JButton hexButton;
  private final JButton binaryButton;
  private final JButton assembleButton;
  private final JButton stepButton;
  private final JButton runButton;
  private final JButton resetButton;
  private Simulator simulator;
  private boolean displayInHex = false;

  /**
   * The main entry point for launching the GUI.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(AssemblySimulatorGUI :: new);
  }

  /**
   * Constructs the AssemblySimulatorGUI with all its components.
   * Initializes the layout, text areas, buttons, and event listeners.
   */
  public AssemblySimulatorGUI() {
    JFrame frame = new JFrame("MIPS Assembly Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1280, 720);
    frame.getContentPane().setBackground(Color.BLACK);

    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBackground(Color.BLACK);

    GridBagConstraints topPanelConstraints = new GridBagConstraints();
    topPanelConstraints.gridx = 0;
    topPanelConstraints.gridy = 0;
    topPanelConstraints.weightx = 1;
    topPanelConstraints.weighty = 0.5;
    topPanelConstraints.fill = GridBagConstraints.BOTH;

    GridBagConstraints memoryPanelConstraints = new GridBagConstraints();
    memoryPanelConstraints.gridx = 0;
    memoryPanelConstraints.gridy = 1;
    memoryPanelConstraints.weightx = 1;
    memoryPanelConstraints.weighty = 0.48;
    memoryPanelConstraints.fill = GridBagConstraints.BOTH;

    GridBagConstraints bottomPanelConstraints = new GridBagConstraints();
    bottomPanelConstraints.gridx = 0;
    bottomPanelConstraints.gridy = 2;
    bottomPanelConstraints.weightx = 1;
    bottomPanelConstraints.weighty = 0.02;
    bottomPanelConstraints.fill = GridBagConstraints.BOTH;

    JPanel topPanel = new JPanel(new GridLayout(1, 3));
    topPanel.setBackground(Color.BLACK);

    assemblyInput = createTextArea("ASSEMBLY INSTRUCTIONS");
    topPanel.add(new JScrollPane(assemblyInput));

    JPanel machineCodePanel = new JPanel(new BorderLayout());
    machineCodePanel.setBackground(Color.BLACK);
    machineCodePanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                                             "MACHINE CODE", 2, 2, TEXT_FONT, Color.WHITE));

    machineCodeOutput = createTextArea();
    machineCodeOutput.setEditable(false);
    machineCodePanel.add(new JScrollPane(machineCodeOutput), BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.setBackground(Color.BLACK);
    hexButton = createButton("HEX");
    binaryButton = createButton("BINARY");
    buttonPanel.add(hexButton);
    buttonPanel.add(binaryButton);

    machineCodePanel.add(buttonPanel, BorderLayout.SOUTH);
    topPanel.add(machineCodePanel);

    JPanel registerFilePanel = new JPanel(new GridLayout(1, 2));
    registerFilePanel.setBackground(Color.BLACK);
    registerFilePanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                                             "Register File", 2, 2, TEXT_FONT, Color.WHITE));

    registerFileLeft = createTextArea();
    registerFileLeft.setEditable(false);
    registerFileRight = createTextArea();
    registerFileRight.setEditable(false);

    registerFilePanel.add(new JScrollPane(registerFileLeft));
    registerFilePanel.add(new JScrollPane(registerFileRight));

    topPanel.add(registerFilePanel);
    mainPanel.add(topPanel, topPanelConstraints);

    JPanel memoryPanel = new JPanel(new GridLayout(1, 2));
    memoryPanel.setBackground(Color.BLACK);

    instructionMemoryOutput = createTextArea("INSTRUCTION MEMORY");
    instructionMemoryOutput.setEditable(false);
    memoryPanel.add(new JScrollPane(instructionMemoryOutput));

    dataMemoryOutput = createTextArea("DATA MEMORY");
    dataMemoryOutput.setEditable(false);
    memoryPanel.add(new JScrollPane(dataMemoryOutput));

    mainPanel.add(memoryPanel, memoryPanelConstraints);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottomPanel.setBackground(Color.BLACK);

    assembleButton = createButton("Assemble");
    runButton = createButton("Run");
    stepButton = createButton("Next Step");
    resetButton = createButton("Reset");

    bottomPanel.add(assembleButton);
    bottomPanel.add(runButton);
    bottomPanel.add(stepButton);
    bottomPanel.add(resetButton);
    mainPanel.add(bottomPanel, bottomPanelConstraints);

    frame.add(mainPanel);
    frame.setVisible(true);

    hexButton.addActionListener(e->updateMachineCode(true));
    binaryButton.addActionListener(e->updateMachineCode(false));
    assembleButton.addActionListener(new AssembleListener());
    runButton.addActionListener(new RunListener());
    stepButton.addActionListener(new StepListener());
    resetButton.addActionListener(new ResetListener());
  }

  /**
   * Creates a styled JTextArea with the given title.
   *
   * @param title The title to display above the JTextArea.
   *
   * @return A styled JTextArea.
   */
  private JTextArea createTextArea(String title) {
    JTextArea textArea = new JTextArea();
    textArea.setFont(TEXT_FONT);
    textArea.setBackground(Color.DARK_GRAY);
    textArea.setForeground(Color.WHITE);
    textArea.setCaretColor(Color.WHITE);
    textArea.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title, 2,
                                             2, TEXT_FONT, Color.WHITE));
    return textArea;
  }

  /**
   * Creates a styled JTextArea without a title.
   *
   * @return A styled JTextArea.
   */
  private JTextArea createTextArea() {
    return createTextArea("");
  }

  /**
   * Creates a styled JButton with the given text.
   *
   * @param text The text to display on the button.
   *
   * @return A styled JButton.
   */
  private JButton createButton(String text) {
    JButton button = new JButton(text);
    button.setFont(TEXT_FONT);
    button.setForeground(Color.WHITE);
    button.setBackground(Color.DARK_GRAY);
    button.setFocusPainted(false);
    return button;
  }

  /**
   * Updates the machine code display area with the current machine code.
   *
   * @param toHex If true, displays the machine code in hexadecimal; otherwise, binary.
   */
  private void updateMachineCode(boolean toHex) {
    displayInHex = toHex;
    if(simulator != null){
      StringBuilder output = new StringBuilder();
      for(int i = 0; i < simulator.getInstructionMemorySize() * 4; i += 4){
        String instruction = simulator.getInstruction(i + 0x00400000);
        output.append(toHex ? toHexadecimal(instruction) : instruction).append("\n");
      }
      machineCodeOutput.setText(String.valueOf(output));
      machineCodeOutput.setCaretPosition(0);
    }
  }

  /**
   * Converts a binary string to its hexadecimal representation.
   *
   * @param binary The binary string to convert.
   *
   * @return The hexadecimal representation.
   */
  private String toHexadecimal(String binary) {
    return String.format("0x%08X", Long.parseLong(binary, 2));
  }

  /**
   * Updates the instruction memory display area with the current instruction memory state.
   */
  private void updateInstructionMemory() {
    if(simulator != null){
      instructionMemoryOutput.setText(simulator.getInstructionMemoryState());
      instructionMemoryOutput.setCaretPosition(0);
    }
  }

  /**
   * Updates the data memory display area with the current data memory state.
   */
  private void updateDataMemory() {
    if(simulator != null){
      dataMemoryOutput.setText(simulator.getMemoryState());
      dataMemoryOutput.setCaretPosition(0);
    }
  }

  /**
   * Updates the register file display areas with the current register states.
   */
  private void updateRegisterFile() {
    if(simulator != null){
      String[][] registerState = simulator.getRegisterState();
      StringBuilder leftOutput = new StringBuilder();
      StringBuilder rightOutput = new StringBuilder();

      for(int i = 0; i < 16; i++){
        leftOutput.append(registerState[i][0]).append("\t").append(registerState[i][1])
                  .append("\n");
      }
      for(int i = 16; i < 32; i++){
        rightOutput.append(registerState[i][0]).append("\t").append(registerState[i][1])
                   .append("\n");
      }

      registerFileLeft.setText(leftOutput.toString());
      registerFileLeft.setCaretPosition(0);
      registerFileRight.setText(rightOutput.toString());
      registerFileRight.setCaretPosition(0);
    }
  }

  /**
   * Event listener for the Assemble button. Initializes the simulator and updates all displays.
   */
  private class AssembleListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String assemblyCode = assemblyInput.getText();
      simulator = new Simulator(assemblyCode);

      updateMachineCode(displayInHex);
      updateInstructionMemory();
      updateRegisterFile();
      updateDataMemory();
    }
  }

  /**
   * Event listener for the Step button. Executes one instruction and updates all displays.
   */
  private class StepListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(simulator != null){
        simulator.executeNextStep();
        updateInstructionMemory();
        updateRegisterFile();
        updateDataMemory();
      }
    }
  }

  /**
   * Event listener for the Run button. Executes the program until completion and updates displays.
   */
  private class RunListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(simulator != null){
        while(!simulator.isFinished()){
          simulator.executeNextStep();
          updateInstructionMemory();
          updateRegisterFile();
          updateDataMemory();
        }
      }
    }
  }

  /**
   * Event listener for the Reset button. Resets the simulator and clears all displays.
   */
  private class ResetListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(simulator != null){
        simulator.reset();
        updateMachineCode(displayInHex);
        updateInstructionMemory();
        updateRegisterFile();
        updateDataMemory();
      }
    }
  }
}
