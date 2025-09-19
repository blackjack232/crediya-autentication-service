package co.com.pragma.api.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String identityDocument;
    private String phone;
    private Integer idRole;
    private String password;
    private Double baseSalary;
}