package com.vinova.booking_hotel.payment.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.vinova.booking_hotel.payment.dto.PaymentRequestDto;
import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    //Stripe-API
    //-> Name, amount, quantity, currency
    //-> return sessionId and url
    public StripeResponseDto checkoutBooking(PaymentRequestDto paymentRequestDto) {
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData bookingData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(paymentRequestDto.getName()).build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(paymentRequestDto.getCurrency() == null ? "USD" : paymentRequestDto.getCurrency())
                .setUnitAmount(paymentRequestDto.getAmount())
                .setProductData(bookingData)
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(paymentRequestDto.getQuantity())
                .setPriceData(priceData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/api/v1/public/payment/success")
                .setCancelUrl("http://localhost:8080/api/v1/public/payment/cancel")
                .addLineItem(lineItem)
                .build();

        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException ex) {
            throw new RuntimeException("Failed to create Stripe session: " + ex.getMessage());
        }
        
        return StripeResponseDto.builder()
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }

}
