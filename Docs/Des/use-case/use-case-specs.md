# **ĐẶC TẢ USE CASE CHI TIẾT \- HỆ THỐNG QUẢN LÝ BỆNH VIỆN (HMS)**

## **1\. PHÂN HỆ TIẾP ĐÓN (RECEPTION)**

### **UC-1.1: Đăng ký khám bệnh**

* **Tác nhân:** Lễ tân  
* **Mục đích:** Tiếp nhận bệnh nhân vào hệ thống, định danh chính xác hồ sơ y tế và phân bổ bệnh nhân vào phòng khám phù hợp nhất nhằm tối ưu hóa thời gian chờ đợi.  
* **Tiền điều kiện:**  
  * Lễ tân đã đăng nhập thành công vào hệ thống.  
  * Danh mục phòng khám và bác sĩ trực trong ngày đã được cấu hình sẵn sàng.  
* **Luồng sự kiện chính (Main Flow):**  
  * Lễ tân chọn chức năng "Đăng ký khám" từ Dashboard chính.  
  * Hệ thống hiển thị giao diện tìm kiếm bệnh nhân. Lễ tân yêu cầu bệnh nhân cung cấp thông tin định danh (SĐT, CCCD, Mã BHYT hoặc Mã BN cũ).  
  * Lễ tân nhập từ khóa và nhấn "Tìm kiếm". Hệ thống thực hiện tìm kiếm chính xác và tìm kiếm gần đúng (fuzzy search) để đảm bảo không bỏ sót hồ sơ.  
  * Hệ thống hiển thị kết quả:  
    * **Trường hợp tìm thấy:** Hiển thị tóm tắt thông tin hành chính (Họ tên, Tuổi, Giới tính, Lịch sử khám gần nhất) để Lễ tân xác nhận với khách. Nếu thông tin có thay đổi (vd: đổi địa chỉ, SĐT), Lễ tân cập nhật ngay tại bước này.  
    * **Trường hợp không tìm thấy:** Lễ tân chọn "Thêm mới bệnh nhân". Hệ thống mở form nhập liệu yêu cầu các trường bắt buộc: Họ tên, Ngày sinh (hoặc Năm sinh), Giới tính, SĐT, Địa chỉ, Thông tin người thân (nếu là Nhi/Cấp cứu).  
  * Lễ tân nhập thông tin phiếu khám chi tiết:  
    * **Loại hình:** Chọn BHYT, Thu phí dịch vụ, hoặc Khám sức khỏe định kỳ.  
    * **Chuyên khoa:** Chọn chuyên khoa dựa trên nhu cầu của bệnh nhân (Nội, Ngoại, Sản, Nhi...).  
    * **Lý do khám:** Ghi nhận triệu chứng ban đầu (vd: Đau bụng, Sốt cao).  
    * **Yêu cầu đặc biệt:** Chọn bác sĩ chỉ định hoặc phòng khám VIP (nếu có).  
  * Hệ thống thực hiện thuật toán phân luồng thông minh:  
    * Lọc danh sách các phòng khám thuộc chuyên khoa đã chọn đang có trạng thái `Active` (Bác sĩ đang trực).  
    * Tính toán tải hiện tại của từng phòng (Số người đang chờ \+ Thời gian khám trung bình dự kiến).  
    * Gợi ý phòng khám tối ưu nhất (ít chờ nhất) hoặc cho phép Lễ tân chọn thủ công theo nguyện vọng khách hàng.  
  * Lễ tân xác nhận chọn phòng và bấm "Đăng ký".  
  * Hệ thống xử lý dữ liệu nền:  
    * Sinh số thứ tự (STT) khám theo quy tắc: `[Mã Phòng]-[STT Tăng dần trong phiên]`.  
    * Tạo bản ghi `PhieuKham` mới với trạng thái `PENDING`.  
    * Cập nhật hàng chờ ảo (Virtual Queue) của phòng khám đích.  
  * Hệ thống tự động in "Phiếu khám bệnh" (bao gồm mã vạch/QR Code để check-in tại phòng khám) và hiển thị thông báo thành công.  
* **Luồng ngoại lệ (Alternative Flow):**  
  * *Tại bước 4 (Thêm mới \- Trùng lặp tiềm ẩn):* Nếu hệ thống phát hiện SĐT hoặc số CCCD trùng với một hồ sơ đã tồn tại (có thể do nhập liệu sai trước đó), hệ thống sẽ hiển thị cảnh báo "Hồ sơ có thể đã tồn tại" và yêu cầu Lễ tân sử dụng chức năng "Gộp hồ sơ" (Merge Patient) hoặc xác nhận đây là hai người khác nhau.  
  * *Tại bước 6 (Hết phòng khám):* Nếu không có phòng khám nào thuộc chuyên khoa đó hoạt động (ngoài giờ hành chính hoặc bác sĩ nghỉ đột xuất), hệ thống báo lỗi "Không có phòng khám khả dụng" và gợi ý chuyển sang Khoa Cấp cứu hoặc Hẹn lịch khám vào ngày khác.  
  * *Xử lý Ưu tiên (Priority Handling):* Nếu bệnh nhân thuộc diện Ưu tiên (Người già \> 80 tuổi, Trẻ em \< 6 tuổi, Thương binh), Lễ tân tích chọn checkbox "Ưu tiên". Hệ thống sẽ cấp số STT đặc biệt và đẩy bệnh nhân lên đầu hàng chờ (nhưng sau các ca Cấp cứu).

