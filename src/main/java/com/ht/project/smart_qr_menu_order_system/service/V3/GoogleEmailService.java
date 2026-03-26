package com.ht.project.smart_qr_menu_order_system.service.V3;

import com.ht.project.smart_qr_menu_order_system.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleEmailService implements EmailService {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            log.info("Email queued for {} with subject {}", to, subject);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendAccountVerificationEmail(String to, String verificationUrl) {
        log.info("Đã vào service khác !!!");
        String subject = "Xác thực tài khoản Smart QR";
        String plainText = buildVerificationEmailPlainText(verificationUrl);
        String htmlContent = buildVerificationEmailHtml(to, verificationUrl);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, htmlContent);

            log.info("Verification email queued for {}", to);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send account verification email", e);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String content, File attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.addAttachment(attachment.getName(), attachment);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with attachment", e);
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
