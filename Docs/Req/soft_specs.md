# **ĐẶC TẢ YÊU CẦU PHẦN MỀM**

## **HỆ THỐNG QUẢN LÝ BỆNH VIỆN (HMS)**

### **1.0. PHÂN HỆ QUẢN LÝ TIẾP ĐÓN (RECEPTION)**

#### **1.1. Đăng ký khám bệnh (Register Patient)**

* **Mục đích: Tiếp nhận yêu cầu khám, định danh bệnh nhân và cấp số thứ tự vào hàng chờ.**  
* **Dữ liệu đầu vào (Input):**  
  * ***Khách cũ:*** **Tìm kiếm bằng Mã bệnh nhân (PatientID), Số điện thoại, CCCD.**  
  * ***Khách mới:*** **Thông tin hành chính bắt buộc (Họ tên, Ngày sinh, Giới tính, SĐT, Địa chỉ).**  
  * ***Thông tin phiếu khám:*** **Loại hình khám (BHYT/Dịch vụ), Chuyên khoa (Nội, Ngoại, Nhi...), Lý do khám, Phòng khám mong muốn (hoặc tự động).**  
* **Quy trình xử lý (Process):**  
  * **Hệ thống kiểm tra trùng lặp thông tin bệnh nhân trong CSDL.**  
  * **Nếu bệnh nhân chưa có mã, hệ thống sinh `PatientID` duy nhất.**  
  * **Lọc danh sách phòng khám thuộc chuyên khoa đã chọn có trạng thái `Active`.**  
  * **Thuật toán cân bằng tải gợi ý phòng có lượng chờ ít nhất.**  
  * **Sinh số thứ tự (STT) theo quy tắc: `[Mã Phòng]-[STT Tăng dần trong ngày]`.**  
  * **Tạo bản ghi `PhieuKham` với trạng thái `PENDING`.**  
* **Dữ liệu đầu ra (Output):**  
  * **Phiếu khám bệnh (chứa thông tin bệnh nhân, số phòng, STT, giờ dự kiến).**  
  * **Cập nhật hàng chờ (Queue) của phòng khám.**

#### **1.2. Quản lý hồ sơ bệnh nhân (Patient Records Management)**

* **Mục đích: Lưu trữ, cập nhật và tra cứu lịch sử hành chính của người bệnh.**  
* **Dữ liệu đầu vào (Input): Thông tin cần cập nhật (SĐT mới, Địa chỉ mới...).**  
* **Quy trình xử lý (Process):**  
  * **Validate dữ liệu (định dạng SĐT, Email).**  
  * **Cho phép gộp hồ sơ (Merge) nếu phát hiện trùng lặp (tính năng nâng cao).**  
  * **Lưu lịch sử thay đổi thông tin (Audit Log).**  
* **Dữ liệu đầu ra (Output):**  
  * **Hồ sơ bệnh nhân được cập nhật.**  
  * **Lịch sử khám bệnh được hiển thị tập trung.**

#### **1.3. Kiểm tra thẻ Bảo hiểm Y tế (Check Insurance)**

* **Mục đích: Xác thực thẻ BHYT và xác định mức hưởng bảo hiểm.**  
* **Dữ liệu đầu vào (Input): Số thẻ BHYT, Họ tên, Mã KCBBD (Khám chữa bệnh ban đầu).**  
* **Quy trình xử lý (Process):**  
  * **Kiểm tra định dạng và hạn sử dụng thẻ (`ValidFrom`, `ValidTo`).**  
  * **So khớp thông tin nhân thân trên thẻ với thông tin hệ thống.**  
  * **Xác định tuyến khám:**  
    * **Đúng tuyến: Mã BV hiện tại \== Mã BV trên thẻ (Hưởng 80-100%).**  
    * **Trái tuyến: Hưởng theo quy định hiện hành (VD: 0% ngoại trú).**  
* **Dữ liệu đầu ra (Output):**  
  * **Tỷ lệ hưởng (BenefitRate) được lưu vào `PhieuKham`.**

#### **1.4. Phân luồng hoạt động (Queue Management)**

* **Mục đích: Điều phối bệnh nhân giữa các phòng khám để tránh ùn tắc.**  
* **Dữ liệu đầu vào (Input): Danh sách hàng chờ hiện tại, Trạng thái các phòng khám.**  
* **Quy trình xử lý (Process):**  
  * **Theo dõi số lượng chờ tại từng phòng theo thời gian thực.**  
  * **Cho phép nhân viên điều chuyển bệnh nhân từ phòng đông sang phòng vắng (cùng chuyên khoa).**  
  * **Xử lý ưu tiên (Người già, Trẻ em, Cấp cứu) \-\> Đẩy lên đầu hàng chờ.**  