### **UC-1.3: Kiểm tra thẻ BHYT**

* **Tác nhân:** Lễ tân  
* **Mục đích:** Xác thực tính hợp lệ của thẻ BHYT thông qua cổng thông tin giám định, xác định mức hưởng (Đúng tuyến/Trái tuyến) để đảm bảo quyền lợi cho bệnh nhân và tránh xuất toán cho bệnh viện.  
* **Tiền điều kiện:** Đang trong quy trình "Đăng ký khám" và Loại hình khám là "BHYT".  
* **Luồng sự kiện chính:**  
  * Lễ tân chọn tab "Thông tin BHYT".  
  * Lễ tân nhập Số thẻ BHYT, Họ tên, Ngày sinh và Mã KCB Ban đầu (hoặc sử dụng đầu đọc thẻ chip/QR Code trên ứng dụng VssID để tự động điền).  
  * Hệ thống gửi yêu cầu xác thực (API Call) đến Cổng giám định BHYT Quốc gia (hoặc Database nội bộ nếu mất kết nối mạng \- Chế độ Offline).  
  * Hệ thống kiểm tra các điều kiện logic:  
    * **Hạn sử dụng:** Kiểm tra ngày hiện tại có nằm trong khoảng `ValidFrom` và `ValidTo` không.  
    * **Thời điểm đủ 5 năm liên tục:** Để xác định quyền lợi chi trả cao.  
    * **Lịch sử khám:** Kiểm tra xem bệnh nhân có đang điều trị nội trú ở viện khác hoặc đã khám tại nơi khác trong cùng ngày hay không.  
  * Hệ thống xác định tuyến khám và tỷ lệ hưởng:  
    * **Đúng tuyến:** Mã BV hiện tại trùng Mã KCB ban đầu HOẶC Có giấy chuyển tuyến hợp lệ. Mức hưởng: 80%, 95% hoặc 100% tùy theo mã đối tượng (DN, HT, TE...).  
    * **Trái tuyến:** Không có giấy chuyển tuyến. Mức hưởng: Theo quy định hiện hành (Ví dụ: 0% ngoại trú, 40% hoặc 100% nội trú tùy hạng bệnh viện).  
  * Hệ thống hiển thị kết quả kiểm tra: Mức hưởng (`BenefitRate`), Tuyến khám (`Route`), và thông báo về Giấy chuyển tuyến (nếu cần).  
  * Lễ tân nhập thông tin Giấy chuyển tuyến (Số giấy, Nơi chuyển, Ngày chuyển) nếu là ca chuyển viện đến.  
  * Hệ thống lưu toàn bộ thông tin BHYT vào phiên đăng ký hiện tại để phục vụ tính toán viện phí sau này.  
* **Luồng ngoại lệ:**  
  * *Thẻ hết hạn hoặc bị báo mất:* Hệ thống báo lỗi đỏ, hiển thị lý do từ cổng giám định. Hệ thống tự động chuyển loại hình khám về "Dịch vụ" (Bệnh nhân tự chi trả 100%).  
  * *Thông tin không khớp:* Nếu Họ tên/Ngày sinh trên thẻ không khớp với dữ liệu nhân thân trong hệ thống, hệ thống cảnh báo và yêu cầu Lễ tân đối chiếu giấy tờ tùy thân (CCCD) để sửa lại dữ liệu gốc cho khớp trước khi tiếp tục.  
  * *Mất kết nối Cổng giám định:* Hệ thống cho phép "Ghi nhận tạm thời" để bệnh nhân được khám trước, nhưng cảnh báo Lễ tân cần thực hiện tra cứu lại trước khi thanh toán ra viện.

## **2\. PHÂN HỆ KHÁM LÂM SÀNG (CLINICAL)**

### **UC-2.1: Khám lâm sàng**

