package co.com.pragma.model.user;

import lombok.*;


import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class User implements Serializable {

 
    private Long idUser; // corresponde a id_usuario en la BD

    private String firstName;
    private String lastName;
    private String email;
    private String identityDocument;
    private String phone;
    public  String password;
    private Long idRole; // FK a rol.UniqueID
    private BigDecimal baseSalary;
}