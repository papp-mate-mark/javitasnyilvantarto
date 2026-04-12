package otvosuzlet.javitasnyilntarto.Utilities;

public class StringUtils {
    public static String nullable(Object obj) {
    return obj != null ? obj.toString() : "nincs megadva";
}
}
