package forum.hub.service.member;

import forum.hub.service.exception.SHA256HashFailException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class MemberUtil {

    public String hashPassword(String password) {
        //SHA-256 hash algorithm
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            return bytesToHex(md.digest());
        } catch (Exception e) {
            throw new SHA256HashFailException(e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
