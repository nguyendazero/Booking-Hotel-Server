package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.model.Booking;

import java.time.LocalDate;
import java.util.List;

public interface PdfService {
    String createBookingReport(List<Booking> bookings, LocalDate date);
}