* **Tác nhân:** Bác sĩ  
* **Mục đích:** Bác sĩ thực hiện quy trình chuyên môn: hỏi bệnh, khám thực thể, đánh giá các chỉ số sinh tồn và đưa ra chẩn đoán sơ bộ. Đây là bước khởi đầu quan trọng cho luồng điều trị.  
* **Tiền điều kiện:** Bệnh nhân đã check-in và đang ở trạng thái `WAITING` tại phòng khám của bác sĩ.  
* **Luồng sự kiện chính:**  
  * Bác sĩ xem danh sách hàng chờ, chọn bệnh nhân tiếp theo (ưu tiên ca `Emergency` hoặc `Priority`) và nhấn "Gọi khám". Hệ thống cập nhật trạng thái hiển thị trên bảng điện tử bên ngoài.  
  * Khi bệnh nhân vào, Bác sĩ nhấn "Bắt đầu khám", chuyển trạng thái phiếu sang `IN_PROGRESS`.  
  * Hệ thống hiển thị **Dashboard Bệnh nhân 360 độ**, bao gồm: Thông tin hành chính, Tiền sử dị ứng (Cảnh báo đỏ nếu có), Lịch sử các lần khám trước, và Kết quả xét nghiệm cũ.  
  * Bác sĩ (hoặc Điều dưỡng) nhập các **Chỉ số sinh tồn (Vitals)**: Mạch, Nhiệt độ, Huyết áp, Nhịp thở, Cân nặng, Chiều cao. Hệ thống tự động tính chỉ số BMI.  
  * Bác sĩ nhập thông tin khám bệnh:  
    * **Lý do đến khám & Bệnh sử:** Diễn biến bệnh.  
    * **Khám thực thể:** Mô tả các bộ phận (Tim, Phổi, Bụng...).  
    * **Tiền sử:** Bản thân và Gia đình.  
  * Bác sĩ đưa ra **Chẩn đoán sơ bộ**:  
    * Nhập mã bệnh ICD-10 hoặc tên bệnh (Hệ thống gợi ý danh sách bệnh thường gặp theo chuyên khoa).  
    * Nhập chẩn đoán kèm theo (nếu có).  
    * Nhập chẩn đoán phân biệt (dạng văn bản tự do).  
  * Hệ thống tự động lưu nháp định kỳ (Auto-save) để tránh mất dữ liệu. Sau khi nhập xong, Bác sĩ nhấn "Lưu" để cập nhật vào Bệnh án điện tử (EMR).  
* **Luồng ngoại lệ:**  
  * *Cảnh báo chỉ số sinh tồn:* Nếu nhập Huyết áp \> 180mmHg hoặc Nhiệt độ \> 40 độ C, hệ thống hiển thị cảnh báo **"Chỉ số nguy hiểm"** và gợi ý xử lý cấp cứu ngay lập tức.  
  * *Dị ứng thuốc:* Nếu bệnh nhân khai báo tiền sử dị ứng mới, Bác sĩ cập nhật ngay vào hồ sơ gốc. Hệ thống sẽ dùng dữ liệu này để chặn kê đơn thuốc dị ứng ở các bước sau.

### **UC-2.2: Chỉ định dịch vụ (CLS)**

* **Tác nhân:** Bác sĩ  
* **Mục đích:** Yêu cầu các bộ phận hỗ trợ thực hiện xét nghiệm (Lab) hoặc chẩn đoán hình ảnh (Radiology) để củng cố chẩn đoán lâm sàng.  
* **Tiền điều kiện:** Đang trong phiên khám bệnh (UC-2.1).  
* **Luồng sự kiện chính:**  
  1. Bác sĩ chuyển sang tab "Chỉ định cận lâm sàng".  
  2. Hệ thống hiển thị danh mục dịch vụ được phân nhóm (Huyết học, Sinh hóa, X-Quang, Siêu âm, CT-Scanner...).  
  3. Bác sĩ tìm kiếm dịch vụ theo tên hoặc mã, hoặc chọn từ các "Gói chỉ định mẫu" (Combo) đã tạo sẵn (ví dụ: Gói Tiền phẫu, Gói Khám tổng quát).  
  4. Bác sĩ chọn dịch vụ, nhập số lượng và **Ghi chú lâm sàng** (bắt buộc đối với các dịch vụ chẩn đoán hình ảnh để KTV biết vùng cần khảo sát).  
  5. Hệ thống tính toán sơ bộ tổng tiền:  
     * Kiểm tra quy tắc BHYT: Dịch vụ này có được BHYT chi trả cho chẩn đoán ICD-10 hiện tại không? Nếu không, hệ thống đánh dấu "Tự túc".  
     * Kiểm tra số dư tài khoản tạm ứng (đối với bệnh nhân nội trú).  
  6. Bác sĩ nhấn "Gửi chỉ định".  
  7. Hệ thống tách phiếu chỉ định và gửi lệnh đi các khoa phòng tương ứng:  
     * Phiếu Xét nghiệm \-\> Hệ thống LIS (Lab Information System).  
     * Phiếu Chụp chiếu \-\> Hệ thống PACS/RIS (Radiology Information System).  
  8. Trạng thái phiếu chỉ định chuyển thành `ORDERED`.

