package co.com.pragma.model.role;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Role implements Serializable {

    private Long id; 

    private String name;
    private String description;
}