* **Dữ liệu đầu ra (Output):**  
  * **Danh sách hàng chờ được cập nhật lại.**  
  * **Thông báo thay đổi phòng khám (trên bảng điện tử/loa).**

### **2.0. PHÂN HỆ QUẢN LÝ KHÁM CHỮA BỆNH (CLINICAL)**

#### **2.1. Khám lâm sàng (Clinical Examination)**

* **Mục đích: Bác sĩ thực hiện khám, chẩn đoán và ghi nhận thông tin bệnh án.**  
* **Dữ liệu đầu vào (Input): Chọn bệnh nhân từ hàng chờ, Chỉ số sinh tồn (Mạch, Nhiệt, Huyết áp), Triệu chứng, Mã bệnh (ICD-10).**  
* **Quy trình xử lý (Process):**  
  * **Chuyển trạng thái phiếu khám sang `IN_PROGRESS`.**  
  * **Truy xuất lịch sử khám cũ để tham khảo.**  
  * **Hỗ trợ tìm kiếm mã ICD-10 theo từ khóa tên bệnh.**  
* **Dữ liệu đầu ra (Output):**  
  * **Thông tin khám lâm sàng được lưu vào Bệnh án điện tử (EMR).**

#### **2.2. Chỉ định dịch vụ (Order Lab/Rad Tests)**

* **Mục đích: Yêu cầu thực hiện các xét nghiệm hoặc chẩn đoán hình ảnh.**  
* **Dữ liệu đầu vào (Input): Loại dịch vụ (Xét nghiệm máu, X-Quang, Siêu âm...), Số lượng, Ghi chú chỉ định.**  
* **Quy trình xử lý (Process):**  
  * **Tạo phiếu chỉ định dịch vụ (`ServiceOrder`).**  
  * **Tính tổng tiền tạm tính.**  
  * **Chuyển yêu cầu đến các khoa Cận lâm sàng tương ứng.**  
* **Dữ liệu đầu ra (Output):**  
  * **Phiếu chỉ định (trạng thái `ORDERED`).**

#### **2.3. Nhập kết quả cận lâm sàng (Lab/Rad Results Entry)**

* **Mục đích: Ghi nhận kết quả từ phòng xét nghiệm/chẩn đoán hình ảnh.**  
* **Dữ liệu đầu vào (Input): Mã phiếu chỉ định, Các chỉ số kết quả (Kết quả xét nghiệm), File ảnh (X-Quang/Siêu âm), Kết luận của KTV.**  
* **Quy trình xử lý (Process):**  
  * **Validate các chỉ số (nếu nằm ngoài ngưỡng bình thường \-\> cảnh báo).**  
  * **Upload và lưu trữ file ảnh đính kèm.**  
  * **Cập nhật trạng thái phiếu chỉ định \-\> `COMPLETED`.**  
* **Dữ liệu đầu ra (Output):**  
  * **Kết quả hiển thị ngay lập tức trên màn hình của Bác sĩ khám.**

#### **2.4. Kê đơn thuốc (E-Prescription)**

* **Mục đích: Chỉ định thuốc điều trị.**  
* **Dữ liệu đầu vào (Input): Tên thuốc, Số lượng, Liều dùng, Cách dùng.**  
* **Quy trình xử lý (Process):**  
  * **Check tồn kho khả dụng (`AvailableStock`) theo thời gian thực. Nếu thiếu \-\> Chặn kê đơn.**  
  * **Kiểm tra tương tác thuốc (nếu có module Dược lâm sàng).**  
  * **Lưu đơn thuốc vào hồ sơ.**  
* **Dữ liệu đầu ra (Output):**  
  * **Đơn thuốc điện tử (gửi sang Dược).**

#### **2.5. Kết luận khám (Diagnosis Conclusion)**

* **Mục đích: Đưa ra quyết định điều trị cuối cùng.**  
* **Dữ liệu đầu vào (Input): Mã chẩn đoán xác định, Hướng xử lý (Cho về / Nhập viện / Chuyển viện), Lời dặn dò, Hẹn tái khám.**  
* **Quy trình xử lý (Process):**  
  * **Khóa hồ sơ bệnh án của lượt khám này.**  
  * **Nếu nhập viện \-\> Tạo hồ sơ bệnh án nội trú.**  
  * **Nếu chuyển viện \-\> In giấy chuyển tuyến.**  
