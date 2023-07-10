package baykov.daniel.springbootlibraryapp.entities;

import baykov.daniel.springbootlibraryapp.utils.AppConstants;
import com.github.ladutsko.isbn.ISBNException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ebooks")
public class EBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "default 'EBOOK'")
    private String bookType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Author author;

    @Column(nullable = false)
    private String genre;
    private String description;

    @Column(unique = true, nullable = false)
    private com.github.ladutsko.isbn.ISBN ISBN;

    {
        try {
            ISBN = com.github.ladutsko.isbn.ISBN.parseIsbn(String.valueOf(ThreadLocalRandom.current().nextLong(AppConstants.ISBN_LOWER_BOUND, AppConstants.ISBN_UPPER_BOUND)));
        } catch (ISBNException e) {
            throw new RuntimeException(e);
        }
    }

    private String readingLink;
    private String downloadLink;
}
