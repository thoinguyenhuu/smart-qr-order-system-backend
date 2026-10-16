package com.ht.project.smart_qr_menu_order_system.service.V3;

import com.ht.project.smart_qr_menu_order_system.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SendGridEmailService implements EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Override
    public void sendEmail(String to, String subject, String content) {
        Mail mail = createHtmlMail(to, subject, content);
        send(mail, "email");
    }

    @Override
    public void sendAccountVerificationEmail(String to, String verificationUrl) {
        String subject = "Xác thực tài khoản Smart QR";
        String plainText = buildVerificationEmailPlainText(verificationUrl);
        String htmlContent = buildVerificationEmailHtml(to, verificationUrl);

        Mail mail = createTransactionalMail(to, subject, plainText, htmlContent);
        send(mail, "verification email");
    }

    private Mail createHtmlMail(String to, String subject, String htmlContent) {
        Mail mail = new Mail();
        Personalization personalization = new Personalization();

        personalization.addTo(new Email(to));
        mail.setFrom(new Email(fromEmail, fromName));
        mail.setSubject(subject);
        mail.addPersonalization(personalization);
        mail.addContent(new Content("text/html", htmlContent));
        return mail;
    }

    private Mail createTransactionalMail(String to, String subject, String plainText, String htmlContent) {
        Mail mail = new Mail();
        Personalization personalization = new Personalization();

        personalization.addTo(new Email(to));
        mail.setFrom(new Email(fromEmail, fromName));
        mail.setSubject(subject);
        mail.addPersonalization(personalization);
        mail.addContent(new Content("text/plain", plainText));
        mail.addContent(new Content("text/html", htmlContent));
        return mail;
    }

    private void send(Mail mail, String emailType) {
        try {
            SendGrid sendGrid = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getBody());
            }

            log.info("SendGrid {} queued successfully", emailType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send " + emailType + " via SendGrid", e);
        }
    }

    private String buildVerificationEmailPlainText(String verificationUrl) {
        return """
                Smart QR - Xác thực tài khoản

                Cảm ơn bạn đã đăng ký tài khoản quản lý nhà hàng trên Smart QR.
                Nhấn vào liên kết bên dưới để kích hoạt tài khoản:

                %s

                Nếu bạn không thực hiện đăng ký, bạn có thể bỏ qua email này.
                """
                .formatted(verificationUrl);
    }

    private String buildVerificationEmailHtml(String email, String verificationUrl) {
        String escapedEmail = escapeHtml(email);
        String escapedVerificationUrl = escapeHtml(verificationUrl);

        return """
                <!DOCTYPE html>
                <html lang="vi">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Xác thực tài khoản Smart QR</title>
                  </head>
                  <body style="margin:0;padding:0;background-color:#f8fafc;font-family:Arial,'Helvetica Neue',Helvetica,sans-serif;color:#0f172a;">
                    <div style="display:none;max-height:0;overflow:hidden;opacity:0;">
                      Hoàn tất xác thực tài khoản Smart QR để bắt đầu quản lý nhà hàng.
                    </div>
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f8fafc;padding:24px 12px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:600px;background:#ffffff;border:1px solid #e2e8f0;border-radius:24px;overflow:hidden;">
                            <tr>
                              <td style="padding:0;background:linear-gradient(135deg,#0f172a 0%%,#1e293b 58%%,#f59e0b 100%%);">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                  <tr>
                                    <td style="padding:32px 32px 28px 32px;">
                                      <div style="display:inline-block;padding:8px 14px;border-radius:999px;background:rgba(255,255,255,0.12);color:#ffffff;font-size:12px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;">
                                        Smart QR
                                      </div>
                                      <h1 style="margin:18px 0 10px 0;font-size:30px;line-height:1.25;color:#ffffff;font-weight:700;">
                                        Kích hoạt tài khoản quản lý nhà hàng
                                      </h1>
                                      <p style="margin:0;font-size:15px;line-height:1.8;color:rgba(255,255,255,0.82);">
                                        Hoàn tất xác thực email để bắt đầu đăng nhập, quản lý menu QR và xử lý đơn hàng theo thời gian thực.
                                      </p>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:32px;">
                                <p style="margin:0 0 12px 0;font-size:14px;line-height:1.7;color:#475569;">
                                  Xin chào <strong style="color:#0f172a;">%s</strong>,
                                </p>
                                <p style="margin:0 0 20px 0;font-size:15px;line-height:1.8;color:#334155;">
                                  Hệ thống đã nhận yêu cầu tạo tài khoản chủ quán trên Smart QR. Nhấn vào nút bên dưới để xác thực email và kích hoạt tài khoản.
                                </p>
                                <table role="presentation" cellspacing="0" cellpadding="0" style="margin:0 0 24px 0;">
                                  <tr>
                                    <td align="center" bgcolor="#f59e0b" style="border-radius:14px;">
                                      <a href="%s" style="display:inline-block;padding:14px 24px;font-size:15px;font-weight:700;line-height:1;color:#0f172a;text-decoration:none;">
                                        Xác thực tài khoản
                                      </a>
                                    </td>
                                  </tr>
                                </table>
                                <div style="padding:18px 20px;border-radius:18px;background:#fff7ed;border:1px solid #fdba74;margin-bottom:24px;">
                                  <p style="margin:0 0 8px 0;font-size:13px;font-weight:700;letter-spacing:0.08em;text-transform:uppercase;color:#c2410c;">
                                    Lưu ý
                                  </p>
                                  <p style="margin:0;font-size:14px;line-height:1.8;color:#7c2d12;">
                                    Nếu nút không hoạt động, hãy sao chép liên kết bên dưới và mở trong trình duyệt của bạn.
                                  </p>
                                </div>
                                <p style="margin:0 0 8px 0;font-size:13px;font-weight:700;color:#0f172a;">
                                  Liên kết dự phòng
                                </p>
                                <p style="margin:0 0 24px 0;font-size:14px;line-height:1.8;word-break:break-all;">
                                  <a href="%s" style="color:#ea580c;text-decoration:none;">%s</a>
                                </p>
                                <p style="margin:0;font-size:13px;line-height:1.8;color:#64748b;">
                                  Nếu bạn không thực hiện đăng ký, bạn có thể bỏ qua email này.
                                </p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:20px 32px 28px 32px;border-top:1px solid #e2e8f0;background:#f8fafc;">
                                <p style="margin:0;font-size:12px;line-height:1.8;color:#64748b;">
                                  Smart QR Menu &amp; Order System
                                </p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """
                .formatted(escapedEmail, escapedVerificationUrl, escapedVerificationUrl, escapedVerificationUrl);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
