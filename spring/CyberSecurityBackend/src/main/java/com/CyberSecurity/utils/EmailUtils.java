package com.CyberSecurity.utils;

import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.SetPasswordToken;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.stereotype.Component;
import com.sendgrid.helpers.mail.Mail;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Component
public class EmailUtils {
    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    public void sendMail(String to, String subject, String content) throws IOException {
        Email from = new Email("teodor.perun1@gmail.com");
        Email toEmail = new Email(to);
        Content emailContent = new Content("text/html", content);
        Mail mail = new Mail(from, subject, toEmail, emailContent);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);
        System.out.println(response.getBody());
    }

    public void sendVerificationEmail(Account account){
        String verificationLink = "http://localhost:4200/verify/" + account.getVerification();
        String body = "<!DOCTYPE html>\n" +
                "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "  <title>Verify Your Account</title>\n" +
                "  <!--[if mso]>\n" +
                "  <noscript>\n" +
                "    <xml>\n" +
                "      <o:OfficeDocumentSettings>\n" +
                "        <o:PixelsPerInch>96</o:PixelsPerInch>\n" +
                "      </o:OfficeDocumentSettings>\n" +
                "    </xml>\n" +
                "  </noscript>\n" +
                "  <![endif]-->\n" +
                "</head>\n" +
                "<body style=\"margin:0; padding:0; background-color:#c0d7e0; font-family:'Georgia', 'Times New Roman', serif;\">\n" +
                "\n" +
                "  <!-- Preheader (hidden preview text) -->\n" +
                "  <div style=\"display:none; max-height:0; overflow:hidden; mso-hide:all;\">\n" +
                "    Verify your account to get started &#8203;&zwnj;&nbsp;&#8203;&zwnj;&nbsp;&#8203;&zwnj;\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- Outer wrapper -->\n" +
                "  <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"\n" +
                "    style=\"background-color:#c0d7e0; min-height:100vh;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\" style=\"padding: 48px 16px;\">\n" +
                "\n" +
                "        <!-- Email card -->\n" +
                "        <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"\n" +
                "          style=\"max-width:480px; background-color:#7fa8bb; border-radius:24px; overflow:hidden;\">\n" +
                "\n" +
                "          <!-- Header bar -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color:#2c5566; padding: 28px 40px 24px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Georgia','Times New Roman',serif; font-size:13px;\n" +
                "                letter-spacing:0.2em; text-transform:uppercase; color:#a8ccd8;\">\n" +
                "                Account Verification\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Title -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 40px 40px 8px 40px; text-align:center;\">\n" +
                "              <h1 style=\"margin:0; font-family:'Georgia','Times New Roman',serif; font-size:2.2rem;\n" +
                "                font-weight:700; color:#2c5566; letter-spacing:0.01em; line-height:1.2;\">\n" +
                "                Confirm your<br/>email address\n" +
                "              </h1>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 20px 40px 0 40px;\">\n" +
                "              <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-top: 2px solid #2c5566; opacity:0.25;\"></td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Body text -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 28px 40px 8px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace; font-size:1rem;\n" +
                "                color:#ffffff; line-height:1.7; letter-spacing:0.02em;\">\n" +
                "                Hi <strong>{{name}}</strong>,<br/><br/>\n" +
                "                Thank you for registering. Click the button below\n" +
                "                to verify your email address and activate your account.\n" +
                "                This link will expire in <strong>24 hours</strong>.\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- CTA Button -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding: 36px 40px 32px 40px;\">\n" +
                "              <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-radius: 4px; background-color:#d6e8ef;\n" +
                "                    border: 2.5px solid #2c5566;\">\n" +
                "                    <a href=\"{{verificationLink}}\"\n" +
                "                      style=\"display:inline-block; padding:14px 40px;\n" +
                "                      font-family:'Georgia','Times New Roman',serif;\n" +
                "                      font-size:1.1rem; font-weight:700; color:#2c5566;\n" +
                "                      text-decoration:none; letter-spacing:0.05em;\">\n" +
                "                      Verify Account\n" +
                "                    </a>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Fallback link -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 0 40px 28px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace;\n" +
                "                font-size:0.78rem; color:#ffffff; line-height:1.6; letter-spacing:0.01em;\">\n" +
                "                If the button doesn't work, copy and paste this link into your browser:<br/>\n" +
                "                <a href=\"{{verificationLink}}\"\n" +
                "                  style=\"color:#2c5566; word-break:break-all; text-decoration:underline;\">\n" +
                "                  {{verificationLink}}\n" +
                "                </a>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 0 40px;\">\n" +
                "              <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-top: 2px solid #2c5566; opacity:0.25;\"></td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Footer -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 24px 40px 36px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace;\n" +
                "                font-size:0.75rem; color:#ffffff; line-height:1.7; letter-spacing:0.01em; opacity:0.8;\">\n" +
                "                If you did not create an account, please ignore this email.<br/>\n" +
                "                No action is required on your part.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 12px 0 0 0; font-family:'Georgia','Times New Roman',serif;\n" +
                "                font-size:0.8rem; color:#2c5566; font-weight:700; letter-spacing:0.05em;\">\n" +
                "                &copy; 2026 Public Key Infrastucture\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "        </table>\n" +
                "        <!-- /Email card -->\n" +
                "\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        String subject = "PKI Verification";
        body = body.replace("{{name}}", account.getUser().getName());
        body = body.replace("{{verificationLink}}", verificationLink);
        try{
            sendMail("ignjaticivana70@gmail.com", subject, body);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendSetPasswordEmail(SetPasswordToken token){
        String setPasswordLink = "http://localhost:4200/set-password/" + token.getToken();
        String body = "<!DOCTYPE html>\n" +
                "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "  <title>Set Your Password</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0; padding:0; background-color:#c0d7e0; font-family:'Georgia', 'Times New Roman', serif;\">\n" +
                "\n" +
                "  <div style=\"display:none; max-height:0; overflow:hidden; mso-hide:all;\">\n" +
                "    Set your password to activate your account &#8203;&zwnj;&nbsp;&#8203;&zwnj;\n" +
                "  </div>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"\n" +
                "    style=\"background-color:#c0d7e0; min-height:100vh;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\" style=\"padding: 48px 16px;\">\n" +
                "\n" +
                "        <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"\n" +
                "          style=\"max-width:480px; background-color:#7fa8bb; border-radius:24px; overflow:hidden;\">\n" +
                "\n" +
                "          <!-- Header bar -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color:#2c5566; padding: 28px 40px 24px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Georgia','Times New Roman',serif; font-size:13px;\n" +
                "                letter-spacing:0.2em; text-transform:uppercase; color:#a8ccd8;\">\n" +
                "                Password Setup\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Title -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 40px 40px 8px 40px; text-align:center;\">\n" +
                "              <h1 style=\"margin:0; font-family:'Georgia','Times New Roman',serif; font-size:2.2rem;\n" +
                "                font-weight:700; color:#2c5566; letter-spacing:0.01em; line-height:1.2;\">\n" +
                "                Set your<br/>password\n" +
                "              </h1>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 20px 40px 0 40px;\">\n" +
                "              <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-top: 2px solid #2c5566; opacity:0.25;\"></td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Body text -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 28px 40px 8px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace; font-size:1rem;\n" +
                "                color:#ffffff; line-height:1.7; letter-spacing:0.02em;\">\n" +
                "                Hi <strong>{{name}}</strong>,<br/><br/>\n" +
                "                Click the button below to set your password\n" +
                "                and complete your account setup.\n" +
                "                This link will expire in <strong>24 hours</strong>.\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- CTA Button -->\n" +
                "          <tr>\n" +
                "            <td align=\"center\" style=\"padding: 36px 40px 32px 40px;\">\n" +
                "              <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-radius: 4px; background-color:#d6e8ef;\n" +
                "                    border: 2.5px solid #2c5566;\">\n" +
                "                    <a href=\"{{passwordLink}}\"\n" +
                "                      style=\"display:inline-block; padding:14px 40px;\n" +
                "                      font-family:'Georgia','Times New Roman',serif;\n" +
                "                      font-size:1.1rem; font-weight:700; color:#2c5566;\n" +
                "                      text-decoration:none; letter-spacing:0.05em;\">\n" +
                "                      Set Password\n" +
                "                    </a>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Fallback link -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 0 40px 28px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace;\n" +
                "                font-size:0.78rem; color:#ffffff; line-height:1.6; letter-spacing:0.01em;\">\n" +
                "                If the button doesn't work, copy and paste this link into your browser:<br/>\n" +
                "                <a href=\"{{passwordLink}}\"\n" +
                "                  style=\"color:#2c5566; word-break:break-all; text-decoration:underline;\">\n" +
                "                  {{passwordLink}}\n" +
                "                </a>\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Divider -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 0 40px;\">\n" +
                "              <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                <tr>\n" +
                "                  <td style=\"border-top: 2px solid #2c5566; opacity:0.25;\"></td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "          <!-- Footer -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding: 24px 40px 36px 40px; text-align:center;\">\n" +
                "              <p style=\"margin:0; font-family:'Courier New', Courier, monospace;\n" +
                "                font-size:0.75rem; color:#ffffff; line-height:1.7; letter-spacing:0.01em; opacity:0.8;\">\n" +
                "                If you did not request this, please ignore this email.<br/>\n" +
                "                No action is required on your part.\n" +
                "              </p>\n" +
                "              <p style=\"margin: 12px 0 0 0; font-family:'Georgia','Times New Roman',serif;\n" +
                "                font-size:0.8rem; color:#2c5566; font-weight:700; letter-spacing:0.05em;\">\n" +
                "                &copy; 2026 Public Key Infrastructure\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "\n" +
                "        </table>\n" +
                "\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        String subject = "Setting Password";
        body = body.replace("{{name}}", token.getAccount().getUser().getName());
        body = body.replace("{{passwordLink}}", setPasswordLink);
        try{
            sendMail("ignjaticivana70@gmail.com", subject, body);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
}
