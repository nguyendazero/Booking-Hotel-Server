package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.service.impl.PdfServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfServiceImplTest {

    @InjectMocks
    private PdfServiceImpl pdfService;

    @TempDir
    Path tempDir;

    private Path reportsDirectory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportsDirectory = tempDir.resolve("reports");
        // Đảm bảo thư mục được tạo trước khi chạy test
        if (!reportsDirectory.toFile().exists()) {
            reportsDirectory.toFile().mkdirs();
        }
    }

    @Test
    void createBookingReport_shouldCreatePdfFileWithCorrectPath_whenBookingsExist() {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Booking> bookings = Arrays.asList(
                createBooking(1L, "Hotel A", today.plusDays(1), today.plusDays(3), 200.0, BookingStatus.CONFIRMED),
                createBooking(2L, "Hotel B", today.plusDays(2), today.plusDays(4), 150.0, BookingStatus.PENDING)
        );
        // Lấy đường dẫn tuyệt đối của thư mục "reports" bên trong thư mục dự án
        String projectReportsPath = new File("reports").getAbsolutePath();
        Path expectedFilePath = Path.of(projectReportsPath, "booking_report_" + today + ".pdf");

        // Act
        String filePath = pdfService.createBookingReport(bookings, today);
        File createdFile = new File(filePath);

        // Assert
        assertTrue(new File("reports").exists(), "Reports directory should exist"); // Kiểm tra thư mục trong dự án
        assertTrue(createdFile.exists(), "PDF file should exist");
        assertEquals(expectedFilePath.toString(), createdFile.getAbsolutePath(), "File path should be correct");
        assertTrue(createdFile.length() > 0, "PDF file should not be empty");

        // Cleanup
        createdFile.delete();
        new File("reports").delete();
    }

    @Test
    void createBookingReport_shouldCreatePdfFileWithCorrectPath_whenNoBookingsExist() {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Booking> bookings = List.of();

        // Lấy đường dẫn tuyệt đối đến thư mục "reports" trong thư mục dự án
        String projectReportsPath = new File("reports").getAbsolutePath();
        Path expectedFilePath = Path.of(projectReportsPath, "booking_report_" + today + ".pdf");

        // Act
        String filePath = pdfService.createBookingReport(bookings, today);
        File createdFile = new File(filePath);

        // Assert
        assertTrue(new File("reports").exists(), "Reports directory in project should exist");
        assertTrue(createdFile.exists(), "PDF file should exist");
        assertEquals(expectedFilePath.toString(), createdFile.getAbsolutePath(), "File path should be correct");
        assertTrue(createdFile.length() > 0, "PDF file should not be empty");

        // Cleanup
        createdFile.delete();
        new File("reports").delete();
    }

    @Test
    void createBookingReport_shouldContainNoBookingsMessage_whenNoBookingsExist() throws IOException {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Booking> bookings = List.of();
        Path expectedFilePath = reportsDirectory.resolve("booking_report_" + today + ".pdf");

        // Act
        String filePath = pdfService.createBookingReport(bookings, today);
        File createdFile = new File(filePath);

        // Assert
        assertTrue(reportsDirectory.toFile().exists(), "Reports directory should exist");
        assertTrue(createdFile.exists(), "PDF file should exist");
        assertTrue(createdFile.length() > 0, "PDF file should not be empty");

        // Cleanup
        createdFile.delete();
        new File("reports").delete();
    }

    @Test
    void createBookingReportNoBookings_shouldCreatePdfFileWithCorrectPath() {
        // Arrange
        LocalDate today = LocalDate.now();
        Account owner = new Account();
        owner.setId(123L);
        owner.setUsername("testOwner");

        // Lấy đường dẫn tuyệt đối đến thư mục "reports" trong thư mục dự án
        String projectReportsPath = new File("reports").getAbsolutePath();
        Path expectedFilePath = Path.of(projectReportsPath, "no_bookings_report_" + owner.getId() + "_" + today + ".pdf");

        // Act
        String filePath = pdfService.createBookingReportNoBookings(owner, today);
        File createdFile = new File(filePath);

        // Assert
        assertTrue(new File("reports").exists(), "Reports directory in project should exist");
        assertTrue(createdFile.exists(), "PDF file should exist");
        assertEquals(expectedFilePath.toString(), createdFile.getAbsolutePath(), "File path should be correct");
        assertTrue(createdFile.length() > 0, "PDF file should not be empty");

        // Cleanup
        createdFile.delete();
        new File("reports").delete();
    }

    @Test
    void createBookingReportNoBookings_shouldContainOwnerInfoInFilename() {
        // Arrange
        LocalDate today = LocalDate.now();
        Account owner = new Account();
        owner.setId(456L);
        owner.setUsername("anotherOwner");

        // Lấy đường dẫn tuyệt đối đến thư mục "reports" trong thư mục dự án
        String projectReportsPath = new File("reports").getAbsolutePath();
        Path expectedFilePath = Path.of(projectReportsPath, "no_bookings_report_" + owner.getId() + "_" + today + ".pdf");

        // Act
        String filePath = pdfService.createBookingReportNoBookings(owner, today);
        File createdFile = new File(filePath);

        // Assert
        assertTrue(new File("reports").exists(), "Reports directory in project should exist");
        assertTrue(createdFile.exists(), "PDF file should exist");
        assertEquals(expectedFilePath.toString(), createdFile.getAbsolutePath(), "File path should be correct");
        assertTrue(createdFile.getAbsolutePath().contains(String.valueOf(owner.getId())), "File path should contain owner ID");
        assertTrue(createdFile.length() > 0, "PDF file should not be empty");

        // Cleanup
        createdFile.delete();
        new File("reports").delete();
    }

    // Helper method to create a Booking object
    private Booking createBooking(Long id, String hotelName, LocalDate startDate, LocalDate endDate, Double totalPrice, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        Hotel hotel = new Hotel();
        hotel.setName(hotelName);
        booking.setHotel(hotel);
        booking.setStartDate(ZonedDateTime.from(startDate.atStartOfDay(ZoneId.systemDefault())));
        booking.setEndDate(ZonedDateTime.from(endDate.atStartOfDay(ZoneId.systemDefault())));
        booking.setTotalPrice(BigDecimal.valueOf(totalPrice));
        booking.setStatus(status);
        return booking;
    }
}