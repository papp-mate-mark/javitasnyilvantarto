package otvosuzlet.javitasnyilntarto.exceptions;

public class OrderImpossibleException extends RuntimeException {
    private String source;
    public OrderImpossibleException(String message)
    {
        super(message);
        this.source = "undefinied source";
    }
    public OrderImpossibleException(String message, String source)
    {
        super(message);
        this.source = source;
    }
    public String getSource() {
        return source;
    }
    
}
