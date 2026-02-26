//Ghi chú:
//Muốn thêm thuốc thì nhấn Button "+ Thêm thuốc" ở góc trên phải màn hình,
//muốn sửa, xoá thì nhấn vào loại thuốc muốn chỉnh sửa ở JTable và nhấn chuột phải sẽ có PopupMenu
//hiện ra để lựa chọn sửa hoặc xoá.
//để chuột vào tên thuốc sẽ hiện lên mô tả.
package com.hospital.gui.panels;
import com.hospital.bus.MedicineBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.model.Medicine;
import com.hospital.util.AppUtils;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class MedicinePanel extends JPanel {
    private List<Medicine> currentList;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnNhapThuocMoi;
    private MedicineBUS medicineBUS = new MedicineBUS();
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private JLabel lblTongThuoc, lblTongTonKho, lblSapHetHan, lblSapHetHang;

    public MedicinePanel() {
        initLayout();
        loadData();
        addEvents();
    }

    private void initLayout() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= 1. KHU VỰC PHÍA TRÊN (TOP) =================
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Thuốc");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +8");
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlHeaderRight.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm thuốc...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnNhapThuocMoi = new JButton("+ Nhập thuốc mới");
        btnNhapThuocMoi.putClientProperty(FlatClientProperties.STYLE,
                "background: #0d6efd; foreground: #ffffff; arc: 10; font: bold; borderWidth: 0;");

        pnlHeaderRight.add(txtSearch);
        pnlHeaderRight.add(btnNhapThuocMoi);
        pnlHeader.add(pnlHeaderRight, BorderLayout.EAST);

        // 1.2 Thẻ Thống kê (KPI Cards)
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlCards.setOpaque(false);
        pnlCards.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Khởi tạo các Label chứa giá trị KPI để update sau
        lblTongThuoc = new JLabel("0");
        lblTongTonKho = new JLabel("0");
        lblSapHetHan = new JLabel("0");
        lblSapHetHang = new JLabel("0");

        pnlCards.add(createKPICard("Tổng danh mục thuốc", lblTongThuoc, "", null, null));
        pnlCards.add(createKPICard("Tổng tồn kho", lblTongTonKho, "đv", null, null));
        pnlCards.add(createKPICard("Sắp hết hạn (30 ngày)", lblSapHetHan, "", "", UIConstants.PRIMARY));
        pnlCards.add(createKPICard("Sắp hết hàng", lblSapHetHang, "", "", UIConstants.WARNING_ORANGE));

        JPanel pnlFilter = new JPanel(new BorderLayout());
        pnlFilter.setOpaque(false);

        JPanel pnlFilterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilterLeft.setOpaque(false);
        ButtonGroup groupFilter = new ButtonGroup();
        pnlFilterLeft.add(createFilterButton("Tất cả thuốc", true, groupFilter));
