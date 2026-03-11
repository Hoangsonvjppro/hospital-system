package com.hospital.util;

import com.hospital.bus.ClinicConfigBUS;
import com.hospital.model.ClinicConfig;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceMedicineDetail;
import com.hospital.model.InvoiceServiceDetail;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class InvoicePrinter {

    private static final NumberFormat MONEY_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final BaseColor HEADER_BG = new BaseColor(192, 57, 43);  
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);


    public static void exportPdf(Invoice invoice, String destPath) throws Exception {
        BaseFont bf = createVietnameseBaseFont();

        Font fNormal    = new Font(bf, 11, Font.NORMAL);
        Font fBold      = new Font(bf, 11, Font.BOLD);
        Font fTitle     = new Font(bf, 18, Font.BOLD, HEADER_BG);
        Font fHeader    = new Font(bf, 13, Font.BOLD);
        Font fSmall     = new Font(bf, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fSmallIt   = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
        Font fTableHead = new Font(bf, 10, Font.BOLD, BaseColor.WHITE);
        Font fTotal     = new Font(bf, 13, Font.BOLD, HEADER_BG);

        Document doc = new Document(PageSize.A4, 40, 40, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(destPath));
        doc.open();

        ClinicConfig cfg = new ClinicConfigBUS().getConfig();
        addCentered(doc, cfg.getClinicName().toUpperCase(), fHeader);
        String subHeader = safe(cfg.getClinicAddress());
        if (!cfg.getClinicPhone().isEmpty()) {
            subHeader += "  •  ĐT: " + cfg.getClinicPhone();
        }
        addCentered(doc, subHeader, fSmall);
        doc.add(Chunk.NEWLINE);

        addCentered(doc, "HÓA ĐƠN THANH TOÁN", fTitle);
        doc.add(new Paragraph(" "));

        PdfPTable infoTbl = new PdfPTable(2);
        infoTbl.setWidthPercentage(100);
        infoTbl.setWidths(new float[]{35, 65});
        infoTbl.setSpacingBefore(6);
        infoTbl.setSpacingAfter(14);

        addInfoRow(infoTbl, "Mã hóa đơn:", invoice.getInvoiceCode(), fBold, fNormal);
        addInfoRow(infoTbl, "Ngày khám:", invoice.getExamDate(), fBold, fNormal);
        addInfoRow(infoTbl, "Bệnh nhân:",
                safe(invoice.getPatientName()) + "  (" + invoice.getPatientCode() + ")", fBold, fNormal);
        addInfoRow(infoTbl, "Bác sĩ:", safe(invoice.getDoctorName()), fBold, fNormal);
        if ("PAID".equals(invoice.getStatus())) {
            addInfoRow(infoTbl, "Trạng thái:", invoice.getStatusDisplay(), fBold, fNormal);
            addInfoRow(infoTbl, "Phương thức TT:", invoice.getPaymentMethodDisplay(), fBold, fNormal);
        }
        doc.add(infoTbl);

        PdfPTable table = new PdfPTable(new float[]{6, 34, 10, 8, 21, 21});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        String[] headers = {"#", "Tên mục", "Loại", "SL", "Đơn giá", "Thành tiền"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fTableHead));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(7);
            table.addCell(cell);
        }

        int stt = 1;
        boolean alt = false;

        if (invoice.getExamFee() > 0) {
            addDetailRow(table, stt++, "Phí khám", "Khám",
                    1, invoice.getExamFee(), invoice.getExamFee(), fNormal, alt);
            alt = !alt;
        }

        if (invoice.getServiceDetails() != null) {
            for (InvoiceServiceDetail d : invoice.getServiceDetails()) {
                addDetailRow(table, stt++, d.getServiceName(), "Dịch vụ",
                        d.getQuantity(), d.getUnitPrice(), d.getLineTotal(), fNormal, alt);
                alt = !alt;
            }
        }

        if (invoice.getMedicineDetails() != null) {
            for (InvoiceMedicineDetail d : invoice.getMedicineDetails()) {
                String name = d.getMedicineName();
                if (d.getUnit() != null && !d.getUnit().isEmpty()) {
                    name += " (" + d.getUnit() + ")";
                }
                addDetailRow(table, stt++, name, "Thuốc",
                        d.getQuantity(), d.getUnitPrice(), d.getLineTotal(), fNormal, alt);
                alt = !alt;
            }
        }

        doc.add(table);

        doc.add(new Paragraph(" "));
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(50);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingBefore(6);

        addSummaryRow(summary, "Phí khám:", invoice.getExamFee(), fNormal);
        addSummaryRow(summary, "Tiền thuốc:", invoice.getMedicineFee(), fNormal);
        addSummaryRow(summary, "Phí dịch vụ:", invoice.getOtherFee(), fNormal);
        if (invoice.getDiscount() > 0) {
            addSummaryRow(summary, "Giảm giá:", -invoice.getDiscount(), fNormal);
        }

        PdfPCell totalLbl = new PdfPCell(new Phrase("TỔNG CỘNG:", fTotal));
        totalLbl.setBorder(Rectangle.TOP);
        totalLbl.setBorderColorTop(BaseColor.DARK_GRAY);
        totalLbl.setPaddingTop(8);
        totalLbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        summary.addCell(totalLbl);

        PdfPCell totalVal = new PdfPCell(new Phrase(formatMoney(invoice.getTotalAmount()), fTotal));
        totalVal.setBorder(Rectangle.TOP);
        totalVal.setBorderColorTop(BaseColor.DARK_GRAY);
        totalVal.setPaddingTop(8);
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.addCell(totalVal);

        if ("PAID".equals(invoice.getStatus())) {
            addSummaryRow(summary, "Tiền khách đưa:", invoice.getPaidAmount(), fNormal);
            addSummaryRow(summary, "Tiền thừa:", invoice.getChangeAmount(), fNormal);
        }

        doc.add(summary);

        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        addCentered(doc, "Cảm ơn quý khách đã sử dụng dịch vụ!", fSmallIt);
        addCentered(doc, "Ngày in: " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")), fSmall);

        doc.close();
    }

    private static BaseFont createVietnameseBaseFont() throws Exception {
        String[] candidates = {
                "/usr/share/fonts/jetbrains-mono-fonts/JetBrainsMono-Regular.otf",
                "/usr/share/fonts/jetbrains-mono-fonts/JetBrainsMono-ExtraBoldItalic.otf", 
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }


    private static void addCentered(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private static void addInfoRow(PdfPTable table, String label, String value,
                                   Font fLabel, Font fValue) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fLabel));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(4);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(safe(value), fValue));
        val.setBorder(Rectangle.NO_BORDER);
        val.setPaddingBottom(4);
        table.addCell(val);
    }

    private static void addDetailRow(PdfPTable table, int stt, String name, String type,
                                     int qty, double unitPrice, double lineTotal,
                                     Font font, boolean alternate) {
        BaseColor bg = alternate ? LIGHT_GRAY : BaseColor.WHITE;

        PdfPCell c1 = makeCell(String.valueOf(stt), font, Element.ALIGN_CENTER, bg);
        PdfPCell c2 = makeCell(name, font, Element.ALIGN_LEFT, bg);
        PdfPCell c3 = makeCell(type, font, Element.ALIGN_CENTER, bg);
        PdfPCell c4 = makeCell(String.valueOf(qty), font, Element.ALIGN_CENTER, bg);
        PdfPCell c5 = makeCell(formatMoney(unitPrice), font, Element.ALIGN_RIGHT, bg);
        PdfPCell c6 = makeCell(formatMoney(lineTotal), font, Element.ALIGN_RIGHT, bg);

        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);
        table.addCell(c4);
        table.addCell(c5);
        table.addCell(c6);
    }

    private static PdfPCell makeCell(String text, Font font, int align, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private static void addSummaryRow(PdfPTable table, String label, double value, Font font) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, font));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(3);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(formatMoney(value), font));
        val.setBorder(Rectangle.NO_BORDER);
        val.setPaddingBottom(3);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(val);
    }

    private static String formatMoney(double amount) {
        return MONEY_FMT.format(amount) + " đ";
    }

    private static String safe(String s) {
        return s != null ? s : "—";
    }
}
