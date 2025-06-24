package com.esprit.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;


public class StripePaymentService {
    private static final String STRIPE_SECRET_KEY = "sk_test_51RJuNtRuIw2oDYbXeMmuyLcPb7b2lyUadITsxHKN5HpTrwy91OgrUwPYmupH03U2VX2q3tJ4ZkDrdxWTFce06q6v001zVsAi3m";

    public StripePaymentService() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public Session createCheckoutSession(long amountInCents, String currency, String successUrl, String cancelUrl) throws StripeException {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(currency)
                                                        .setUnitAmount(amountInCents)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order Payment")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        return Session.create(params);
    }
}