* **Dữ liệu đầu ra (Output):**  
  * **Hồ sơ khám hoàn tất (Trạng thái `FINISHED`).**  
  * **Giấy ra viện / Giấy hẹn khám lại.**

### **3.0. PHÂN HỆ QUẢN LÝ DƯỢC & VẬT TƯ (PHARMACY & INVENTORY)**

#### **3.1. Quản lý danh mục thuốc (Drug Master Data)**

* **Mục đích: Quản lý thông tin gốc của các loại thuốc/vật tư.**  
* **Dữ liệu đầu vào (Input): Tên thuốc, Hoạt chất, Hàm lượng, Đơn vị tính, Giá nhập, Giá bán, Quy cách đóng gói, Hạn mức tồn tối thiểu/tối đa.**  
* **Quy trình xử lý (Process):**  
  * **Thêm mới/Cập nhật thông tin thuốc.**  
  * **Phân loại nhóm thuốc (Kháng sinh, Giảm đau...).**  
* **Dữ liệu đầu ra (Output):**  
  * **Danh mục thuốc dùng chung cho toàn hệ thống.**

#### **3.2. Nhập kho (Import Stock)**

* **Mục đích: Nhập hàng từ nhà cung cấp vào kho.**  
* **Dữ liệu đầu vào (Input): Thông tin hóa đơn, Nhà cung cấp, Chi tiết lô (Số lô, Hạn dùng), Số lượng, Đơn giá.**  
* **Quy trình xử lý (Process):**  
  * **Tạo phiếu nhập.**  
  * **Cộng số lượng vào kho tổng: `TonMoi = TonCu + SL_Nhap`.**  
  * **Quản lý theo Lô và Hạn sử dụng (Batch Management).**  
* **Dữ liệu đầu ra (Output):**  
  * **Phiếu nhập kho.**

#### **3.3. Duyệt/Phát thuốc (Dispense Medicine)**

* **Mục đích: Xuất thuốc cho bệnh nhân ngoại trú.**  
* **Dữ liệu đầu vào (Input): Mã đơn thuốc.**  
* **Quy trình xử lý (Process):**  
  * **Dược sĩ kiểm tra đơn thuốc trên hệ thống.**  
  * **Soạn thuốc theo chỉ định.**  
  * **Xác nhận phát thuốc \-\> Hệ thống trừ kho: `TonMoi = TonCu - SL_Xuat`.**  
  * **Ưu tiên trừ lô có hạn sử dụng gần nhất (FEFO \- First Expired First Out).**  
* **Dữ liệu đầu ra (Output):**  
  * **Phiếu xuất kho kiêm hướng dẫn sử dụng.**

#### **3.4. Kiểm kê kho (Inventory Check)**

* **Mục đích: Đối chiếu số liệu phần mềm và thực tế.**  
* **Dữ liệu đầu vào (Input): Số lượng thực tế đếm được.**  
* **Quy trình xử lý (Process):**  
  * **So sánh `SL_ThucTe` và `SL_HeThong`.**  
  * **Ghi nhận chênh lệch (Thừa/Thiếu).**  
  * **Tạo phiếu cân bằng kho (Adjustment) để điều chỉnh số liệu hệ thống về đúng thực tế.**  
* **Dữ liệu đầu ra (Output):**  
  * **Biên bản kiểm kê.**

#### **3.5. Quản lý trang thiết bị (Medical Equipment Management)**

* **Mục đích: Theo dõi tình trạng máy móc, thiết bị y tế.**  
* **Dữ liệu đầu vào (Input): Tên thiết bị, Mã tài sản, Ngày nhập, Tình trạng (Tốt/Hỏng/Đang bảo trì).**  
* **Quy trình xử lý (Process):**  
  * **Cập nhật trạng thái hoạt động.**  
  * **Lên lịch bảo trì định kỳ.**  
* **Dữ liệu đầu ra (Output):**  
  * **Danh sách thiết bị khả dụng cho các phòng khám/CLS.**

### **4.0. PHÂN HỆ QUẢN LÝ TÀI CHÍNH & VIỆN PHÍ (BILLING)**

#### **4.1. Quản lý bảng giá dịch vụ (Service Price List)**

* **Mục đích: Định nghĩa giá cho các dịch vụ kỹ thuật và thuốc.**  
* **Dữ liệu đầu vào (Input): Tên dịch vụ, Giá BHYT, Giá Dịch vụ (Yêu cầu), Giá Ngoài giờ.**  
* **Quy trình xử lý (Process):**  
  * **Thiết lập hiệu lực của giá (Áp dụng từ ngày...).**  
  * **Cập nhật giá mới vào hệ thống tính tiền.**  
