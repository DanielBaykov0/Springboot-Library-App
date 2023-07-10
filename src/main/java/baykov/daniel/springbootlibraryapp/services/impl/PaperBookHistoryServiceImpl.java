package baykov.daniel.springbootlibraryapp.services.impl;

import baykov.daniel.springbootlibraryapp.entities.PaperBook;
import baykov.daniel.springbootlibraryapp.entities.PaperBookHistory;
import baykov.daniel.springbootlibraryapp.entities.User;
import baykov.daniel.springbootlibraryapp.exceptions.LibraryHTTPException;
import baykov.daniel.springbootlibraryapp.exceptions.ResourceNotFoundException;
import baykov.daniel.springbootlibraryapp.payload.dto.PaperBookHistoryDTO;
import baykov.daniel.springbootlibraryapp.repositories.PaperBookHistoryRepository;
import baykov.daniel.springbootlibraryapp.repositories.PaperBookRepository;
import baykov.daniel.springbootlibraryapp.repositories.UserRepository;
import baykov.daniel.springbootlibraryapp.services.PaperBookHistoryService;
import baykov.daniel.springbootlibraryapp.services.PaperBookService;
import baykov.daniel.springbootlibraryapp.utils.AppConstants;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class PaperBookHistoryServiceImpl implements PaperBookHistoryService {

    private final PaperBookHistoryRepository paperBookHistoryRepository;
    private final UserRepository userRepository;
    private final PaperBookRepository paperBookRepository;
    private final PaperBookService paperBookService;
    private final ModelMapper mapper;

    public PaperBookHistoryServiceImpl(PaperBookHistoryRepository paperBookHistoryRepository, UserRepository userRepository, PaperBookRepository paperBookRepository, PaperBookService paperBookService, ModelMapper mapper) {
        this.paperBookHistoryRepository = paperBookHistoryRepository;
        this.userRepository = userRepository;
        this.paperBookRepository = paperBookRepository;
        this.paperBookService = paperBookService;
        this.mapper = mapper;
    }

    @Override
    public PaperBookHistoryDTO borrowPaperBookById(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<PaperBookHistory> paperBookHistoryList = paperBookHistoryRepository.findByUser(user);
        paperBookHistoryList.stream()
                .filter(record -> record.getReturnDateTime().isBefore(LocalDateTime.now()) && !record.isReturned())
                .findAny()
                .ifPresent(record -> {
                    throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.PENDING_RETURN_MESSAGE);
                });

        PaperBook paperBook = paperBookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (paperBook.getPaperBookNumberOfCopiesAvailable() < 1) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.NO_BOOKS_AVAILABLE_MESSAGE);
        }

        PaperBookHistory paperBookHistory = new PaperBookHistory();
        paperBookHistory.setPaperBook(paperBook);
        paperBookHistory.setUser(user);
        paperBookHistory.setBorrowDateTime(LocalDateTime.now());
        paperBookHistory.setReturnDateTime(LocalDateTime.now().plusDays(AppConstants.DEFAULT_DAYS_TO_RETURN_A_BOOK));
        paperBookHistory.setReturned(false);

        paperBookService.updateNumberOfBooksAfterBorrowing(bookId);
        return mapToDTO(paperBookHistoryRepository.save(paperBookHistory));
    }

    @Override
    public PaperBookHistoryDTO returnPaperBookByHistoryId(Long userId, Long borrowPaperBookHistoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        PaperBookHistory paperBookHistory = paperBookHistoryRepository.findById(borrowPaperBookHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow History", "id", borrowPaperBookHistoryId));

        if (!Objects.equals(paperBookHistory.getUser().getId(), user.getId()))
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.NO_VALID_BOOK_USER_MESSAGE);

        if (paperBookHistory.isReturned()) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.BOOK_ALREADY_RETURNED_MESSAGE);
        }

        paperBookHistory.setReturned(true);
        paperBookService.updateNumberOfBooksAfterReturning(paperBookHistory.getPaperBook().getId());
        return mapToDTO(paperBookHistoryRepository.save(paperBookHistory));
    }

    @Override
    public PaperBookHistoryDTO postponeReturnDateByHistoryId(Long userId, Long borrowPaperBookHistoryId, Long days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        PaperBookHistory paperBookHistory = paperBookHistoryRepository.findById(borrowPaperBookHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow History", "id", borrowPaperBookHistoryId));

        if (!Objects.equals(paperBookHistory.getUser().getId(), user.getId())) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.NO_VALID_BOOK_USER_MESSAGE);
        }

        if (paperBookHistory.isReturned()) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.BOOK_ALREADY_RETURNED_MESSAGE);
        }

        if (days < 1) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.INVALID_POSTPONE_DAYS_MESSAGE);
        }

        if (ChronoUnit.DAYS.between(paperBookHistory.getBorrowDateTime(),
                paperBookHistory.getReturnDateTime().plusDays(days)) > AppConstants.DEFAULT_MAX_POSTPONE_DAYS) {
            throw new LibraryHTTPException(HttpStatus.BAD_REQUEST, AppConstants.LIMIT_POSTPONE_DAYS_MESSAGE);
        }

        paperBookHistory.setReturnDateTime(paperBookHistory.getReturnDateTime().plusDays(days));
        PaperBookHistory updatedPaperBookHistory = paperBookHistoryRepository.save(paperBookHistory);
        return mapToDTO(updatedPaperBookHistory);
    }

    private PaperBookHistoryDTO mapToDTO(PaperBookHistory paperBookHistory) {
        return mapper.map(paperBookHistory, PaperBookHistoryDTO.class);
    }
}