### **UC-2.4: Kê đơn thuốc**

* **Tác nhân:** Bác sĩ  
* **Mục đích:** Chỉ định thuốc điều trị ngoại trú, đảm bảo an toàn, hiệu quả và kinh tế. Hệ thống hỗ trợ tối đa việc kiểm tra tương tác và tồn kho.  
* **Tiền điều kiện:** Đang trong phiên khám bệnh.  
* **Luồng sự kiện chính:**  
  * Bác sĩ chọn tab "Kê đơn thuốc".  
  * Bác sĩ tìm kiếm thuốc (theo Tên thương mại hoặc Hoạt chất). Hệ thống hiển thị danh sách thuốc kèm các thông tin quan trọng: **Đơn vị tính, Giá bán, Tồn kho khả dụng, Hạn sử dụng gần nhất**.  
  * Bác sĩ chọn thuốc và điền chi tiết:  
    * Số lượng (viên/vỉ/chai).  
    * Liều dùng (Sáng/Trưa/Chiều/Tối).  
    * Cách dùng (Uống sau ăn, Uống trước ăn...).  
    * Số ngày thuốc.  
  * **Kiểm tra Tương tác thuốc & Dị ứng (Clinical Decision Support):**  
    * Ngay khi chọn thuốc, hệ thống kiểm tra chéo với danh sách thuốc đã kê trong đơn và Tiền sử dị ứng của bệnh nhân.  
    * Nếu phát hiện tương tác (Mức độ Trung bình/Nghiêm trọng), hệ thống hiện Popup cảnh báo (vd: "Thuốc A tương tác với Thuốc B gây giảm tác dụng").  
  * **Kiểm tra Tồn kho:**  
    * Hệ thống kiểm tra: Nếu `Số lượng kê` \<= `Tồn kho khả dụng` \-\> Cho phép.  
  * Bác sĩ xem lại đơn thuốc tổng thể và nhấn "Lưu & Phát hành".  
  * Hệ thống thực hiện:  
    * Trừ tồn kho tạm thời (Hard allocation) để giữ thuốc cho bệnh nhân này.  
    * Gửi dữ liệu đơn thuốc sang Phân hệ Dược.  
* **Luồng ngoại lệ:**  
  * *Hết thuốc:* Nếu thuốc hết hàng (`Stock` \= 0\) hoặc không đủ số lượng, hệ thống gợi ý **"Thuốc thay thế"** (cùng hoạt chất, cùng hàm lượng) đang có sẵn trong kho.  
  * *Vượt trần BHYT:* Nếu tổng giá trị đơn thuốc vượt quá quy định chi trả của BHYT cho 1 lần khám, hệ thống cảnh báo để bác sĩ cân nhắc cắt giảm hoặc chuyển sang thuốc tự nguyện.

### **UC-2.5: Kết luận khám**

* **Tác nhân:** Bác sĩ  
* **Luồng sự kiện chính:**  
  1. Sau khi có đủ kết quả CLS (nếu có), Bác sĩ đưa ra hướng xử lý cuối cùng:  
     * **Điều trị ngoại trú (Cho về):** Kê đơn thuốc về nhà.  
     * **Điều trị nội trú (Nhập viện):** Tạo phiếu Vào viện.  
     * **Chuyển tuyến:** Tạo giấy Chuyển viện lên tuyến trên.  
  2. Bác sĩ nhập "Lời dặn của bác sĩ" (Chế độ ăn uống, sinh hoạt).  
  3. Bác sĩ thiết lập lịch "Hẹn tái khám" (nếu cần): Chọn ngày, Hệ thống tự động tính ngày dựa trên số ngày thuốc đã kê.  
  4. Bác sĩ nhấn "Kết thúc khám".  
  5. Hệ thống thực hiện quy trình đóng hồ sơ:  
     * Chuyển trạng thái phiếu khám sang `FINISHED`.  
     * Khóa quyền chỉnh sửa (Read-only) đối với bệnh án này.  
     * Gửi lệnh thanh toán xuống bộ phận Thu ngân (nếu còn khoản chưa thu).  
     * In các giấy tờ: Đơn thuốc, Phiếu kết quả khám, Giấy nghỉ ốm hưởng BHXH (nếu có).

## **3\. PHÂN HỆ DƯỢC (PHARMACY)**

### **UC-3.2: Nhập kho thuốc**

