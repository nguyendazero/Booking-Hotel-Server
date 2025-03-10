package com.vinova.booking_hotel.property.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.service.PdfService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService{

    @Override
    public String createBookingReport(List<Booking> bookings, LocalDate date) {
        String filePath = "reports/booking_report_" + date + ".pdf";
        new File("reports").mkdir(); // Tạo thư mục nếu chưa có

        try {
            PdfWriter writer = new PdfWriter(new File(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Thêm tiêu đề
            document.add(new Paragraph("Booking Report for " + date));

            if (bookings.isEmpty()) {
                document.add(new Paragraph("No bookings for today."));
            } else {
                for (Booking booking : bookings) {
                    document.add(new Paragraph("Booking ID: " + booking.getId()));
                    document.add(new Paragraph("Hotel: " + booking.getHotel().getName()));
                    document.add(new Paragraph("Start Date: " + booking.getStartDate()));
                    document.add(new Paragraph("End Date: " + booking.getEndDate()));
                    document.add(new Paragraph("Total Price: " + booking.getTotalPrice()));
                    document.add(new Paragraph("Status: " + booking.getStatus()));
                    document.add(new Paragraph("")); // Thêm dòng trống
                }
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }
}
