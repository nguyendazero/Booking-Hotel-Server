package com.vinova.booking_hotel.property.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.service.PdfService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService {

    @Override
    public String createBookingReport(List<Booking> bookings, LocalDate date) {
        String filePath = "reports/booking_report_" + date + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(new File(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Thêm tiêu đề với định dạng
            PdfFont font = PdfFontFactory.createFont("Helvetica-Bold");
            Paragraph title = new Paragraph("Booking Report for " + date)
                    .setFont(font)
                    .setFontSize(18) // Kích thước phông chữ
                    .setFontColor(ColorConstants.BLUE); // Màu phông chữ
            document.add(title);

            // Thêm một dòng phân cách
            document.add(new Paragraph("-----------------------------------------------------"));

            // Tạo bảng cho các booking
            Table table = new Table(6); // Số cột
            table.setWidth(100); // Đặt chiều rộng bảng

            // Thêm tiêu đề cho bảng
            table.addHeaderCell(new Cell().add(new Paragraph("Booking ID")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Hotel")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Start Date")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("End Date")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Price")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Status")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            if (bookings.isEmpty()) {
                document.add(new Paragraph("No bookings for today."));
            } else {
                for (Booking booking : bookings) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(booking.getId()))));
                    table.addCell(new Cell().add(new Paragraph(booking.getHotel().getName())));
                    table.addCell(new Cell().add(new Paragraph(booking.getStartDate().toString())));
                    table.addCell(new Cell().add(new Paragraph(booking.getEndDate().toString())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(booking.getTotalPrice()))));
                    table.addCell(new Cell().add(new Paragraph(booking.getStatus().toString())));
                }
                document.add(table);
            }

            // Thêm footer
            document.add(new Paragraph("-----------------------------------------------------"));
            document.add(new Paragraph("Thank you for using our booking service!"));

            document.close();
        } catch (IOException e) {
            System.out.println("Lỗi khi tạo báo cáo PDF: " + e.getMessage());
        }

        return filePath;
    }
}