* **Tác nhân:** Thủ kho  
* **Luồng sự kiện chính:**  
  1. Thủ kho chọn chức năng "Nhập kho từ Nhà cung cấp".  
  2. Nhập thông tin hóa đơn chứng từ: Chọn Nhà cung cấp, Số hóa đơn đỏ, Ngày hóa đơn, Ngày nhập kho, VAT.  
  3. Nhập chi tiết danh mục thuốc nhập:  
     * Chọn mã thuốc từ danh mục gốc.  
     * **Quản lý Lô/Date:** Bắt buộc nhập **Số lô (Batch Number)** và **Hạn sử dụng (Expiry Date)**. Đây là thông tin tối quan trọng để quản lý chất lượng.  
     * Nhập Số lượng, Đơn giá nhập trước thuế.  
  4. Hệ thống tự động tính toán:  
     * `Thành tiền` \= Số lượng \* Đơn giá.  
     * `Giá vốn` (Moving Average hoặc FIFO tùy cấu hình).  
     * `Giá bán dự kiến` \= Giá nhập \* (1 \+ Tỷ lệ thặng số quy định).  
  5. Thủ kho kiểm tra tổng tiền hóa đơn so với thực tế.  
  6. Nhấn "Lưu tạm" hoặc "Nhập kho".  
  7. Khi nhấn "Nhập kho":  
     * Hệ thống tăng số lượng tồn kho tổng.  
     * Hệ thống tạo các bản ghi chi tiết tồn kho theo Lô (`Stock_Lot`).  
     * Hệ thống cập nhật giá vốn mới cho mặt hàng.

### **UC-3.3: Duyệt & Phát thuốc**

* **Tác nhân:** Dược sĩ  
* **Tiền điều kiện:** Đơn thuốc đã được bác sĩ kê và Bệnh nhân đã hoàn tất nghĩa vụ tài chính (Trạng thái `PAID` hoặc `BHYT Approved`).  
* **Luồng sự kiện chính:**  
  * Dược sĩ nhận đơn thuốc (trực tuyến trên màn hình chờ cấp phát).  
  * Dược sĩ gọi tên bệnh nhân, quét mã vạch trên phiếu chỉ định/đơn thuốc hoặc tìm theo Mã BN.  
  * Hệ thống hiển thị chi tiết đơn thuốc cần soạn: Tên thuốc, Hàm lượng, Số lượng, Cách dùng.  
  * Dược sĩ đi lấy thuốc từ kệ. Lúc này hệ thống hỗ trợ chỉ dẫn vị trí kệ (Shelf Location) của lô thuốc cần lấy.  
  * Dược sĩ thực hiện "Kiểm tra 3 chiếu 5 đối" (quy tắc an toàn ngành dược).  
  * Dược sĩ nhấn "Xác nhận phát thuốc".  
  * Hệ thống thực hiện thuật toán trừ kho chi tiết (Inventory Deduction Logic):  
    * Tìm các lô thuốc (Lot) của mặt hàng đó đang có `SoLuong > 0`.  
    * Sắp xếp các lô theo thứ tự **Hạn dùng gần nhất (FEFO \- First Expired First Out)**.  
    * Trừ lần lượt từ lô cận date nhất cho đến khi đủ số lượng yêu cầu.  
    * Cập nhật `Tồn kho khả dụng` và `Tồn kho thực tế`.  
  * Hệ thống in "Nhãn hướng dẫn sử dụng" để dán lên vỏ hộp thuốc (Tên BN, Cách dùng sáng/chiều/tối).  
* **Luồng ngoại lệ:**  
  * *Phát hiện sai sót/Hết lô:* Nếu lô thuốc trên hệ thống gợi ý không tìm thấy trong thực tế (do lệch kho), Dược sĩ có quyền **"Từ chối phát dòng đó"** và chọn lô khác hoặc tạo "Yêu cầu kiểm kê đột xuất". Hệ thống sẽ đánh dấu dòng thuốc đó là "Chưa phát" để xử lý sau hoặc hoàn tiền.  
  * *Chưa thanh toán:* Nếu đơn thuốc chưa được thanh toán, hệ thống khóa nút "Xác nhận phát" và hiển thị cảnh báo "Vui lòng yêu cầu bệnh nhân đóng viện phí".

## **4\. PHÂN HỆ TÀI CHÍNH (BILLING)**

### **UC-4.2: Lập hóa đơn & Tính phí**

