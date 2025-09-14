package co.com.pragma.model.user.constants;

import java.math.BigDecimal;

public class UserConstants {
    public static final String IDENTIFICATION = "identification";
    // ✅ Constantes de validación
    public static final BigDecimal MIN_SALARY = new BigDecimal("0");
    public static final BigDecimal MAX_SALARY = new BigDecimal("20000000");


    // ✅ Regex para validación de correo
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

}
