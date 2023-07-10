package baykov.daniel.springbootlibraryapp.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String countryBorn;
    private LocalDate birthDate;
    private boolean isAlive;
    private LocalDate deathDate;
}
