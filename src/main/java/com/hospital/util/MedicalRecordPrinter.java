package com.hospital.util;

import com.hospital.bus.*;
import com.hospital.dao.DoctorDAO;
import com.hospital.model.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tiện ích xuất bệnh án tóm tắt ra file PDF.
 * <p>
 * Sử dụng thư viện iText 5 với font hệ thống hỗ trợ tiếng Việt (Unicode).
 * <p>
 * Cách dùng:
 * <pre>
 *   MedicalRecordPrinter.exportPdf(record, "C:/output/BA00001.pdf");
 * </pre>
 */
public class MedicalRecordPrinter {

    private static final NumberFormat MONEY_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final BaseColor HEADER_BG = new BaseColor(192, 57, 43);   // PRIMARY_RED
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Xuất bệnh án tóm tắt ra file PDF.
     *
     * @param record   bệnh án (MedicalRecord)
     * @param destPath đường dẫn file PDF đích
     * @throws Exception nếu có lỗi ghi file hoặc font
     */
    public static void exportPdf(MedicalRecord record, String destPath) throws Exception {
        BaseFont bf = createVietnameseBaseFont();

        Font fNormal    = new Font(bf, 11, Font.NORMAL);
        Font fBold      = new Font(bf, 11, Font.BOLD);
        Font fTitle     = new Font(bf, 18, Font.BOLD, HEADER_BG);
        Font fHeader    = new Font(bf, 13, Font.BOLD);
        Font fSection   = new Font(bf, 12, Font.BOLD, HEADER_BG);
        Font fSmall     = new Font(bf, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fSmallIt   = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
        Font fTableHead = new Font(bf, 10, Font.BOLD, BaseColor.WHITE);

        Document doc = new Document(PageSize.A4, 40, 40, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(destPath));
        doc.open();

        // ── 1. HEADER — Tên phòng khám ──────────────────────
        ClinicConfig cfg = new ClinicConfigBUS().getConfig();
        addCentered(doc, cfg.getClinicName().toUpperCase(), fHeader);
        String subHeader = safe(cfg.getClinicAddress());
        if (cfg.getClinicPhone() != null && !cfg.getClinicPhone().isEmpty()) {
            subHeader += "  •  ĐT: " + cfg.getClinicPhone();
        }
        addCentered(doc, subHeader, fSmall);
        doc.add(Chunk.NEWLINE);

        // ── 2. TITLE ─────────────────────────────────────────
        addCentered(doc, "BỆNH ÁN TÓM TẮT", fTitle);
        addCentered(doc, "Mã bệnh án: #" + record.getId(), fSmall);
        doc.add(new Paragraph(" "));

        // ── 3. THÔNG TIN BỆNH NHÂN ──────────────────────────
        addSectionTitle(doc, "I. THÔNG TIN BỆNH NHÂN", fSection);

        PdfPTable patientTbl = new PdfPTable(2);
        patientTbl.setWidthPercentage(100);
        patientTbl.setWidths(new float[]{30, 70});
        patientTbl.setSpacingBefore(4);
        patientTbl.setSpacingAfter(10);

        Patient patient = new PatientBUS().findById((int) record.getPatientId());
        if (patient != null) {
            addInfoRow(patientTbl, "Họ tên:", patient.getFullName(), fBold, fNormal);
            addInfoRow(patientTbl, "Mã BN:", patient.getPatientCode(), fBold, fNormal);
            addInfoRow(patientTbl, "Giới tính:", patient.getGender() != null ? patient.getGender().toString() : "—", fBold, fNormal);
            addInfoRow(patientTbl, "Ngày sinh:", patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FMT) : "—", fBold, fNormal);
            addInfoRow(patientTbl, "SĐT:", safe(patient.getPhone()), fBold, fNormal);
            addInfoRow(patientTbl, "Địa chỉ:", safe(patient.getAddress()), fBold, fNormal);
            if (patient.getAllergyHistory() != null && !patient.getAllergyHistory().isEmpty()) {
                addInfoRow(patientTbl, "Dị ứng:", patient.getAllergyHistory(), fBold, fNormal);
            }
        }

        Doctor doctor = new DoctorDAO().findById((int) record.getDoctorId());
        addInfoRow(patientTbl, "Bác sĩ khám:", doctor != null ? doctor.getFullName() : "—", fBold, fNormal);
        addInfoRow(patientTbl, "Ngày khám:", record.getVisitDate() != null
                ? record.getVisitDate().format(DATETIME_FMT) : "—", fBold, fNormal);

        doc.add(patientTbl);

        // ── 4. SINH HIỆU ────────────────────────────────────
        addSectionTitle(doc, "II. SINH HIỆU", fSection);

        PdfPTable vitalsTbl = new PdfPTable(6);
        vitalsTbl.setWidthPercentage(100);
        vitalsTbl.setSpacingBefore(4);
        vitalsTbl.setSpacingAfter(10);

        String[] vitalHeaders = {"Cân nặng", "Chiều cao", "Huyết áp", "Mạch", "Nhiệt độ", "SpO2"};
        for (String h : vitalHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fTableHead));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            vitalsTbl.addCell(cell);
        }

        addVitalCell(vitalsTbl, record.getWeight() > 0 ? record.getWeight() + " kg" : "—", fNormal);
        addVitalCell(vitalsTbl, record.getHeight() > 0 ? record.getHeight() + " cm" : "—", fNormal);
        addVitalCell(vitalsTbl, safe(record.getBloodPressure()), fNormal);
        addVitalCell(vitalsTbl, record.getPulse() > 0 ? record.getPulse() + " bpm" : "—", fNormal);
        addVitalCell(vitalsTbl, record.getTemperature() > 0 ? record.getTemperature() + "°C" : "—", fNormal);
        addVitalCell(vitalsTbl, record.getSpo2() > 0 ? record.getSpo2() + "%" : "—", fNormal);

        doc.add(vitalsTbl);

        // ── 5. CHẨN ĐOÁN ────────────────────────────────────
        addSectionTitle(doc, "III. CHẨN ĐOÁN", fSection);

        PdfPTable diagTbl = new PdfPTable(2);
        diagTbl.setWidthPercentage(100);
        diagTbl.setWidths(new float[]{30, 70});
        diagTbl.setSpacingBefore(4);
        diagTbl.setSpacingAfter(10);

        addInfoRow(diagTbl, "Triệu chứng:", safe(record.getSymptoms()), fBold, fNormal);
        addInfoRow(diagTbl, "Chẩn đoán:", safe(record.getDiagnosis()), fBold, fNormal);
        if (record.getDiagnosisCode() != null && !record.getDiagnosisCode().isEmpty()) {
            addInfoRow(diagTbl, "Mã ICD-10:", record.getDiagnosisCode(), fBold, fNormal);
        }
        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            addInfoRow(diagTbl, "Ghi chú BS:", record.getNotes(), fBold, fNormal);
        }

        doc.add(diagTbl);

        // ── 6. KẾT QUẢ XÉT NGHIỆM (nếu có) ────────────────
        List<LabResult> labResults = new LabResultBUS().findByRecordId(record.getId());
        if (labResults != null && !labResults.isEmpty()) {
            addSectionTitle(doc, "IV. KẾT QUẢ XÉT NGHIỆM", fSection);

            PdfPTable labTbl = new PdfPTable(new float[]{6, 30, 20, 14, 20, 10});
            labTbl.setWidthPercentage(100);
            labTbl.setSpacingBefore(4);
            labTbl.setSpacingAfter(10);

            String[] labHeaders = {"#", "Tên xét nghiệm", "Kết quả", "Đơn vị", "Giá trị BT", "Ghi chú"};
            for (String h : labHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fTableHead));
                cell.setBackgroundColor(HEADER_BG);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                labTbl.addCell(cell);
            }

            int stt = 1;
            boolean alt = false;
            for (LabResult lr : labResults) {
                BaseColor bg = alt ? LIGHT_GRAY : BaseColor.WHITE;
                addCell(labTbl, String.valueOf(stt++), fNormal, Element.ALIGN_CENTER, bg);
                addCell(labTbl, safe(lr.getTestName()), fNormal, Element.ALIGN_LEFT, bg);
                addCell(labTbl, safe(lr.getResultValue()), fNormal, Element.ALIGN_CENTER, bg);
                addCell(labTbl, safe(lr.getUnit()), fNormal, Element.ALIGN_CENTER, bg);
                addCell(labTbl, safe(lr.getNormalRange()), fNormal, Element.ALIGN_CENTER, bg);
                addCell(labTbl, safe(lr.getNotes()), fNormal, Element.ALIGN_CENTER, bg);
                alt = !alt;
            }

            doc.add(labTbl);
        }

        // ── 7. ĐƠN THUỐC ───────────────────────────────────
        PrescriptionBUS prescriptionBUS = new PrescriptionBUS();
        List<Prescription> prescriptions = prescriptionBUS.getByMedicalRecordId(record.getId());
        if (prescriptions != null && !prescriptions.isEmpty()) {
            String sectionNum = (labResults != null && !labResults.isEmpty()) ? "V" : "IV";
            addSectionTitle(doc, sectionNum + ". ĐƠN THUỐC", fSection);

            PdfPTable rxTbl = new PdfPTable(new float[]{6, 28, 20, 8, 14, 14, 10});
            rxTbl.setWidthPercentage(100);
            rxTbl.setSpacingBefore(4);
            rxTbl.setSpacingAfter(10);

            String[] rxHeaders = {"#", "Tên thuốc", "Liều dùng", "SL", "Đơn giá", "Thành tiền", "Hướng dẫn"};
            for (String h : rxHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fTableHead));
                cell.setBackgroundColor(HEADER_BG);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                rxTbl.addCell(cell);
            }

            int stt = 1;
            boolean alt = false;
            double totalRx = 0;
            for (Prescription presc : prescriptions) {
                List<PrescriptionDetail> details = prescriptionBUS.getDetails(presc.getId());
                for (PrescriptionDetail d : details) {
                    BaseColor bg = alt ? LIGHT_GRAY : BaseColor.WHITE;
                    double lineTotal = d.getQuantity() * d.getUnitPrice();
                    totalRx += lineTotal;

                    addCell(rxTbl, String.valueOf(stt++), fNormal, Element.ALIGN_CENTER, bg);
                    addCell(rxTbl, safe(d.getMedicineName()), fNormal, Element.ALIGN_LEFT, bg);
                    addCell(rxTbl, safe(d.getDosage()), fNormal, Element.ALIGN_LEFT, bg);
                    addCell(rxTbl, String.valueOf(d.getQuantity()), fNormal, Element.ALIGN_CENTER, bg);
                    addCell(rxTbl, formatMoney(d.getUnitPrice()), fNormal, Element.ALIGN_RIGHT, bg);
                    addCell(rxTbl, formatMoney(lineTotal), fNormal, Element.ALIGN_RIGHT, bg);
                    addCell(rxTbl, safe(d.getInstruction()), fNormal, Element.ALIGN_LEFT, bg);
                    alt = !alt;
                }
            }

            doc.add(rxTbl);

            // Tổng tiền thuốc
            Paragraph pTotal = new Paragraph("Tổng tiền thuốc: " + formatMoney(totalRx), fBold);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            pTotal.setSpacingAfter(10);
            doc.add(pTotal);
        }

        // ── 8. HẸN TÁI KHÁM ────────────────────────────────
        if (record.getFollowUpDate() != null) {
            doc.add(Chunk.NEWLINE);
            Paragraph pFollowUp = new Paragraph(
                    "📅 Hẹn tái khám: " + record.getFollowUpDate().format(DATE_FMT), fBold);
            pFollowUp.setSpacingBefore(6);
            doc.add(pFollowUp);
        }

        // ── 9. FOOTER ───────────────────────────────────────
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        addCentered(doc, "Chúc quý bệnh nhân sớm bình phục!", fSmallIt);
        addCentered(doc, "Ngày in: " + LocalDate.now().format(DATE_FMT), fSmall);

        doc.close();
    }

    // ══════════════════════════════════════════════════════════
    //  FONT HELPER
    // ══════════════════════════════════════════════════════════

    private static BaseFont createVietnameseBaseFont() throws Exception {
        String[] candidates = {
                "c:/windows/fonts/times.ttf",
                "c:/windows/fonts/arial.ttf",
                "c:/windows/fonts/tahoma.ttf",
                "c:/windows/fonts/segoeui.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }

    // ══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    private static void addCentered(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private static void addSectionTitle(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(2);
        doc.add(p);
    }

    private static void addInfoRow(PdfPTable table, String label, String value,
                                   Font fLabel, Font fValue) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fLabel));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(3);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(safe(value), fValue));
        val.setBorder(Rectangle.NO_BORDER);
        val.setPaddingBottom(3);
        table.addCell(val);
    }

    private static void addVitalCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, Font font, int align, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static String formatMoney(double amount) {
        return MONEY_FMT.format(amount) + " đ";
    }

    private static String safe(String s) {
        return s != null ? s : "—";
    }
}