* **Dữ liệu đầu ra (Output):**  
  * **Bảng giá niêm yết.**

#### **4.2. Lập hóa đơn (Billing Calculation)**

* **Mục đích: Tổng hợp chi phí.**  
* **Quy trình xử lý (Process):**  
  * **Quét toàn bộ dịch vụ/thuốc bệnh nhân đã sử dụng trong đợt khám.**  
  * **Tính toán chi phí:**  
    * **`TongTien` \= (SL \* DonGia).**  
    * **`BHYT_Tra` \= TongTien \* %Huong (theo logic đúng/trái tuyến).**  
    * **`BenhNhan_Tra` \= TongTien \- BHYT\_Tra.**  
* **Dữ liệu đầu ra (Output):**  
  * **Bảng kê chi phí chi tiết.**

#### **4.3. Thu ngân (Payment Collection)**

* **Mục đích: Thực hiện thu tiền.**  
* **Dữ liệu đầu vào (Input): Mã phiếu khám/Mã bệnh nhân, Số tiền khách trả, Phương thức thanh toán (Tiền mặt/Chuyển khoản/Thẻ).**  
* **Quy trình xử lý (Process):**  
  * **Xác nhận đã nhận đủ tiền.**  
  * **Ghi nhận doanh thu.**  
  * **Chuyển trạng thái phiếu khám \-\> `PAID` (Đã thanh toán) \-\> Mở khóa quy trình tiếp theo (VD: Lấy thuốc).**  
* **Dữ liệu đầu ra (Output):**  
  * **Hóa đơn tài chính (E-Invoice) hoặc Biên lai thu tiền.**

### **5.0. PHÂN HỆ QUẢN LÝ THỐNG KÊ & BÁO CÁO (REPORTING)**

#### **5.1. Thống kê doanh thu (Revenue Report)**

* **Mục đích: Theo dõi tình hình tài chính.**  
* **Dữ liệu đầu vào (Input): Khoảng thời gian (Từ ngày \- Đến ngày), Loại doanh thu (Khám/Thuốc/CLS).**  
* **Quy trình xử lý (Process):**  
  * **Truy vấn bảng `HoaDon`.**  
  * **Tổng hợp (`SUM`) doanh thu theo tiêu chí lọc.**  
* **Dữ liệu đầu ra (Output):**  
  * **Biểu đồ doanh thu hoặc Bảng số liệu chi tiết.**

#### **5.2. Thống kê bệnh phổ biến (Disease Statistics)**

* **Mục đích: Phục vụ nghiên cứu dịch tễ và báo cáo Sở Y tế.**  
* **Dữ liệu đầu vào (Input): Khoảng thời gian.**  
* **Quy trình xử lý (Process):**  
  * **Truy vấn bảng `HoSoBenhAn`.**  
  * **Đếm số lượng (`COUNT`) theo từng mã bệnh ICD-10.**  
  * **Sắp xếp giảm dần để tìm Top bệnh.**  
* **Dữ liệu đầu ra (Output):**  
  * **Danh sách Top 10 bệnh thường gặp.**

#### **5.3. Báo cáo tài chính (Financial Report)**

* **Mục đích: Tổng hợp thu chi, công nợ.**  
* **Dữ liệu đầu vào (Input): Kỳ báo cáo (Tháng/Quý/Năm).**  
* **Quy trình xử lý (Process):**  
  * **Tổng hợp Doanh thu thực thu.**  
  * **Tổng hợp Giá vốn hàng bán (Thuốc/Vật tư đã xuất).**  
  * **Tính toán Lãi gộp.**  
* **Dữ liệu đầu ra (Output):**  
  * **Báo cáo kết quả hoạt động kinh doanh sơ bộ.**

### **6.0. PHÂN HỆ QUẢN LÝ NHÂN SỰ (HRM)**

#### **6.1. Phân ca trực/Lịch làm việc (Shift Scheduling)**

* **Mục đích: Đảm bảo nhân sự cho các vị trí trực.**  
* **Dữ liệu đầu vào (Input): Danh sách nhân viên, Ca trực (Sáng/Chiều/Đêm), Phòng ban.**  
* **Quy trình xử lý (Process):**  
  * **Gán nhân viên vào các slot trực trên lịch.**  
  * **Kiểm tra quy tắc (VD: Không trực 2 ca liên tiếp, tổng giờ làm không quá quy định).**  
* **Dữ liệu đầu ra (Output):**  
  * **Lịch trực tuần/tháng.**

