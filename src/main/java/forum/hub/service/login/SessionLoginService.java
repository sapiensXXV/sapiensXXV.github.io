package forum.hub.service.login;

import forum.hub.service.exception.SHA256HashFailException;

import java.security.MessageDigest;

public class SessionLoginService implements LoginService{

    @Override
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
