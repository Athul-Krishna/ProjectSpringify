package com.athul.springify.shared;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.athul.springify.GeneralConstants;
import com.athul.springify.shared.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class AmazonSES {
    final String PUBLIC_URL = GeneralConstants.ec2InstancePublicUrl;
    final String FROM = GeneralConstants.adminEmail;
    final String SUBJECT = "One last step to complete your registration";
    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering with our mobile app. To complete registration process and be able to login,"
            + " click on the following link: "
            + "<a href='$publicUrl/verification-service/email-verification.html?token=$tokenValue'>"
            + "Final step to complete your registration" + "</a><br/><br/>"
            + "Thank you! And we are waiting for you inside!";

    final String TEXTBODY = "Please verify your email address. "
            + "Thank you for registering with our mobile app. To complete registration process and be able to login,"
            + " open the following URL in your browser window: "
            + " $publicUrl/verification-service/email-verification.html?token=$tokenValue"
            + " Thank you! And we are waiting for you inside!";

    final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Hi, $firstName!</p> "
            + "<p>Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " Otherwise please click on the link below to set a new password: "
            + "<a href='$publicUrl/verification-service/password-reset.html?token=$tokenValue'>"
            + " Click this link to reset password" + "</a><br/><br/>"
            + "Thank you!";

    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password"
            + "Hi, $firstName! "
            + "Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " Otherwise please open the link below in your browser window to set a new password: "
            + " $publicUrl/verification-service/password-reset.html?token=$tokenValue"
            + " Thank you!";

    public void verifyEmail(UserDto userDto) {
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", userDto.getEmailVerificationToken());
        String textBodyWithToken = TEXTBODY.replace("$tokenValue", userDto.getEmailVerificationToken());
        htmlBodyWithToken = htmlBodyWithToken.replace("$publicUrl", PUBLIC_URL);
        textBodyWithToken = textBodyWithToken.replace("$publicUrl", PUBLIC_URL);

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(userDto.getEmail()))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);

        client.sendEmail(request);
        System.out.println("Email sent!");
    }

    public boolean sendPasswordResetRequest(String firstName, String email, String token) {
        boolean returnValue = false;
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
        String htmlBodyWithToken = PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token);
        htmlBodyWithToken = htmlBodyWithToken.replace("$firstName", firstName);
        htmlBodyWithToken = htmlBodyWithToken.replace("$publicUrl", PUBLIC_URL);
        String textBodyWithToken = PASSWORD_RESET_TEXTBODY.replace("$tokenValue", token);
        textBodyWithToken = textBodyWithToken.replace("$firstName", firstName);
        textBodyWithToken = textBodyWithToken.replace("$publicUrl", PUBLIC_URL);

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(email))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
                .withSource(FROM);

        SendEmailResult result = client.sendEmail(request);
        if(result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
            returnValue = true;
        }

        return returnValue;
    }
}
