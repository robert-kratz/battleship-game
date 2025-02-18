package protocol;

public class RegisterMessage extends Message {
    private final String username;

    public RegisterMessage(String username) {
        super(MessageType.REGISTER);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "Willkommen, " + username + "!";
    }
}