//        pnlFilterLeft.add(createFilterButton("Kháng sinh", false, groupFilter));
        JPanel pnlFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilterRight.setOpaque(false);
        JButton btnBoLoc = new JButton("Bộ lọc nâng cao");
        JButton btnXuatExcel = new JButton("Xuất Excel");
        String btnStyle = "background: #ffffff; arc: 10; borderWidth: 1; borderColor: #dddddd";
        btnBoLoc.putClientProperty(FlatClientProperties.STYLE, btnStyle);
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, btnStyle);
//        pnlFilterRight.add(btnBoLoc);
//        pnlFilterRight.add(btnXuatExcel);

        pnlFilter.add(pnlFilterLeft, BorderLayout.WEST);
        pnlFilter.add(pnlFilterRight, BorderLayout.EAST);

        pnlTop.add(pnlHeader);
        pnlTop.add(pnlCards);
        pnlTop.add(pnlFilter);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = {"TÊN THUỐC", "ĐƠN VỊ TÍNH", "GIÁ BÁN", "SỐ LƯỢNG TỒN", "HẠN SỬ DỤNG", "TRẠNG THÁI"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model){
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row > -1 && col == 0) {
                    Medicine m = currentList.get(row);
                    String desc = m.getDescription();
                    if (desc != null && !desc.isEmpty()) {
                        return "<html><div style='width: 200px;'>" + desc + "</div></html>";
                    }
                    return "Chưa có mô tả cho thuốc này";
                }
                return super.getToolTipText(e);
            }

        };

        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "font: bold; height: 40; background: #ffffff");

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createKPICard(String title, JLabel lblValue, String unit, String badge, Color leftBorderColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0");

        if (leftBorderColor != null) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, leftBorderColor),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        } else {
            card.setBorder(new EmptyBorder(15, 15, 15, 15));
        }

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);
        pnlTop.add(lblTitle, BorderLayout.WEST);
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlBottom.setOpaque(false);
        lblValue.putClientProperty(FlatClientProperties.STYLE, "font: bold +12");
        pnlBottom.add(lblValue);

        if (!unit.isEmpty()) {
            JLabel lblUnit = new JLabel(unit);
            lblUnit.setForeground(Color.GRAY);
            pnlBottom.add(lblUnit);
        }

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlBottom, BorderLayout.SOUTH);

        return card;
    }

    private JToggleButton createFilterButton(String text, boolean isSelected, ButtonGroup group) {
        JToggleButton btn = new JToggleButton(text);
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; margin: 5,15,5,15; borderWidth: 0; focusWidth: 0;" +
                        "background: #ffffff; foreground: #666666;" +
                        "selectedBackground: #e8f0fe; selectedForeground: #0d6efd;");
        btn.setSelected(isSelected);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        group.add(btn);
        return btn;
    }

    private void addEvents() {
            btnNhapThuocMoi.addActionListener(e -> {
                MedicineDialog dialog = new MedicineDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
                dialog.setVisible(true);
                if (dialog.isDataChanged()) {
                    loadData();
                }
            });

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem itemEdit = new JMenuItem("Sửa thông tin thuốc");
            JMenuItem itemDelete = new JMenuItem("Xóa thuốc này");
            popupMenu.add(itemEdit);
            popupMenu.addSeparator();
            popupMenu.add(itemDelete);
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        int row =table.rowAtPoint(e.getPoint());
                        if (row >= 0 && row < table.getRowCount()) {
                            table.setRowSelectionInterval(row, row); // Tự động chọn dòng bị click
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
            itemEdit.addActionListener(e -> {
                int row=table.getSelectedRow();
                if (row >= 0) {
                    Medicine selectedMedicine = currentList.get(row); // Lấy object Thuốc từ danh sách
                    // Mở Dialog với chế độ "Sửa" (truyền object vào)
                    MedicineDialog dialog = new MedicineDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedMedicine);
                    dialog.setVisible(true);

                    if (dialog.isDataChanged()) {
                        loadData();
                    }
                }
            });

            itemDelete.addActionListener(e -> {
                int row=table.getSelectedRow();
                if (row >= 0) {
                    Medicine selectedMedicine = currentList.get(row);
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Bạn có chắc muốn xóa thuốc: " + selectedMedicine.getMedicineName() + "?",
                            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            if (medicineBUS.delete(selectedMedicine.getId())) {
                                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                                loadData();
                            } else {
                                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thuốc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (DataAccessException ex) {
                            AppUtils.showError(this, "Lỗi hệ thống: " + ex.getMessage());
                        }
                    }
                }
            });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(!txtSearch.getText().trim().isEmpty()){
                    renderTable(medicineBUS.findByName(txtSearch.getText().trim()));
                }
                else{
                    loadData();
                }
            }
        });
    }

    private void loadData() {
        currentList=medicineBUS.findAll();
        renderTable(currentList);

    }
    private void renderTable(List<Medicine> arr){
        model.setRowCount(0);
        int tonKho = 0;
        int sapHetHang = 0;
        for (Medicine thuoc : arr) {
            tonKho += thuoc.getStockQty();
            String trangThai = "Còn hàng";
            if (thuoc.getStockQty()==0) {
                trangThai="Hết hàng";
            }
            else if (thuoc.getStockQty() <= thuoc.getMinThreshold()) {
                trangThai = "Sắp hết hàng";
                sapHetHang++;
            }

            String tenThuocFormat = "<html><b>" + thuoc.getMedicineName() + "</b><br><span style='color:gray; font-size:9px'>Mã: " + String.format("%03d", thuoc.getId()) + "</span></html>";
            model.addRow(new Object[]{
                    tenThuocFormat,
                    thuoc.getUnit(),
                    formatter.format(thuoc.getSellPrice()) + " đ",
                    thuoc.getStockQty(),
                    thuoc.getExpiryDate() != null ? thuoc.getExpiryDate().format(dateFormatter) : "Không có",
                    trangThai
            });
        }
        lblTongThuoc.setText(formatter.format(arr.size()));
        lblTongTonKho.setText(formatter.format(tonKho));
        lblSapHetHang.setText(String.valueOf(sapHetHang));
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel wrapper = new JPanel(new GridBagLayout());
            if(isSelected) {
                wrapper.setBackground(table.getSelectionBackground());
            } else {
                wrapper.setBackground(Color.WHITE);
            }
            wrapper.add(label);
            return wrapper;
        }
    }
}
class MedicineDialog extends JDialog {
    private static final DateTimeFormatter dateFormat=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
    private Medicine currentMedicine;
    private boolean isDataChanged=false;
    private MedicineBUS bus = new MedicineBUS();
    private JFormattedTextField txtExpiryDate;
    private JTextField txtName, txtUnit, txtCostPrice, txtSellPrice, txtStock, txtMinThreshold,txtDescription;
    public MedicineDialog(Frame parent, Medicine medicine) {
        super(parent, medicine == null ? "Thêm Thuốc" : "Sửa Thuốc", true);
        this.currentMedicine = medicine;
        initComponents();
        if (medicine != null) fillData();
    }
    private void initComponents() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        JPanel pnl = new JPanel(new GridLayout(8, 2, 10, 15));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.add(new JLabel("Tên thuốc:")); txtName = new JTextField();
        pnl.add(txtName);
        pnl.add(new JLabel("Đơn vị:")); txtUnit = new JTextField();
        pnl.add(txtUnit);
        pnl.add(new JLabel("Giá nhập:")); txtCostPrice = new JTextField();
        pnl.add(txtCostPrice);
        pnl.add(new JLabel("Giá bán:")); txtSellPrice = new JTextField();
        pnl.add(txtSellPrice);
        pnl.add(new JLabel("Tồn kho:")); txtStock = new JTextField();
        pnl.add(txtStock);
        pnl.add(new JLabel("Mức báo hết:")); txtMinThreshold = new JTextField();
        pnl.add(txtMinThreshold);
        pnl.add(new JLabel("Ngày hết hạn:"));
        txtExpiryDate=new JFormattedTextField(simpleDateFormat); txtExpiryDate.setValue(new Date());
        pnl.add(txtExpiryDate);
        pnl.add(new JLabel("Thông tin thuốc:")); txtDescription=new JTextField();
        pnl.add(txtDescription);
        add(pnl, BorderLayout.CENTER);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            save();
        });

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtn.add(btnSave);
        add(pnlBtn, BorderLayout.SOUTH);
    }

    private void fillData() {
        txtName.setText(currentMedicine.getMedicineName());
        txtUnit.setText(currentMedicine.getUnit());
        txtCostPrice.setText(String.valueOf(currentMedicine.getCostPrice()));
        txtSellPrice.setText(String.valueOf(currentMedicine.getSellPrice()));
        txtStock.setText(String.valueOf(currentMedicine.getStockQty()));
        txtMinThreshold.setText(String.valueOf(currentMedicine.getMinThreshold()));
        if (currentMedicine.getExpiryDate()!=null) {
            txtExpiryDate.setText(currentMedicine.getExpiryDate().format(dateFormat));
        }
        txtDescription.setText(currentMedicine.getDescription());
    }

    private void save() {
        try {
            if (currentMedicine == null) currentMedicine = new Medicine();
            currentMedicine.setMedicineName(txtName.getText().trim());
            currentMedicine.setUnit(txtUnit.getText().trim());
            currentMedicine.setCostPrice(parseDouble(txtCostPrice, "Giá nhập"));
            currentMedicine.setSellPrice(parseDouble(txtSellPrice, "Giá bán"));
            currentMedicine.setStockQty(parseInt(txtStock, "Số lượng tồn"));
            currentMedicine.setMinThreshold(parseInt(txtMinThreshold, "Mức báo hết"));
            currentMedicine.setExpiryDate(LocalDate.parse(txtExpiryDate.getText(), dateFormat));
            currentMedicine.setDescription(txtDescription.getText());
            boolean res = (currentMedicine.getId() == 0) ? bus.insert(currentMedicine) : bus.update(currentMedicine);
            if (res) {
                JOptionPane.showMessageDialog(this, "Lưu thành công!");
                isDataChanged = true;
                dispose();
            }
        } catch (BusinessException ex) {
            AppUtils.showError(this, ex.getMessage());
        } catch (DataAccessException ex) {
            AppUtils.showError(this, "Lỗi hệ thống: " + ex.getMessage());
        } catch (Exception ex) {
            AppUtils.showError(this, "Dữ liệu không hợp lệ: " + ex.getMessage());
        }
    }

    private double parseDouble(JTextField field, String fieldName) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + " phải là số hợp lệ.");
        }
    }

    private int parseInt(JTextField field, String fieldName) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + " phải là số nguyên hợp lệ.");
        }
    }
    public boolean isDataChanged() { return isDataChanged; }
}