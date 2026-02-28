package com.hospital.bus;

import com.hospital.dao.MedicineDAO;
import com.hospital.dao.MedicineExportDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Medicine;
import com.hospital.model.MedicineExport;
import com.hospital.model.PrescriptionDetail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicineExportBUS {
    private final MedicineExportDAO exportDAO;
    private final MedicineDAO medicineDAO;

    public MedicineExportBUS() {
        this.exportDAO=new MedicineExportDAO();
        this.medicineDAO=new MedicineDAO();
    }
    /*
     * Xử lý nghiệp vụ xuất kho cho một đơn thuốc
     * * @param details Danh sách chi tiết các loại thuốc trong đơn (PrescriptionDetail)
     * @param pharmacistId ID của người duyệt xuất thuốc (Dược sĩ)
     * @param allowPartialExport true: Xuất những thuốc đủ, bỏ qua thuốc thiếu. false: Nếu thiếu 1 loại sẽ hủy toàn bộ.
     * @return Chuỗi thông báo kết quả để hiển thị lên UI
     */
    public String processPrescriptionExport(List<PrescriptionDetail> details,int pharmacistId,boolean allowPartialExport){
        // Bước 1: Kiểm tra tồn kho cho TỪNG DÒNG trong đơn thuốc
        List<String> missingMedicines=new ArrayList<>();
        List<PrescriptionDetail> exportable=new ArrayList<>();
        for(PrescriptionDetail detail:details){
            Medicine med=medicineDAO.findById(detail.getMedicineId());
            if(med==null){
                missingMedicines.add("Không tìm thấy thuốc có id: "+detail.getMedicineId()+" trong hệ thống");
                continue;
            }
            int requiredQty=detail.getQuantity();
            int currentStock=med.getStockQty();
            if(currentStock<requiredQty){
                missingMedicines.add(String.format("- Thuốc '%s' (Mã: %s): Cần %d, Tồn kho chỉ còn %d",
                        med.getMedicineName(), med.getId(), requiredQty, currentStock));
            } else {
                exportable.add(detail);
            }
        }
        // Bước 2: Xử lý dựa vào chính sách "cho phép xuất một phần" hay không
        if(!missingMedicines.isEmpty()){
            String errorMsg = "CẢNH BÁO TỒN KHO - DANH SÁCH THUỐC THIẾU:\n" + String.join("\n", missingMedicines);
            if(!allowPartialExport){
                throw new BusinessException(errorMsg + "\n\nXuất thuốc thất bại. Vui lòng nhập thêm kho hoặc đánh dấu 'Cho phép xuất một phần'.");
            }
            System.out.println("Tiến hành xuất kho một phần. Các thuốc thiếu:\n" + errorMsg);//test thử
        }
        // Bước 3: Tiến hành gọi DAO để xuất các thuốc ĐỦ ĐIỀU KIỆN
        int successCount=0;
        for (PrescriptionDetail detail:exportable){
            MedicineExport exportInfo = new MedicineExport(
                    detail.getId(),          // ID chi tiết đơn thuốc
                    detail.getMedicineId(),  // ID thuốc
                    detail.getQuantity(),    // Số lượng xuất
                    LocalDateTime.now(),     // Thời gian xuất
                    pharmacistId             // ID dược sĩ
            );
            try {
                if(exportDAO.exportAndDeductMedicine(exportInfo)) {
                    successCount++;
                }
            } catch (Exception e) {
                throw new BusinessException("Lỗi khi xuất thuốc ID " + detail.getMedicineId() + ": " + e.getMessage());
            }
        }
        // Bước 4: Trả về thông điệp tổng hợp cho UI
        if (missingMedicines.isEmpty()) {
            return "Đã xuất kho toàn bộ đơn thuốc thành công (" + successCount + " loại thuốc).";
        } else {
            return "Đã xuất kho một phần (" + successCount + "/" + details.size() + ").\nCó " + missingMedicines.size() + " loại thiếu tồn kho không thể xuất.";
        }
    }
    //Lấy lịch sử:
    public List<MedicineExport> getExportHistory(){
        return exportDAO.getExportHistory();
    }
}
