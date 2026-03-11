//Ghi chú:
//Muốn thêm thuốc thì nhấn Button "+ Thêm thuốc" ở góc trên phải màn hình,
//muốn sửa, xoá thì nhấn vào loại thuốc muốn chỉnh sửa ở JTable và nhấn chuột phải sẽ có PopupMenu
//hiện ra để lựa chọn sửa hoặc xoá.
//để chuột vào tên thuốc sẽ hiện lên mô tả.
package com.hospital.gui.panels;
import com.hospital.bus.*;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.gui.UIConstants;
import com.hospital.model.Invoice;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Medicine;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionDetail;
import com.hospital.util.AppUtils;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class MedicinePanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MedicinePanel.class.getName());
    private JToggleButton btnTatca, btnHethan, btnHetHang;
    private List<Medicine> currentList;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnNhapThuocMoi,btnNhapExcel,btnXuatExcel;
    private JLabel lblTongThuoc, lblTongTonKho, lblSapHetHan, lblSapHetHang;

    //
    private final MedicineBUS medicineBUS = new MedicineBUS();
    private final MedicalRecordBUS medicalRecordBUS = new MedicalRecordBUS();
    private final PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
    private final MedicineExportBUS medicineExportBUS = new MedicineExportBUS();
    private final InvoiceBUS invoiceBUS = new InvoiceBUS();
    //
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DataFormatter dataFormatter=new DataFormatter();

    //Component tab 2:
    private JTabbedPane tabbedPane;
    private DefaultTableModel modelPendingRecords;
    private JTable tablePendingRecords;
    private JTable tablePrescriptionDetails;
    private DefaultTableModel modelPrescriptionDetails;
    private JButton btnPhatThuoc;


    public MedicinePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        tabbedPane=new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);
        tabbedPane.addTab("Quản lý kho thuốc",createMedicinePanel());
        tabbedPane.addTab("Phát thuốc",createDispenseMedicinePanel());
        tabbedPane.addTab("Báo cáo & Thống kê", createReportTab());
        add(tabbedPane);
        loadMedicineData();
        addEvents();
    }

    private JPanel createMedicinePanel() {
        JPanel pnl=new JPanel(new BorderLayout(0,20));
        pnl.setBackground(UIConstants.CONTENT_BG);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

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

        pnlCards.add(createKPICard("Tổng danh mục thuốc", lblTongThuoc, "", null, UIConstants.ACCENT_BLUE));
        pnlCards.add(createKPICard("Tổng tồn kho", lblTongTonKho, "đv", null, UIConstants.ACCENT_BLUE));
        pnlCards.add(createKPICard("Sắp hết hạn (30 ngày)", lblSapHetHan, "", "", UIConstants.PRIMARY));
        pnlCards.add(createKPICard("Sắp hết hàng", lblSapHetHang, "", "", UIConstants.WARNING_ORANGE));

        JPanel pnlFilter = new JPanel(new BorderLayout());
        pnlFilter.setOpaque(false);

        JPanel pnlFilterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilterLeft.setOpaque(false);
        ButtonGroup groupFilter = new ButtonGroup();
        btnTatca=createFilterButton("Tất cả thuốc",true,groupFilter);
        btnHethan=createFilterButton("Thuốc hết hạn",false,groupFilter);
        btnHetHang=createFilterButton("Thuốc hết hàng",false,groupFilter);
        pnlFilterLeft.add(btnTatca);
        pnlFilterLeft.add(btnHethan);
        pnlFilterLeft.add(btnHetHang);
        JPanel pnlFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilterRight.setOpaque(false);
        String btnStyle = "background: #ffffff; arc: 10; borderWidth: 1; borderColor: #dddddd";
        btnNhapExcel=new JButton("Nhập Excel");
        btnXuatExcel=new JButton("Xuất Excel");
        btnNhapExcel.putClientProperty(FlatClientProperties.STYLE, btnStyle);
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, btnStyle);
        pnlFilterRight.add(btnNhapExcel);
        pnlFilterRight.add(btnXuatExcel);

        pnlFilter.add(pnlFilterLeft, BorderLayout.WEST);
        pnlFilter.add(pnlFilterRight, BorderLayout.EAST);

        pnlTop.add(pnlHeader);
        pnlTop.add(pnlCards);
        pnlTop.add(pnlFilter);
        pnl.add(pnlTop, BorderLayout.NORTH);

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
        table.setDefaultRenderer(Object.class,new InventoryCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }
    private JPanel createDispenseMedicinePanel(){
        JPanel pnl=new JPanel(new BorderLayout(10,10));
        pnl.setBackground(UIConstants.CONTENT_BG);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlTop=new JPanel(new BorderLayout());
        pnlTop.setBorder(BorderFactory.createTitledBorder("Danh sách thuốc chờ phát"));
        String[] colPending = {"ID Bệnh Án", "Tên Bệnh Nhân", "Bác Sĩ Kê Đơn", "Ngày Khám"};
        modelPendingRecords=new DefaultTableModel(colPending,0);
        tablePendingRecords=new JTable();
        tablePendingRecords.setModel(modelPendingRecords);
        tablePendingRecords.setRowHeight(35);
        pnlTop.add(new JScrollPane(tablePendingRecords),BorderLayout.CENTER);

        JPanel pnlBottom=new JPanel(new BorderLayout());
        pnlBottom.setBorder(BorderFactory.createTitledBorder("Chi tiết đơn thuốc"));
        String[] colDetails = {"Tên Thuốc", "Đơn Vị", "Số Lượng", "Đơn Giá", "Thành Tiền", "Hướng Dẫn"};
        modelPrescriptionDetails=new DefaultTableModel(colDetails,0);
        tablePrescriptionDetails=new JTable();
        tablePrescriptionDetails.setModel(modelPrescriptionDetails);
        tablePrescriptionDetails.setRowHeight(35);
        pnlBottom.add(new JScrollPane(tablePrescriptionDetails),BorderLayout.CENTER);

        JPanel pnlAction=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPhatThuoc=new JButton("Phát thuốc");
        btnPhatThuoc.setFont(UIConstants.FONT_BOLD);
        btnPhatThuoc.putClientProperty(FlatClientProperties.STYLE,"background: #198754; foreground: #ffffff; arc: 10; borderWidth: 0");
        btnPhatThuoc.setEnabled(false);
        pnlAction.add(btnPhatThuoc);
        pnlBottom.add(pnlAction,BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlTop, pnlBottom);
        splitPane.setDividerLocation(250);
        pnl.add(splitPane, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createReportTab() {
        JPanel pnlReport = new JPanel(new GridLayout(1, 2, 20, 0)); // Chia làm 2 cột
        pnlReport.setBackground(UIConstants.CONTENT_BG);
        pnlReport.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel pnlExpiring = new JPanel(new BorderLayout());
        pnlExpiring.setBorder(BorderFactory.createTitledBorder("CẢNH BÁO: Thuốc sắp hết hạn (Dưới 30 ngày)"));
        String[] colExpiring = {"Tên Thuốc", "Ngày Hết Hạn", "Tồn Kho"};
        DefaultTableModel modExpiring = new DefaultTableModel(colExpiring, 0);
        JTable tblExpiring = new JTable(modExpiring);
        List<Medicine> expiringList = medicineBUS.getExpiredMedicinesList(); // Cần wrap lại qua BUS
        for (Medicine m : expiringList) {
            modExpiring.addRow(new Object[]{m.getMedicineName(), m.getExpiryDate().format(dateFormatter), m.getStockQty()});
        }
        pnlExpiring.add(new JScrollPane(tblExpiring), BorderLayout.CENTER);
        JPanel pnlTopExport = new JPanel(new BorderLayout());
        pnlTopExport.setBorder(BorderFactory.createTitledBorder("Top 10 thuốc xuất kho nhiều nhất"));
        String[] colTop = {"Tên Thuốc", "Tổng SL Đã Xuất"};
        DefaultTableModel modTop = new DefaultTableModel(colTop, 0);
        JTable tblTop = new JTable(modTop);
        List<Object[]> topList = medicineBUS.getTopExportedMedicinesList();
        for (Object[] row : topList) {
            modTop.addRow(row);
        }
        pnlTopExport.add(new JScrollPane(tblTop), BorderLayout.CENTER);
        pnlReport.add(pnlExpiring);
        pnlReport.add(pnlTopExport);
        return pnlReport;
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
                    loadMedicineData();
                }
            });
            btnTatca.addActionListener(e -> {
                loadMedicineData();
            });
            btnHethan.addActionListener(e -> {
                List<Medicine> expiredMedicines=new ArrayList<>();
                expiredMedicines=medicineBUS.getExpiredMedicinesList();
                renderTable(expiredMedicines);
            });
            btnHetHang.addActionListener(e -> {
                List<Medicine> lowStock=new ArrayList<>();
                lowStock=medicineBUS.getLowStockMedicinesList();
                renderTable(lowStock);
            });

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem itemEdit = new JMenuItem("Sửa thông tin thuốc");
            JMenuItem itemDelete = new JMenuItem("Xóa thuốc này");
            JMenuItem itemImport=new JMenuItem("Nhập thêm");
            popupMenu.add(itemImport);
            popupMenu.addSeparator();
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
        itemImport.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Medicine selectedMedicine = currentList.get(row);
                String qtyStr = JOptionPane.showInputDialog(this,
                        "Nhập số lượng nhập kho cho thuốc '" + selectedMedicine.getMedicineName() + "':",
                        "Nhập kho", JOptionPane.QUESTION_MESSAGE);
                if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        // Giả định currentUserId = 1 (hoặc lấy từ phiên đăng nhập thực tế)
                        medicineBUS.importStock(selectedMedicine.getId(), qty, 1, "Nhập kho bổ sung từ giao diện");
                        JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                        loadMedicineData();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập kho", JOptionPane.ERROR_MESSAGE);
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
                        loadMedicineData();
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
                                loadMedicineData();
                            } else {
                                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thuốc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (BusinessException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
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
                    loadMedicineData();
                }
            }
        });
        btnXuatExcel.addActionListener(e->{
            String[] columns = {"MÃ","TÊN THUỐC", "ĐƠN VỊ TÍNH", "GIÁ BÁN", "SỐ LƯỢNG TỒN", "HẠN SỬ DỤNG", "TRẠNG THÁI"};
            JFileChooser fileChooser=new JFileChooser();
            fileChooser.setDialogTitle("Chọn nơi lưu");
            int choice=fileChooser.showSaveDialog(null);
            if(choice==JFileChooser.APPROVE_OPTION){
                File file=fileChooser.getSelectedFile();
                String filepath=file.getAbsolutePath();
                if(!filepath.endsWith(".xlsx")){
                    filepath+=".xlsx";
                }
                try(Workbook workbook=new XSSFWorkbook()){
                    Sheet sheet=workbook.createSheet("Thuốc");
                    Row headerRow=sheet.createRow(0);

                    for(int i=0;i<columns.length;i++) {
                        headerRow.createCell(i).setCellValue(columns[i]);//tạo header
                    }
                    int rowIndex=1;
                    for (Medicine med : currentList) {
                        Row row = sheet.createRow(rowIndex++);
                        row.createCell(0).setCellValue("MED" + med.getId());
                        row.createCell(1).setCellValue(med.getMedicineName());
                        row.createCell(2).setCellValue(med.getUnit());
                        row.createCell(3).setCellValue(med.getSellPrice());
                        row.createCell(4).setCellValue(med.getStockQty());
                        row.createCell(5).setCellValue(med.getExpiryDate() != null ? med.getExpiryDate().format(dateFormatter) : "");
                        String status = "Còn hàng";
                        if (med.getStockQty() <= 0) status = "Hết hàng";
                        else if (med.getStockQty() <= med.getMinThreshold()) status = "Sắp hết hàng";
                        row.createCell(6).setCellValue(status);
                    }
                    workbook.write(new FileOutputStream(filepath));
                    workbook.close();
                }catch (IOException ex){
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnNhapExcel.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn file Excel nhập thuốc");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (java.io.InputStream fis = new java.io.FileInputStream(file);
                     Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    int countSuccess = 0;
                    int countError = 0;
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null || row.getCell(0) == null) continue;
                        try {
                            Medicine med = new Medicine();
                            // Đọc trực tiếp và format thành String ngay tại chỗ
                            med.setMedicineCode(dataFormatter.formatCellValue(row.getCell(0)));
                            med.setMedicineName(dataFormatter.formatCellValue(row.getCell(1)));
                            med.setGenericName(dataFormatter.formatCellValue(row.getCell(2)));
                            med.setUnit(dataFormatter.formatCellValue(row.getCell(3)));

                            String costStr = dataFormatter.formatCellValue(row.getCell(4)).replaceAll("[^\\d.]", "");
                            med.setCostPrice(costStr.isEmpty() ? 0 : Double.parseDouble(costStr));

                            String sellStr = dataFormatter.formatCellValue(row.getCell(5)).replaceAll("[^\\d.]", "");
                            med.setSellPrice(sellStr.isEmpty() ? 0 : Double.parseDouble(sellStr));

                            String stockStr = dataFormatter.formatCellValue(row.getCell(6)).replaceAll("[^\\d]", "");
                            med.setStockQty(stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr));

                            String thresholdStr = dataFormatter.formatCellValue(row.getCell(7)).replaceAll("[^\\d]", "");
                            med.setMinThreshold(thresholdStr.isEmpty() ? 0 : Integer.parseInt(thresholdStr));

                            med.setManufacturer(dataFormatter.formatCellValue(row.getCell(8)));

                            String dateStr = dataFormatter.formatCellValue(row.getCell(9));
                            if (!dateStr.isEmpty()) {
                                med.setExpiryDate(LocalDate.parse(dateStr, dateFormatter));
                            }
                            med.setDescription(dataFormatter.formatCellValue(row.getCell(10)));

                            if (medicineBUS.insert(med)) countSuccess++; else countError++;
                        } catch (Exception ex) {
                            countError++;
                            LOGGER.warning("Lỗi dòng " +(i+1) + ": " + ex.getMessage());
                        }
                    }
                    loadMedicineData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi đọc file: " + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //tab 2
        tablePendingRecords.getSelectionModel().addListSelectionListener(e ->{
           if(!e.getValueIsAdjusting()&&tablePendingRecords.getSelectedRow()!=-1){
               btnPhatThuoc.setEnabled(true);
               long recordId=Long.parseLong(tablePendingRecords.getValueAt(tablePendingRecords.getSelectedRow(),0).toString());
               loadPrescriptionDetails(recordId);
           }
        });
        btnPhatThuoc.addActionListener(e->{
            int row=tablePendingRecords.getSelectedRow();
            if(row==-1) return;
            long recordId=Long.parseLong(tablePendingRecords.getValueAt(row,0).toString());
            try {
                // 1. Lấy đơn thuốc + chi tiết từ DB
                List<Prescription> prescriptions = prescriptionBUS.getByMedicalRecordId(recordId);
                List<PrescriptionDetail> allDetails = new ArrayList<>();
                for (Prescription p : prescriptions) {
                    if ("CONFIRMED".equals(p.getStatus())) {
                        allDetails.addAll(prescriptionBUS.getDetails(p.getId()));
                    }
                }
                if (allDetails.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không có chi tiết đơn thuốc nào để phát.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 2. Xuất kho thuốc
                String msg = medicineExportBUS.processPrescriptionExport(allDetails, 1, true);
                LOGGER.info(msg);

                // 3. Cập nhật trạng thái đơn thuốc → DISPENSED
                for (Prescription p : prescriptions) {
                    if ("CONFIRMED".equals(p.getStatus())) {
                        prescriptionBUS.updateStatus(p.getId(), Prescription.STATUS_DISPENSED);
                    }
                }

                // 4. Chuyển trạng thái bệnh án → COMPLETED
                medicalRecordBUS.updateStatus(recordId, "COMPLETED");

                // 5. Tạo hóa đơn
                Invoice invoice = invoiceBUS.createInvoiceFromMedicalRecord(recordId);
                String invoiceMsg = invoice != null ? "\nMã hóa đơn: " + invoice.getId() : "";
                JOptionPane.showMessageDialog(null, "Đã phát thuốc thành công!" + invoiceMsg + "\nHóa đơn đã được chuyển sang bộ phận Kế toán.", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                loadMedicineData();
                loadPendingPrescriptions();
                modelPrescriptionDetails.setRowCount(0);
                btnPhatThuoc.setEnabled(false);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi phát thuốc: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        }
    private void loadPendingPrescriptions(){
        modelPendingRecords.setRowCount(0);
        try {
            List<Prescription> pending = prescriptionBUS.getPendingPrescriptions();
            for (Prescription p : pending) {
                long recordId = p.getMedicalRecordId();
                MedicalRecord rec = medicalRecordBUS.findById(recordId);
                String patientName = "BN #" + (rec != null ? rec.getPatientId() : "?");
                String doctorName = "BS #" + (rec != null ? rec.getDoctorId() : "?");
                String visitDate = "";
                if (rec != null && rec.getVisitDate() != null) {
                    visitDate = rec.getVisitDate().format(dateFormatter);
                }
                modelPendingRecords.addRow(new Object[]{
                    String.valueOf(recordId), patientName, doctorName, visitDate
                });
            }
        } catch (Exception e) {
            LOGGER.warning("Lỗi tải danh sách chờ phát thuốc: " + e.getMessage());
        }
    }

    private void loadPrescriptionDetails(long recordId) {
        modelPrescriptionDetails.setRowCount(0);
        try {
            List<Prescription> prescriptions = prescriptionBUS.getByMedicalRecordId(recordId);
            for (Prescription p : prescriptions) {
                List<PrescriptionDetail> details = prescriptionBUS.getDetails(p.getId());
                for (PrescriptionDetail d : details) {
                    String medName = d.getMedicineName() != null ? d.getMedicineName() : "Thuốc #" + d.getMedicineId();
                    double lineTotal = d.getLineTotal();
                    modelPrescriptionDetails.addRow(new Object[]{
                        medName, "", d.getQuantity(), formatter.format(d.getUnitPrice()), formatter.format(lineTotal),
                        d.getInstruction() != null ? d.getInstruction() : d.getDosage()
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Lỗi tải chi tiết đơn thuốc: " + e.getMessage());
        }
    }

    private void loadMedicineData() {
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

            String tenThuocFormat = "<html><b>" + thuoc.getMedicineName() + "</b><br><span style='color:gray; font-size:9px'>Mã: MED" + String.format("%03d", thuoc.getId()) + "</span></html>";
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

    static class StatusCellRenderer extends DefaultTableCellRenderer {
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
    static class InventoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = table.getValueAt(row, 5).toString(); // Cột trạng thái

            if (!isSelected) {
                if (status.contains("Hết hàng") || status.contains("Đã hết hạn")) {
                    c.setBackground(UIConstants.ERROR_COLOR);
                    c.setForeground(UIConstants.ERROR_COLOR);
                } else if (status.contains("Sắp hết hàng") || status.contains("Sắp hết hạn")) {
                    c.setBackground(UIConstants.PRIMARY_BG_SOFT);
                    c.setForeground(new Color(150, 100, 0));
                } else {
                    c.setBackground(Color.WHITE); // Bình thường
                    c.setForeground(table.getForeground());
                }
            }
            return c;
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
    private JTextField txtName, txtUnit, txtCostPrice, txtSellPrice, txtStock, txtMinThreshold,txtDescription,txtManufacturer;
    public MedicineDialog(Frame parent, Medicine medicine) {
        super(parent, medicine == null ? "Thêm Thuốc" : "Sửa Thuốc", true);
        this.currentMedicine = medicine;
        initComponents();
        if (medicine != null) fillData();
    }
    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        JPanel pnl = new JPanel(new GridLayout(9, 2, 10, 15));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.add(new JLabel("Tên thuốc:")); txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtName);
        pnl.add(new JLabel("Đơn vị:")); txtUnit = new JTextField();
        txtUnit.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtUnit);
        pnl.add(new JLabel("Giá nhập:")); txtCostPrice = new JTextField();
        txtCostPrice.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtCostPrice);
        pnl.add(new JLabel("Giá bán:")); txtSellPrice = new JTextField();
        txtSellPrice.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtSellPrice);
        pnl.add(new JLabel("Tồn kho:")); txtStock = new JTextField();
        txtStock.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtStock);
        pnl.add(new JLabel("Mức báo hết:")); txtMinThreshold = new JTextField();
        txtMinThreshold.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtMinThreshold);
        pnl.add(new JLabel("Nhà cung cấp")); txtManufacturer = new JTextField();
        txtManufacturer.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtManufacturer);
        pnl.add(new JLabel("Ngày hết hạn:"));
        txtExpiryDate=new JFormattedTextField(simpleDateFormat); txtExpiryDate.setValue(new Date());
        txtExpiryDate.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
        pnl.add(txtExpiryDate);
        pnl.add(new JLabel("Thông tin thuốc:")); txtDescription=new JTextField();
        txtDescription.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin:5,10,5,10");
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
        txtManufacturer.setText(currentMedicine.getManufacturer());
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
            currentMedicine.setManufacturer(txtManufacturer.getText().trim());
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