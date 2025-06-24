package com.esprit.models;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsNotifier {

    public static final String ACCOUNT_SID = com.esprit.config.Secrets.get("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = com.esprit.config.Secrets.get("TWILIO_AUTH_TOKEN");
    public static final String FROM_NUMBER = "+19134301033"; // Numéro Twilio

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendSms(String to, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(FROM_NUMBER),
                messageBody
        ).create();

        System.out.println("SMS envoyé : " + message.getSid());
    }
}
