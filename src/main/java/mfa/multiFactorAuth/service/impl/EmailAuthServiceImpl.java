package mfa.multiFactorAuth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.service.EmailAuthService;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Slf4j
@Service("emailAuthService")
@RequiredArgsConstructor
public class EmailAuthServiceImpl implements EmailAuthService {
    private final JavaMailSender emailSender;
    private final String pin = createPinNumber();
    @Override
    public String sendAuthEmail(String receiver) throws Exception {
        MimeMessage message = createMessage(receiver);
        try {
            emailSender.send(message);
        } catch(MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return pin;
    }

    private MimeMessage createMessage(String receiver) throws Exception {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, receiver);//보내는 대상
        message.setSubject("[인증 코드] 2차 인증 PIN 번호");//제목

        StringBuilder msgg = new StringBuilder();
        msgg.append("<div style='margin:100px;'>");
        msgg.append("<h1> 안녕하세요 Multi Auth 관리자입니다. </h1>");
        msgg.append("<br>");
        msgg.append("<p>아래 PIN을 로그인 화면에서 입력해주세요<p>");
        msgg.append("<br>");
        msgg.append("<p>감사합니다.<p>");
        msgg.append("<br>");
        msgg.append("<div align='center' style='border:1px solid black;>");
        msgg.append("<h3 style='color:blue;'>로그인 인증 PIN입니다.</h3>");
        msgg.append("<div style='font-size:130%'>");
        msgg.append("CODE : <strong>");
        msgg.append(pin+"</strong><div><br/> ");
        msgg.append("</div>");

        message.setText(msgg.toString(), "utf-8", "html");
        message.setFrom(new InternetAddress("email.properties","Multi-Auth"));

        return message;
    }
    private static String createPinNumber() {
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for(int i=0; i<8; i++) {
            int index = rnd.nextInt(3);
            switch (index) {
                case 0:
                    sb.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    sb.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    sb.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return sb.toString();
    }
}
