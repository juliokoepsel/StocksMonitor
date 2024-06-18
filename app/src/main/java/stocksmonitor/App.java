package stocksmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class App extends JFrame {
    private static DefaultTableModel tableModel;

    private JTable itemList;
    private JButton addButton;
    private JButton removeButton;
    private JTextField textField;
    private Map<Integer, SortOrder> columnSortOrderMap;

    private static Set<String> availableElements;
    private static List<String> existingElements;
    private static List<Float> existingValues;

    private static String SERVER_ADDRESS = "localhost";
    private static int SERVER_PORT = 31415;

    public App() {
        setTitle("📈  StocksMonitor");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(450, 300));

        tableModel = new DefaultTableModel(new String[] { "Nome", "Valor (BRL)" }, 0);
        itemList = new JTable(tableModel);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        itemList.setRowSorter(sorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        itemList.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemList.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        addButton = new JButton("Inserir");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newItem = textField.getText().trim();
                if (!newItem.isEmpty() && !existingElements.contains(newItem) && availableElements.contains(newItem)) {
                    Object[] row = { newItem, "0.0" };
                    tableModel.addRow(row);
                    existingElements.add(newItem);
                    existingValues.add(0.0f);
                    textField.setText("");
                    saveToFile();
                    System.out.println("Inserção conluída!");
                } else if (existingElements.contains(newItem)) {
                    JOptionPane.showMessageDialog(App.this, "Elemento já existe na lista!", "Erro de inserção",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!availableElements.contains(newItem)) {
                    JOptionPane.showMessageDialog(App.this, "Elemento não foi encontrado!", "Erro de inserção",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeButton = new JButton("Remover");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedViewRow = itemList.getSelectedRow();
                if (selectedViewRow != -1) {
                    int selectedModelRow = itemList.convertRowIndexToModel(selectedViewRow);
                    String selectedValue = tableModel.getValueAt(selectedModelRow, 0).toString();
                    tableModel.removeRow(selectedModelRow);
                    for (int i = 0; i < existingElements.size(); i++) {
                        if (existingElements.get(i).equals(selectedValue)) {
                            existingElements.remove(i);
                            existingValues.remove(i);
                        }
                    }
                    saveToFile();
                    System.out.println("Remoção conluída!");
                }
            }
        });

        textField = new JTextField(15);

        JPanel inputPanel = new JPanel();
        inputPanel.add(textField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);

        JScrollPane scrollPane = new JScrollPane(itemList);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        columnSortOrderMap = new HashMap<>();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            columnSortOrderMap.put(i, SortOrder.UNSORTED);
        }

        JTableHeader tableHeader = itemList.getTableHeader();
        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = tableHeader.columnAtPoint(e.getPoint());
                if (columnIndex != -1) {
                    SortOrder currentSortOrder = columnSortOrderMap.get(columnIndex);
                    SortOrder nextSortOrder = getNextSortOrder(currentSortOrder);
                    if (nextSortOrder == SortOrder.UNSORTED) {
                        sorter.setSortKeys(null);
                    } else {
                        sorter.setSortKeys(
                                Collections.singletonList(new RowSorter.SortKey(columnIndex, nextSortOrder)));
                    }
                    columnSortOrderMap.put(columnIndex, nextSortOrder);
                }
            }
        });

        populateElements();
    }

    private SortOrder getNextSortOrder(SortOrder currentSortOrder) {
        switch (currentSortOrder) {
            case ASCENDING:
                return SortOrder.DESCENDING;
            case DESCENDING:
                return SortOrder.UNSORTED;
            default:
                return SortOrder.ASCENDING;
        }
    }

    private static void saveToFile() {
        String fileName = "list.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < existingElements.size(); i++) {
                writer.write(existingElements.get(i));
                writer.write(",");
                writer.write(existingValues.get(i).toString());
                writer.write("\n");
            }
            writer.close();
            System.out.println("(" + fileName + "): Gravação concluída!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro de gravação: Erro ao gravar " + fileName);
        }
    }

    private static void readConnectionFile() {
        String fileName = "client_connection.cfg";
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line = br.readLine();
                if (line != null) {
                    SERVER_ADDRESS = line;
                }
                line = br.readLine();
                if (line != null) {
                    SERVER_PORT = Integer.parseInt(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de leitura: Erro ao ler " + fileName);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.err.println("Erro de formatação numérica: Erro ao ler " + fileName);
            }
        } else {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
                bw.write(SERVER_ADDRESS);
                bw.newLine();
                bw.write(Integer.toString(SERVER_PORT));
                System.out.println("(" + fileName + "): Gravação concluída!");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de gravação: Erro ao gravar " + fileName);
            }
        }
    }

    private static void populateAvailableElements() {
        availableElements = new HashSet<>();
        availableElements.add("MGLU3");
        availableElements.add("PETR4");
        availableElements.add("RRRP3");
        availableElements.add("GOLD11");
        availableElements.add("COGN3");
        availableElements.add("ABEV3");
        availableElements.add("VALE3");
        availableElements.add("SOJA3");
        availableElements.add("EMBR3");
        availableElements.add("OIBR4");
    }

    private static void populateElements() {
        existingElements = new ArrayList<>();
        existingValues = new ArrayList<>();
        String fileName = "list.txt";
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length == 2) {
                        existingElements.add(columns[0]);
                        existingValues.add(Float.parseFloat(columns[1]));
                    }
                }
                System.out.println("(" + fileName + "): Leitura concluída!");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro de leitura: Erro ao ler " + fileName);
            }
        }
        for (int i = 0; i < existingElements.size(); i++) {
            String[] aux = new String[2];
            aux[0] = existingElements.get(i);
            aux[1] = existingValues.get(i).toString();
            tableModel.addRow(aux);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        existingElements.clear();
        populateElements();
    }

    public static void main(String[] args) {
        readConnectionFile();
        populateAvailableElements();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                App listGUI = new App();
                listGUI.setVisible(true);
                System.out.println("Servidor: " + SERVER_ADDRESS + ":" + SERVER_PORT);

                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                            System.out.println("Realizando requisição ao servidor!");
                            out.writeObject(existingElements);
                            out.flush();
                            System.out.println("Elementos enviados!");
                            @SuppressWarnings("unchecked")
                            List<Float> receivedElements = (List<Float>) in.readObject();
                            System.out.println("Resposta:");
                            for (Float element : receivedElements) {
                                System.out.println(element);
                            }
                            existingValues = receivedElements;
                            saveToFile();
                            listGUI.refreshTable();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            System.err
                                    .println("Erro de conexão: Erro ao enviar dados ao servidor " + SERVER_ADDRESS + ":"
                                            + SERVER_PORT);
                        }
                    }
                };
                timer.schedule(task, 0, 60000);
            }
        });

    }
}