* **Tác nhân:** Hệ thống (Tự động chạy ngầm) hoặc Thu ngân (Kích hoạt thủ công).  
* **Mục đích:** Tổng hợp mọi chi phí phát sinh trong quá trình khám chữa bệnh để xác định nghĩa vụ tài chính chính xác.  
* **Luồng sự kiện chính:**  
  1. Thu ngân chọn bệnh nhân từ danh sách "Chờ thanh toán".  
  2. Hệ thống quét toàn bộ hồ sơ của đợt khám này, tập hợp các dịch vụ có trạng thái `UNPAID`:  
     * Công khám bệnh.  
     * Dịch vụ CLS (Xét nghiệm, X-Quang...).  
     * Thuốc, vật tư tiêu hao.  
     * Giường bệnh (nếu nội trú).  
  3. Hệ thống thực hiện **Engine Tính Giá (Pricing Engine)** phức tạp:  
     * Xác định Đơn giá cho từng đối tượng (Giá BHYT, Giá Viện phí, Giá Người nước ngoài...).  
     * Áp dụng quy tắc BHYT:  
       * `Chi phí trong danh mục` \* `%Hưởng BHYT`.  
       * Kiểm tra trần chi phí (Cùng chi trả, Quỹ KCB...).  
       * Xác định các khoản `Đồng chi trả` (Co-payment).  
     * Áp dụng các mã Giảm giá/Miễn giảm (nếu có chính sách từ thiện hoặc ưu đãi).  
  4. Hệ thống tính ra các con số cuối cùng:  
     * `Tổng chi phí`.  
     * `BHYT chi trả`.  
     * `Bệnh nhân cùng chi trả`.  
     * `Bệnh nhân tự trả` (Các khoản ngoài danh mục).  
     * `Tạm ứng đã nộp`.  
     * `Số tiền phải nộp thêm` hoặc `Số tiền hoàn lại`.  
  5. Hệ thống hiển thị Bảng kê chi tiết để Thu ngân giải thích cho khách hàng.

### **UC-4.3: Thu ngân (Thanh toán)**

* **Tác nhân:** Thu ngân  
* **Luồng sự kiện chính:**  
  * Thu ngân in "Bảng kê chi phí" cho bệnh nhân kiểm tra và giải thích các khoản mục (Đặc biệt là phần BHYT từ chối hoặc chênh lệch giá).  
  * Sau khi bệnh nhân đồng ý, Thu ngân chọn phương thức thanh toán:  
    * Tiền mặt.  
    * Chuyển khoản (Quét QR Dynamic \- Tự động xác nhận giao dịch).  
    * Thẻ tín dụng/ATM (Kết nối POS).  
  * Thu ngân nhập số tiền khách đưa. Hệ thống tính toán và hiển thị số tiền thừa cần trả lại (nếu tiền mặt).  
  * Thu ngân nhấn "Thanh toán & Xuất hóa đơn".  
  * Hệ thống thực hiện giao dịch (Transaction):  
    * Ghi nhận doanh thu vào sổ cái.  
    * Chuyển trạng thái tất cả các dịch vụ/đơn thuốc liên quan sang `PAID`.  
    * Mở khóa quy trình tiếp theo (VD: Cho phép Dược sĩ phát thuốc, Cho phép KTV làm xét nghiệm).  
    * Gửi dữ liệu lên cơ quan thuế để xuất Hóa đơn điện tử (E-Invoice).  
  * In Biên lai thu tiền và Hóa đơn GTGT giao cho khách.  
* **Luồng ngoại lệ:**  
  * *Hoàn trả (Refund):* Trong trường hợp bệnh nhân đã đóng tiền nhưng không làm dịch vụ (do đổi ý hoặc bác sĩ hủy chỉ định), Thu ngân thực hiện quy trình "Hoàn ứng/Hủy dịch vụ". Hệ thống yêu cầu xác nhận của Lãnh đạo khoa hoặc Kế toán trưởng trước khi chi tiền ra.

## **5\. PHÂN HỆ BÁO CÁO (REPORTING)**

### **UC-5.1: Thống kê doanh thu**

* **Tác nhân:** Quản lý / Kế toán trưởng / Ban giám đốc  
* **Mục đích:** Cung cấp cái nhìn toàn cảnh về sức khỏe tài chính của bệnh viện, hỗ trợ ra quyết định kinh doanh.  
* **Luồng sự kiện chính:**  
  1. Người dùng truy cập Dashboard báo cáo, chọn module "Tài chính".  
  2. Chọn các tham số lọc báo cáo (Filter):  
     * Khoảng thời gian (Ngày, Tuần, Tháng, Quý, Năm).  
     * Đối tượng (BHYT, Viện phí, Dịch vụ yêu cầu).  
     * Khoa/Phòng thực hiện doanh thu.  
  3. Nhấn "Xem báo cáo". Hệ thống xử lý dữ liệu lớn (Big Data Processing) từ bảng `HoaDon` và `ChiTietHoaDon`.  
  4. Hệ thống hiển thị kết quả dưới dạng đa chiều:  
     * **Biểu đồ:** Biểu đồ cột (Doanh thu theo ngày), Biểu đồ tròn (Cơ cấu nguồn thu).  
     * **Thẻ chỉ số (KPI Card):** Tổng doanh thu, Doanh thu trung bình/Bệnh nhân, Tỷ lệ tăng trưởng so với kỳ trước.  
     * **Bảng số liệu chi tiết:** Cho phép Drill-down (bấm vào dòng tổng để xem chi tiết từng hóa đơn con).  
  5. Người dùng sử dụng tính năng "Xuất dữ liệu" để tải về file Excel/PDF phục vụ lưu trữ hoặc báo cáo cấp trên.

