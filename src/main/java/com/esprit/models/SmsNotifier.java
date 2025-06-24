package com.esprit.models;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsNotifier {

    public static final String ACCOUNT_SID = "AC95d552f2410a17641dcec63a1dfb7398";
    public static final String AUTH_TOKEN = "ec2821722fc7d5267a7f33e557ca0186";
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
