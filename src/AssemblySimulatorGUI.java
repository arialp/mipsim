/**
 * This class represents a GUI for simulating a MIPS assembly program. It provides components for
 * inputting assembly code, displaying machine code, and monitoring instruction memory, data memory,
 * and register state.
 */
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AssemblySimulatorGUI {
  private static final Font TEXT_FONT = new Font("Cascadia Mono", Font.PLAIN, 16);
  private final JTextArea assemblyInput, machineCodeOutput, registerFileLeft, registerFileRight,
          instructionMemoryOutput, dataMemoryOutput;
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
   * Constructs the AssemblySimulatorGUI with all its components. Initializes the layout, text
   * areas, buttons, and event listeners.
   */
  public AssemblySimulatorGUI() {
    JFrame frame = new JFrame("MIPS Assembly Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1600, 900);
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
    memoryPanelConstraints.weighty = 0.5;
    memoryPanelConstraints.fill = GridBagConstraints.BOTH;

    GridBagConstraints bottomPanelConstraints = new GridBagConstraints();
    bottomPanelConstraints.gridx = 0;
    bottomPanelConstraints.gridy = 2;
    bottomPanelConstraints.weightx = 1;
    bottomPanelConstraints.weighty = 0.02;
    bottomPanelConstraints.fill = GridBagConstraints.BOTH;

    JPanel topPanel = new JPanel(new GridLayout(1, 3));
    topPanel.setBackground(Color.BLACK);

    // Assembly input area
    assemblyInput = createTextArea("ASSEMBLY INSTRUCTIONS");
    topPanel.add(createStyledScrollPane(assemblyInput));

    // Machine code output area
    JPanel machineCodePanel = new JPanel(new BorderLayout());
    machineCodePanel.setBackground(Color.BLACK);
    machineCodePanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                                             "MACHINE CODE", 2, 2, TEXT_FONT, Color.WHITE));

    machineCodeOutput = createTextArea();
    machineCodeOutput.setEditable(false);
    machineCodePanel.add(createStyledScrollPane(machineCodeOutput), BorderLayout.CENTER);

    // Machine code display options
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.setBackground(Color.BLACK);
    JButton hexButton = createButton("HEX");
    JButton binaryButton = createButton("BINARY");
    buttonPanel.add(hexButton);
    buttonPanel.add(binaryButton);

    machineCodePanel.add(buttonPanel, BorderLayout.SOUTH);
    topPanel.add(machineCodePanel);

    // Register file display area
    JPanel registerFilePanel = new JPanel(new GridLayout(1, 2));
    registerFilePanel.setBackground(Color.BLACK);
    registerFilePanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                                             "REGISTER FILE", 2, 2, TEXT_FONT, Color.WHITE));

    registerFileLeft = createTextArea();
    registerFileLeft.setEditable(false);
    registerFileRight = createTextArea();
    registerFileRight.setEditable(false);

    registerFilePanel.add(createStyledScrollPane(registerFileLeft));
    registerFilePanel.add(createStyledScrollPane(registerFileRight));

    topPanel.add(registerFilePanel);
    mainPanel.add(topPanel, topPanelConstraints);

    // Memory display area
    JPanel memoryPanel = new JPanel(new GridLayout(1, 2));
    memoryPanel.setBackground(Color.BLACK);

    instructionMemoryOutput = createTextArea("INSTRUCTION MEMORY");
    instructionMemoryOutput.setEditable(false);
    memoryPanel.add(createStyledScrollPane(instructionMemoryOutput));

    dataMemoryOutput = createTextArea("DATA MEMORY");
    dataMemoryOutput.setEditable(false);
    memoryPanel.add(createStyledScrollPane(dataMemoryOutput));

    mainPanel.add(memoryPanel, memoryPanelConstraints);

    // Bottom panel with control buttons
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottomPanel.setBackground(Color.BLACK);

    JButton loadFileButton = createButton("Load File");
    JButton assembleButton = createButton("Assemble");
    JButton runButton = createButton("Run");
    JButton stepButton = createButton("Next Step");
    JButton resetButton = createButton("Reset");

    bottomPanel.add(loadFileButton);
    bottomPanel.add(assembleButton);
    bottomPanel.add(runButton);
    bottomPanel.add(stepButton);
    bottomPanel.add(resetButton);

    mainPanel.add(bottomPanel, bottomPanelConstraints);
    frame.add(mainPanel);
    frame.setVisible(true);

    hexButton.addActionListener(_->updateMachineCode(true));
    binaryButton.addActionListener(_->updateMachineCode(false));
    loadFileButton.addActionListener(new LoadListener());
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
   * Creates a JScrollPane with a custom-styled vertical and horizontal scrollbar. The scrollbars
   * are modernized with a minimalist design and custom colors.
   *
   * @param textArea The JTextArea to be added to the scroll pane.
   *
   * @return A JScrollPane with styled scrollbars.
   */
  private JScrollPane createStyledScrollPane(JTextArea textArea) {
    JScrollPane scrollPane = new JScrollPane(textArea);

    // Set the background color for scrollbars
    scrollPane.getVerticalScrollBar().setBackground(Color.DARK_GRAY);
    scrollPane.getHorizontalScrollBar().setBackground(Color.DARK_GRAY);

    // Apply custom UI to the scrollbars
    scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
    scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

    return scrollPane;
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
      StringBuilder instructionMemoryState = new StringBuilder();
      int programCounter = simulator.getProgramCounter();
      String[][] instructionMemory = simulator.getInstructionMemoryState();

      instructionMemoryState.append(
              String.format("Address     Byte 1   Byte 2   Byte 3   Byte 4   PC = 0x%08X\n",
                            programCounter));

      for(String[] addresses : instructionMemory){
        // Split 32-bit instruction into 8-bit segments
        String[]
                instructionParts =
                {addresses[1].substring(0, 8), addresses[1].substring(8, 16),
                 addresses[1].substring(16, 24), addresses[1].substring(24, 32)};

        // Append address and instruction parts
        String address = addresses[0];
        instructionMemoryState.append(
                String.format("%s: %s %s %s %s", address, instructionParts[0], instructionParts[1],
                              instructionParts[2], instructionParts[3]));
        if(Long.parseLong(address.substring(2), 16) == programCounter){
          instructionMemoryState.append(" <- PC");
        }
        instructionMemoryState.append("\n");
      }

      instructionMemoryOutput.setText(instructionMemoryState.toString());
      instructionMemoryOutput.setCaretPosition(0);
    }
  }

  /**
   * Updates the data memory display area with the current data memory state.
   */
  private void updateDataMemory() {
    if(simulator != null){
      StringBuilder dataMemoryState = new StringBuilder();
      String[][] dataMemory = simulator.getDataMemoryState();

      // Add header
      dataMemoryState.append("Address     Byte 1   Byte 2   Byte 3   Byte 4   Decimal Value\n");

      // Format data memory content
      for(String[] entry : dataMemory){
        String address = entry[0];
        int data = Integer.parseInt(entry[1]);

        // Split 32-bit data into 8-bit parts
        String[] dataBytes = new String[4];
        for(int j = 0; j < 4; j++){
          dataBytes[j] =
                  String.format("%8s", Integer.toBinaryString((data >> (24 - j * 8))&0xFF))
                        .replace(' ', '0');
        }

        // Append formatted address, data bytes, and decimal value
        dataMemoryState.append(
                String.format("%s: %s %s %s %s %d\n", address, dataBytes[0], dataBytes[1],
                              dataBytes[2], dataBytes[3], data));
      }

      // Update GUI
      dataMemoryOutput.setText(dataMemoryState.toString());
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
        leftOutput.append(registerState[i][0]).append(":\t").append(registerState[i][1])
                  .append("\n");
      }
      for(int i = 16; i < 32; i++){
        rightOutput.append(registerState[i][0]).append(":\t").append(registerState[i][1])
                   .append("\n");
      }

      registerFileLeft.setText(leftOutput.toString());
      registerFileLeft.setCaretPosition(0);
      registerFileRight.setText(rightOutput.toString());
      registerFileRight.setCaretPosition(0);
    }
  }

  /**
   * A custom implementation of BasicScrollBarUI to provide a modern and minimalist look for
   * scrollbars. This class customizes the thumb, track, and buttons of the scrollbar.
   */
  private static class ModernScrollBarUI extends BasicScrollBarUI {
    private static final int THUMB_SIZE = 8;

    /**
     * Configures the colors of the scrollbar's thumb and track. The thumb is styled with a
     * translucent color, while the track is fully transparent.
     */
    @Override
    protected void configureScrollBarColors() {
      this.thumbColor = new Color(200, 200, 200, 200); // Light gray with transparency
      this.trackColor = new Color(50, 50, 50, 0); // Fully transparent
    }

    /**
     * Sets the preferred size of the scrollbar to a fixed width/height. This defines the thickness
     * of the scrollbar.
     *
     * @param c The scrollbar component.
     *
     * @return A Dimension object representing the scrollbar's width and height.
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
      return new Dimension(THUMB_SIZE, THUMB_SIZE);
    }

    /**
     * Creates an invisible button for the scrollbar's decrease action. These buttons are hidden to
     * achieve a minimalist design.
     *
     * @param orientation The orientation of the button (e.g., NORTH, SOUTH).
     *
     * @return A JButton with no visible content or functionality.
     */
    @Override
    protected JButton createDecreaseButton(int orientation) {
      return createInvisibleButton();
    }

    /**
     * Creates an invisible button for the scrollbar's increase action. These buttons are hidden to
     * achieve a minimalist design.
     *
     * @param orientation The orientation of the button (e.g., NORTH, SOUTH).
     *
     * @return A JButton with no visible content or functionality.
     */
    @Override
    protected JButton createIncreaseButton(int orientation) {
      return createInvisibleButton();
    }

    /**
     * Paints the scrollbar's thumb with a rounded rectangle shape and anti-aliasing for smooth
     * edges.
     *
     * @param g The Graphics object used for painting.
     * @param c The component to be painted.
     * @param thumbBounds The bounding rectangle of the scrollbar thumb.
     */
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(thumbColor);
      g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 6, 6);

      g2.dispose();
    }

    /**
     * Creates an invisible button for scrollbar controls. These buttons have no dimensions or
     * functionality, ensuring they remain hidden.
     *
     * @return A JButton with no visible content or functionality.
     */
    private JButton createInvisibleButton() {
      JButton button = new JButton();
      button.setPreferredSize(new Dimension(0, 0));
      button.setMinimumSize(new Dimension(0, 0));
      button.setMaximumSize(new Dimension(0, 0));
      button.setFocusable(false);
      button.setBorderPainted(false);
      button.setOpaque(false);
      button.setContentAreaFilled(false);
      return button;
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
        simulator.step();
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
          simulator.step();
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

  private class LoadListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      FileDialog fileDialog = new FileDialog((Frame) null, "Load Assembly File", FileDialog.LOAD);
      fileDialog.setDirectory(System.getProperty("user.dir"));
      fileDialog.setFile("*.asm;*.txt");
      fileDialog.setVisible(true);

      String selectedFile = fileDialog.getFile();
      String selectedDirectory = fileDialog.getDirectory();

      if(selectedFile != null && selectedDirectory != null){
        File file = new File(selectedDirectory, selectedFile);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
          assemblyInput.setText("");
          String line;
          while((line = reader.readLine()) != null){
            assemblyInput.append(line + "\n");
          }
        } catch(IOException ioException){
          JOptionPane.showMessageDialog(null,
                                        "Error while reading file: " + ioException.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
}