#### **6.2. Quản lý hồ sơ nhân viên (Employee Profile)**

* **Mục đích: Lưu trữ thông tin nhân sự.**  
* **Dữ liệu đầu vào (Input): Thông tin cá nhân, Bằng cấp, Chứng chỉ hành nghề, Hợp đồng lao động.**  
* **Quy trình xử lý (Process):**  
  * **Thêm/Sửa/Xóa (Khóa) nhân viên.**  
  * **Quản lý quá trình công tác.**  
* **Dữ liệu đầu ra (Output):**  
  * **Danh sách nhân sự hiện hành.**

#### **6.3. Tính lương (Payroll)**

* **Mục đích: Tính toán lương thưởng cuối tháng.**  
* **Dữ liệu đầu vào (Input): Bảng chấm công (số ca làm việc), Hệ số lương, Phụ cấp (trực đêm, độc hại, phẫu thuật).**  
* **Quy trình xử lý (Process):**  
  * **`LuongCoBan` \= NgayCong \* HeSo.**  
  * **`TongLuong` \= LuongCoBan \+ PhuCap \- BaoHiem \- ThueTNCN.**  
* **Dữ liệu đầu ra (Output):**  
  * **Bảng lương chi tiết (Payslip).**

### **7.0. PHÂN HỆ QUẢN TRỊ HỆ THỐNG (ADMINISTRATION)**

#### **7.1. Quản lý tài khoản (Account Management)**

* **Mục đích: Cấp quyền truy cập hệ thống cho nhân viên.**  
* **Dữ liệu đầu vào (Input): Tên đăng nhập, Mật khẩu (hash), Nhân viên sở hữu.**  
* **Quy trình xử lý (Process):**  
  * **Tạo tài khoản mới liên kết với `NhanVienID`.**  
  * **Kích hoạt hoặc Vô hiệu hóa tài khoản.**  
* **Dữ liệu đầu ra (Output):**  
  * **Tài khoản người dùng.**

#### **7.2. Quản lý nhóm quyền (Role Management)**

* **Mục đích: Phân quyền chức năng (RBAC).**  
* **Dữ liệu đầu vào (Input): Tên nhóm (Role), Danh sách quyền (Permission).**  
* **Quy trình xử lý (Process):**  
  * **Định nghĩa Role (VD: Admin, Doctor, Receptionist).**  
  * **Gán quyền truy cập menu/chức năng cho từng Role.**  
* **Dữ liệu đầu ra (Output):**  
  * **Ma trận phân quyền.**

#### **7.3. Đăng nhập/Đăng xuất (Authentication)**

* **Mục đích: Bảo mật truy cập.**  
* **Dữ liệu đầu vào (Input): Username, Password.**  
* **Quy trình xử lý (Process):**  
  * **Mã hóa Password nhập vào và so sánh với Password trong DB.**  
  * **Nếu đúng \-\> Cấp Token/Session \-\> Chuyển vào trang chủ tương ứng với Role.**  
  * **Ghi log đăng nhập.**  
* **Dữ liệu đầu ra (Output):**  
  * **Phiên làm việc (Session).**

#### **7.4. Đổi mật khẩu (Change Password)**

* **Mục đích: Bảo vệ tài khoản cá nhân.**  
* **Dữ liệu đầu vào (Input): Mật khẩu cũ, Mật khẩu mới, Xác nhận mật khẩu mới.**  
* **Quy trình xử lý (Process):**  
  * **Xác thực mật khẩu cũ.**  
  * **Kiểm tra độ mạnh mật khẩu mới (Độ dài, ký tự đặc biệt...).**  
  * **Cập nhật mã băm (Hash) mới vào DB.**  
* **Dữ liệu đầu ra (Output):**  
  * **Thông báo thành công.**

#### **7.5. Sao lưu/Khôi phục dữ liệu (Backup & Restore)**

* **Mục đích: Đảm bảo an toàn dữ liệu.**  
* **Dữ liệu đầu vào (Input): Lịch sao lưu (Schedule), File sao lưu (cho việc khôi phục).**  
* **Quy trình xử lý (Process):**  
  * **Backup: Dump toàn bộ Database ra file `.sql` hoặc `.bak` theo lịch định kỳ (VD: 2h sáng hàng ngày).**  
  * **Restore: Đọc file backup và ghi đè lại dữ liệu vào Database khi có sự cố.**  
* **Dữ liệu đầu ra (Output):**  
  * **File Backup.**  
  * **Trạng thái phục hồi hệ thống.**