### **UC-5.2: Thống kê bệnh phổ biến**

* **Tác nhân:** Phòng Kế hoạch tổng hợp / Bác sĩ nghiên cứu  
* **Mục đích:** Phân tích mô hình bệnh tật (Dịch tễ học) để dự trù thuốc men, nhân sự và báo cáo lên Sở Y tế.  
* **Luồng sự kiện chính:**  
  1. Người dùng chọn thời gian khảo sát và Chuyên khoa (hoặc toàn viện).  
  2. Hệ thống quét toàn bộ dữ liệu chẩn đoán ICD-10 trong bảng `BenhAn` (EMR) đã hoàn tất (`FINISHED`).  
  3. Hệ thống thực hiện thống kê `COUNT` số lượng ca mắc theo từng mã bệnh và nhóm bệnh (theo chương ICD).  
  4. Hệ thống sắp xếp dữ liệu giảm dần và trích xuất danh sách "Top 10/20 bệnh lý thường gặp nhất".  
  5. Hiển thị biểu đồ Pareto hoặc biểu đồ nhiệt (Heatmap) thể hiện tần suất bệnh theo khu vực địa lý (nếu có dữ liệu địa chỉ).

## **6\. PHÂN HỆ NHÂN SỰ (HRM)**

### **UC-6.1: Phân ca trực (Rostering)**

* **Tác nhân:** Điều dưỡng trưởng / Trưởng khoa / HR  
* **Mục đích:** Đảm bảo bố trí đủ nhân lực cho hoạt động 24/7 của bệnh viện, tuân thủ luật lao động và công bằng giữa các nhân viên.  
* **Luồng sự kiện chính:**  
  * Người dùng chọn Khoa/Phòng cần xếp lịch và Tuần/Tháng mục tiêu.  
  * Hệ thống hiển thị Lưới lịch làm việc (Grid View) với các cột là Ngày và dòng là Ca trực (Sáng, Chiều, Đêm, Trực gác).  
  * Người dùng thực hiện thao tác kéo-thả nhân viên từ danh sách nhân sự vào các ô ca trực.  
    * Hệ thống hỗ trợ "Tự động điền" (Auto-fill) dựa trên mẫu lịch cũ hoặc xoay vòng.  
  * **Kiểm tra quy tắc (Validation Rules):** Ngay khi thả nhân viên vào ô, hệ thống kiểm tra:  
    * *Quy tắc nghỉ ngơi:* Không được trực ca Sáng ngay sau khi vừa trực Đêm hôm trước.  
    * *Tổng giờ làm:* Cảnh báo nếu vượt quá số giờ làm việc tối đa trong tuần (Overtime).  
    * *Trùng lịch:* Nhân viên không thể trực 2 nơi cùng lúc.  
    * *Yêu cầu chuyên môn:* Ca trực cấp cứu bắt buộc phải có ít nhất 1 bác sĩ CK1 và 2 điều dưỡng.  
  * Nếu hợp lệ, hệ thống lưu lịch tạm. Nếu vi phạm nghiêm trọng, hệ thống báo lỗi đỏ và từ chối xếp lịch.  
  * Sau khi hoàn tất, người dùng nhấn "Phát hành lịch" (Publish). Hệ thống gửi thông báo (Notification/Email) lịch làm việc mới đến ứng dụng cá nhân của từng nhân viên.  
* **Luồng ngoại lệ:**  
  * *Xin đổi ca:* Nhân viên có thể gửi yêu cầu đổi ca trực trên phần mềm. Trưởng khoa nhận thông báo và phê duyệt \-\> Hệ thống tự động cập nhật lại lịch.

### **UC-6.3: Tính lương (Payroll)**

* **Tác nhân:** Kế toán lương / HR  
* **Mục đích:** Tự động hóa quy trình tính lương phức tạp đặc thù ngành y tế (lương theo hệ số nhà nước \+ lương dịch vụ \+ phụ cấp trực/thủ thuật).  
* **Tiền điều kiện:** Bảng chấm công (Time attendance) đã được chốt và duyệt. Dữ liệu doanh thu/thủ thuật đã được tổng hợp.  
* **Luồng sự kiện chính:**  
  1. Kế toán chọn kỳ lương (Tháng/Năm) và nhấn "Chạy tính lương".  
  2. Hệ thống duyệt danh sách nhân viên đang hoạt động (`Active`). Với mỗi nhân viên, thực hiện chuỗi công thức:  
     * **Thu nhập 1 (Lương ngạch bậc):** `Hệ số` \* `Lương cơ sở` \* `Số ngày công chuẩn`.  
     * **Thu nhập 2 (Phụ cấp & Trực):** (`Số ca trực` \* `Đơn giá trực`) \+ Phụ cấp độc hại \+ Phụ cấp trách nhiệm.  
     * **Thu nhập 3 (Lương P3/ABC \- Dịch vụ):** Tính % hoa hồng dựa trên tổng doanh thu thủ thuật/khám bệnh mà bác sĩ đã thực hiện (Lấy dữ liệu từ phân hệ Tài chính).  
     * **Các khoản khấu trừ:** BHXH, BHYT, BHTN (theo % quy định), Thuế TNCN (tạm tính theo lũy tiến), Các khoản phạt/trừ tạm ứng.  
     * **Thực lĩnh (Net Salary):** (Thu nhập 1 \+ 2 \+ 3\) \- Khấu trừ.  
  3. Hệ thống tạo ra Bảng lương nháp (`Draft Payroll`).  
  4. Kế toán rà soát, điều chỉnh các khoản đặc biệt (thưởng nóng, truy thu) nếu có.  
  5. Nhấn "Chốt lương & Gửi phiếu lương". Hệ thống gửi Payslip điện tử bảo mật (có password) qua email cho từng nhân viên và tạo lệnh chi lương gửi ngân hàng.

