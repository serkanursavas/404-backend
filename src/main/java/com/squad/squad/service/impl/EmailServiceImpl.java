package com.squad.squad.service.impl;

import com.squad.squad.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailUsername, "Futza App");
            helper.setTo(toEmail);
            helper.setSubject("Şifre Sıfırlama — Futza");
            helper.setText(buildHtml(resetLink), true);
            helper.addInline("futzaLogo", new ClassPathResource("email/logo.png"));

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage(), e);
        }
    }

    private String buildHtml(String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="tr">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Şifre Sıfırlama</title>
              <style>
                @import url('https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap');
              </style>
            </head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,Helvetica,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:40px 0;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:4px;overflow:hidden;">

                      <!-- Header -->
                      <tr>
                        <td style="background-color:#1a1a2e;padding:24px 40px;text-align:center;">
                          <img src="cid:futzaLogo" width="44" height="44"
                               style="vertical-align:middle;margin-right:12px;display:inline-block;" />
                          <span style="font-size:20px;font-weight:900;letter-spacing:4px;color:#ffffff;
                                       vertical-align:middle;display:inline-block;
                                       font-family:'Press Start 2P','Courier New',monospace;">FUTZA</span>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:40px 40px 24px;">
                          <p style="margin:0 0 8px;font-size:20px;font-weight:700;color:#1a1a2e;">Şifre Sıfırlama Talebi</p>
                          <p style="margin:0 0 24px;font-size:14px;color:#555555;line-height:1.7;">
                            Hesabınız için bir şifre sıfırlama talebi aldık.<br/>
                            Aşağıdaki butona tıklayarak yeni şifrenizi belirleyebilirsiniz.
                          </p>

                          <!-- Button -->
                          <table cellpadding="0" cellspacing="0" style="margin:0 0 28px;">
                            <tr>
                              <td style="background-color:#5c6bc0;border-radius:3px;">
                                <a href="%s" target="_blank"
                                   style="display:inline-block;padding:14px 32px;font-size:14px;font-weight:700;color:#ffffff;text-decoration:none;letter-spacing:0.5px;">
                                  Şifremi Sıfırla
                                </a>
                              </td>
                            </tr>
                          </table>

                          <!-- Link fallback -->
                          <p style="margin:0 0 6px;font-size:12px;color:#888888;">Buton çalışmıyorsa aşağıdaki bağlantıyı tarayıcınıza kopyalayın:</p>
                          <p style="margin:0 0 28px;font-size:11px;word-break:break-all;">
                            <a href="%s" style="color:#5c6bc0;text-decoration:none;">%s</a>
                          </p>

                          <!-- Warning box -->
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td style="background-color:#fff8e1;border-left:3px solid #f9a825;padding:12px 16px;border-radius:2px;">
                                <p style="margin:0;font-size:12px;color:#7a6000;line-height:1.6;">
                                  ⏱ Bu bağlantı <strong>15 dakika</strong> geçerlidir.<br/>
                                  Bu talebi siz yapmadıysanız bu e-postayı dikkate almayınız — şifreniz değişmeyecektir.
                                </p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background-color:#f8f8f8;border-top:1px solid #eeeeee;padding:20px 40px;text-align:center;">
                          <p style="margin:0 0 4px;font-size:11px;color:#aaaaaa;">Bu e-posta Futza platformu tarafından otomatik olarak gönderilmiştir.</p>
                          <p style="margin:0;font-size:11px;color:#aaaaaa;">© 2026 Futza — Tüm hakları saklıdır.</p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(resetLink, resetLink, resetLink);
    }
}
