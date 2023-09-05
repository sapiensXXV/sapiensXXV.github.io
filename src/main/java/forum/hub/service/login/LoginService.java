package forum.hub.service.login;

public interface LoginService {

    /**
     * @param password 해시화 시킬 패스워드
     * @return 해시 패스워드
     */
    public String hashPassword(String password);

}
