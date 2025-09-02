package co.com.pragma.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

@Table(name = "users")// si dejas user, debe ir con comillas en SQL -> mejor renombrar a app_user
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column("id_user")
    private Long idUser;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;


    private String email;

    @Column("identity_document")
    private String identityDocument;


    private String phone;

    @Column("id_role")
    private Long idRole;

    @Column("base_salary")
    private BigDecimal baseSalary;
}
