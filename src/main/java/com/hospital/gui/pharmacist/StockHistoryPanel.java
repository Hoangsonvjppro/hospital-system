package com.hospital.gui.pharmacist;

import com.hospital.bus.StockTransactionBUS;
import com.hospital.gui.common.*;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Lịch sử nhập/xuất kho — xem StockTransaction.
 */
public class StockHistoryPanel extends JPanel {

    private final StockTransactionBUS stockTxBUS = new StockTransactionBUS();

    private DefaultTableModel txModel;

    public StockHistoryPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadTransactions();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("📋 Lịch sử kho");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        RoundedButton btnRefresh = new RoundedButton("🔄 Làm mới");
        btnRefresh.setBackground(UIConstants.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadTransactions());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        txModel = new DefaultTableModel(
                new String[]{"ID", "Thuốc", "Lô", "Loại GD", "SL", "Trước", "Sau", "Ngày", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable txTable = new JTable(txModel);
        txTable.setRowHeight(32);
        txTable.setFont(UIConstants.FONT_LABEL);
        txTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        txTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadTransactions() {
        txModel.setRowCount(0);
        try {
            List<StockTransaction> list = stockTxBUS.findAll();
            for (StockTransaction tx : list) {
                String typeDisplay = switch (tx.getTransactionType()) {
                    case "IMPORT" -> "Nhập kho";
                    case "EXPORT_PRESCRIPTION" -> "Xuất theo đơn";
                    case "ADJUSTMENT" -> "Điều chỉnh";
                    case "RETURN_TO_SUPPLIER" -> "Trả NCC";
                    case "EXPIRED_DISPOSAL" -> "Hủy hết hạn";
                    default -> tx.getTransactionType();
                };
                txModel.addRow(new Object[]{
                        tx.getId(),
                        tx.getMedicineName() != null ? tx.getMedicineName() : "TH #" + tx.getMedicineId(),
                        tx.getBatchNumber() != null ? tx.getBatchNumber() : "",
                        typeDisplay,
                        tx.getQuantity(),
                        tx.getStockBefore(),
                        tx.getStockAfter(),
                        tx.getCreatedAt() != null ? tx.getCreatedAt().toLocalDate().toString() : "",
                        tx.getNotes() != null ? tx.getNotes() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử kho: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
