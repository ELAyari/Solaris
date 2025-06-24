package com.esprit.Services;

import com.esprit.models.Evenement;
import com.esprit.models.Reservation;
import com.esprit.models.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

public class TicketService {
//    private final String SENDGRID_API_KEY   = AppConfig.getSendGridApiKey();
   // private final String emailSender      = AppConfig.getEmailSender();
  //  private final String emailSenderName  = AppConfig.getEmailSenderName();

   /* public TicketService() {
        if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isBlank()) {
            throw new IllegalStateException("Missing sendgrid.api.key in config.properties");
        }
    }*/
    private static final String SENDGRID_API_KEY = "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"; // Replace with your SendGrid API key
    private final String emailSender      = "chenoufiwael2@gmail.com";
    private final String emailSenderName  = "Solaris Événements";


    public String generateTicket(Reservation reservation) throws Exception {
        Evenement event = reservation.getEvenement();
        User user = reservation.getUtilisateur();

        // Create a unique filename
        String ticketId = UUID.randomUUID().toString();
        String filename = "ticket_" + ticketId + ".pdf";
        String filePath = System.getProperty("user.home") + File.separator + "tickets";
        Files.createDirectories(Paths.get(filePath));
        String fullPath = filePath + File.separator + filename;

        // Set up PDF
        PdfWriter writer = new PdfWriter(fullPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A5);
        doc.setMargins(20, 20, 20, 20);

        // Title with background
        Paragraph header = new Paragraph("🎫 Votre Ticket d’Événement 🎫")
                .setFontSize(20)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10);
        doc.add(header);

        // Event & User info table
        float[] widths = {1, 2};
        Table infoTable = new Table(UnitValue.createPercentArray(widths))
                .useAllAvailableWidth()
                .setMarginTop(15);

        // Row: Événement
        infoTable.addCell(new Cell().add(new Paragraph("Événement").setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE));
        infoTable.addCell(new Cell().add(new Paragraph(event.getTitre())));

        // Row: Date
        infoTable.addCell(new Cell().add(new Paragraph("Date").setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE));
        infoTable.addCell(new Cell().add(new Paragraph(
                event.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'à' HH:mm"))
        )));

        // Row: Lieu
        infoTable.addCell(new Cell().add(new Paragraph("Lieu").setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE));
        infoTable.addCell(new Cell().add(new Paragraph(event.getAdresse())));

        // Prix
        long cents = reservation.getPriceCents();
        String prix = cents == 0
                ? "Gratuit"
                : String.format("%.2f TND", cents / 100.0);
        infoTable.addCell(new Cell().add(new Paragraph("Prix").setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE));
        infoTable.addCell(new Cell().add(new Paragraph(prix)));

        // Row: Réservé pour
        infoTable.addCell(new Cell().add(new Paragraph("Réservé pour").setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setFontColor(ColorConstants.WHITE));
        infoTable.addCell(new Cell().add(new Paragraph(
                user.getNom() + " " + user.getPrenom()
        )));

        doc.add(infoTable);

        // QR code
        String qrContent = String.format(
                "EVENT:%s;USER:%s;DATE:%s",
                event.getTitre(),
                user.getNom() + "_" + user.getPrenom(),
                event.getDate().toString()
        );
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix matrix = qrWriter.encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
        Path qrPath = Paths.get(filePath, "qr_" + ticketId + ".png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", qrPath);
        Image qrImage = new Image(ImageDataFactory.create(qrPath.toString()))
                .setWidth(150)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginTop(20);
        doc.add(qrImage);
        Files.deleteIfExists(qrPath);

        // Footer note
        Paragraph footer = new Paragraph("Merci de présenter ce ticket à l’entrée de l’événement.")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        doc.add(footer);

        doc.close();
        return fullPath;
    }


    public boolean sendTicketByEmail(String ticketPath, User user, Evenement event) {
        try {
            Email from = new Email(emailSender, emailSenderName);
            Email to = new Email(user.getEmail());
           // Email to = new Email("yossernafeti@gmail.com");
            String subject = "Votre ticket pour " +
                    event.getTitre();

            Content content = new Content(
                    "text/html",
                    "<h2>Merci pour votre réservation!</h2>" +
                            "<p>Veuillez trouver ci-joint votre ticket pour l'événement <b>" +
                            event.getTitre() + "</b>.</p>" +
                            "<p>Date: " + event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>" +
                            "<p>Lieu: " + event.getAdresse() + "</p>" +
                            "<p>N'oubliez pas d'apporter ce ticket le jour de l'événement.</p>"
            );

            Mail mail = new Mail(from, subject, to, content);

            // Add attachment (ticket PDF)
            byte[] fileContent = Files.readAllBytes(Paths.get(ticketPath));
            String fileName = Paths.get(ticketPath).getFileName().toString();

            Attachments attachments = new Attachments();
            attachments.setContent(Base64.getEncoder().encodeToString(fileContent));
            attachments.setType("application/pdf");
            attachments.setFilename(fileName);
            attachments.setDisposition("attachment");
            mail.addAttachments(attachments);

            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            System.out.println(request.getBody());


            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());

            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendCancellationEmail(User user, Evenement event) {
        try {
            Email from = new Email(emailSender, emailSenderName);
            //Email to   = new Email(user.getEmail());
            Email to = new Email("yossernafeti@gmail.com");
            String subject = "Annulation de votre réservation : " + event.getTitre();

            Content content = new Content(
                    "text/html",
                    "<h2>Votre réservation a été annulée</h2>" +
                            "<p>Bonjour " + user.getNom() + ",</p>" +
                            "<p>Votre réservation pour l’événement <b>" + event.getTitre() + "</b> " +
                            "initialement prévue le " +
                            event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                            " a bien été annulée.</p>" +
                            "<p>Nous espérons vous revoir bientôt sur un autre événement.</p>"
            );

            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}