## **7\. PHÂN HỆ QUẢN TRỊ (ADMIN)**

### **UC-7.3: Đăng nhập hệ thống**

* **Tác nhân:** Tất cả người dùng (User)  
* **Luồng sự kiện chính:**  
  * Người dùng mở ứng dụng (Web/Desktop).  
  * Hệ thống hiển thị form đăng nhập. Người dùng nhập Username và Password.  
  * Hệ thống thực hiện xác thực bảo mật:  
    * Băm (Hash) mật khẩu nhập vào bằng thuật toán mạnh (như bcrypt/Argon2).  
    * So sánh chuỗi hash với dữ liệu trong Database.  
  * Nếu thông tin khớp:  
    * Hệ thống kiểm tra trạng thái tài khoản: Nếu `Locked` hoặc `Disabled` \-\> Từ chối truy cập.  
    * Hệ thống kiểm tra thời gian hiệu lực của mật khẩu (nếu có chính sách bắt buộc đổi pass 90 ngày/lần).  
    * Hệ thống tải Profile người dùng và Danh sách quyền hạn (Permissions) tương ứng với Role.  
  * Hệ thống khởi tạo Phiên làm việc (Session/Token JWT) và ghi log "User X đã đăng nhập lúc Y tại IP Z".  
  * Chuyển hướng người dùng vào Dashboard chính phù hợp với vai trò (vd: Bác sĩ vào màn hình Khám, Lễ tân vào màn hình Đăng ký).  
* **Luồng ngoại lệ:**  
  * *Đăng nhập sai:* Nếu sai Username hoặc Password \-\> Hệ thống báo lỗi chung chung "Sai thông tin đăng nhập" (để tránh lộ user). Tăng biến đếm số lần sai. Nếu sai quá 5 lần liên tiếp \-\> Tự động khóa tài khoản (Brute-force protection) và yêu cầu liên hệ Admin để mở khóa.  
  * *Session Timeout:* Nếu người dùng không thao tác trong 30 phút \-\> Hệ thống tự động đăng xuất để bảo mật thông tin bệnh nhân.

### **UC-7.2: Phân quyền (RBAC \- Role Based Access Control)**

* **Tác nhân:** Admin hệ thống  
* **Mục đích:** Quản lý chặt chẽ quyền truy cập dữ liệu theo nguyên tắc "Quyền tối thiểu" (Least Privilege) để đảm bảo an toàn thông tin y tế.  
* **Luồng sự kiện chính:**  
  1. Admin truy cập module "Quản lý Nhóm quyền & Người dùng".  
  2. **Quản lý Role:** Admin định nghĩa các vai trò trong bệnh viện (VD: `Doctor`, `Nurse`, `Receptionist`, `Pharmacist`, `Cashier`, `Admin`).  
  3. **Gán quyền (Assign Permission):**  
     * Admin chọn một Role (ví dụ: `Doctor`).  
     * Hệ thống hiển thị cây chức năng (Menu Tree) và danh sách hành động chi tiết (View, Create, Edit, Delete, Export, Approve).  
     * Admin tích chọn các quyền cho phép. Ví dụ: Bác sĩ được `View` và `Create` Bệnh án, nhưng không được `Delete` Bệnh án và không được `View` Báo cáo tài chính toàn viện.  
  4. Admin nhấn "Lưu cấu hình". Hệ thống cập nhật ma trận phân quyền vào Cache.  
  5. **Gán User vào Role:** Admin gán tài khoản nhân viên cụ thể vào một hoặc nhiều Role. (Ví dụ: Bác sĩ A vừa là `Doctor` vừa là `Department_Head`).  
  6. Khi người dùng thao tác bất kỳ chức năng nào, hệ thống sẽ kiểm tra quyền (Authorization Check) theo cấu hình mới nhất này. Nếu không có quyền \-\> Báo lỗi "Access Denied 403".

