package co.com.pragma.api.dto.response;

import lombok.Data;
@Data
public class UserResponse {

        private String firstName;
        private String lastName;
        private String email;
        private String identityDocument;
        private String phone;
        private Double baseSalary;
    }