package TestGUI;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

public class ListGUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable itemList;
    private JButton addButton;
    private JButton removeButton;
    private JTextField textField;
    private Set<String> existingElements;
    private Map<Integer, SortOrder> columnSortOrderMap;

    public ListGUI() {
        setTitle("StocksMonitor");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(450, 300));

        tableModel = new DefaultTableModel(new String[]{"Nome 📈", "Valor 📉"}, 0);
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
                if (!newItem.isEmpty() && !existingElements.contains(newItem)) {
                    Object[] row = {newItem, "--"};
                    tableModel.addRow(row);
                    existingElements.add(newItem);
                    textField.setText("");
                    saveToFile();
                    System.out.println("Inserção conluída!");
                } else if (existingElements.contains(newItem)) {
                    JOptionPane.showMessageDialog(ListGUI.this, "Element já existe na lista!", "Erro de inserção", JOptionPane.ERROR_MESSAGE);
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
                    String removedElement = tableModel.getValueAt(selectedModelRow, 0).toString();
                    tableModel.removeRow(selectedModelRow);
                    existingElements.remove(removedElement);
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

        existingElements = new HashSet<>();
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
                        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(columnIndex, nextSortOrder)));
                    }
                    columnSortOrderMap.put(columnIndex, nextSortOrder);
                }
            }
        });

        populateTableFromFile();
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

    private void populateTableFromFile() {
        File file = new File("list.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length == 2) {
                        tableModel.addRow(columns);
                        existingElements.add(columns[0]);
                    }
                }
                System.out.println("Leitura concluída!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao ler a lista.", "Erro de leitura", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("list.txt"))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(tableModel.getValueAt(i, j).toString());
                    if (j < tableModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
            System.out.println("Gravação concluída!");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao gravar a lista.", "Erro de gravação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ListGUI listGUI = new ListGUI();
                listGUI.setVisible(true);
            }
        });
    }
}
