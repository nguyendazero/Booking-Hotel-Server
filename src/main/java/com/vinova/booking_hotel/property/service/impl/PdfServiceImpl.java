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
import com.itextpdf.layout.properties.TextAlignment;
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
        // Tạo đường dẫn cho tệp PDF
        String directoryPath = "reports";
        File directory = new File(directoryPath);

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        if (!directory.exists()) {
            directory.mkdir();
        }

        String filePath = directoryPath + File.separator + "booking_report_" + date + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(new File(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Thêm tiêu đề với định dạng
            PdfFont font = PdfFontFactory.createFont("Helvetica-Bold");
            Paragraph title = new Paragraph("Booking Report for " + date)
                    .setFont(font)
                    .setFontSize(18)
                    .setFontColor(ColorConstants.GREEN)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Thêm một dòng phân cách
            document.add(new Paragraph("-----------------------------------------------------")
                    .setTextAlignment(TextAlignment.CENTER));

            // Tạo bảng cho các booking
            Table table = new Table(6);
            table.setWidth(100);

            // Thêm tiêu đề cho bảng
            table.addHeaderCell(new Cell().add(new Paragraph("Booking ID")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Hotel")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Start Date")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("End Date")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Price")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Status")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            if (bookings.isEmpty()) {
                document.add(new Paragraph("No bookings for today.")
                        .setTextAlignment(TextAlignment.CENTER));
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
            document.add(new Paragraph("-----------------------------------------------------")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Thank you for using our booking service!")
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
        } catch (IOException e) {
            System.out.println("ERROR CREATE PDF: " + e.getMessage());
        }

        return filePath;
    }
}