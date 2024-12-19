package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void setUp() {
    libraryManager.addBook("book1", 5);
    libraryManager.addBook("book2", 0);
  }

  @Test
  void testAddBook() {
    libraryManager.addBook("book1", 5);
    libraryManager.addBook("book2", 5);
    libraryManager.addBook("book3", 5);
    
    int book1Quantity = libraryManager.getAvailableCopies("book1");
    int book2Quantity = libraryManager.getAvailableCopies("book2");
    int book3Quantity = libraryManager.getAvailableCopies("book3");

    assertEquals(10, book1Quantity);
    assertEquals(5, book2Quantity);
    assertEquals(5, book3Quantity);
  }

  @Test
  void testGetAvailableCopies() {
    int book1Quantity = libraryManager.getAvailableCopies("book1");
    int bookNonExistentQuantity = libraryManager.getAvailableCopies("bookNonExistent");

    assertEquals(5, book1Quantity);
    assertEquals(0, bookNonExistentQuantity);
  }

  @Test
  void testBorrowBookInactiveUser() {
    when(userService.isUserActive(any())).thenReturn(false);
    boolean result = libraryManager.borrowBook("book1", "user1");

    assertEquals(false, result);
  }

  @Test
  void testBorrowBookZeroAvailableCopies() {
    when(userService.isUserActive(any())).thenReturn(true);
    boolean result = libraryManager.borrowBook("book3", "user1");

    assertEquals(false, result);
  }

  @Test
  void testBorrowBookSuccess() {
    when(userService.isUserActive(any())).thenReturn(true);
    int book1Quantity = libraryManager.getAvailableCopies("book1");
    boolean result = libraryManager.borrowBook("book1", "user1");

    assertEquals(true, result);
    assertEquals(book1Quantity - 1, libraryManager.getAvailableCopies("book1"));
  }

  @Test
  void testReturnBookNotContainsKey() {
    boolean result = libraryManager.returnBook("book1", "user1");

    assertEquals(false, result);
  }

  @Test
  void testReturnBookNotMatchUser() {
    when(userService.isUserActive(any())).thenReturn(true);
    libraryManager.borrowBook("book1", "user1");
    boolean result = libraryManager.returnBook("book1", "user2");

    assertEquals(false, result);
  }

  @Test
  void testReturnBookSuccess() {
    int book1Quantity = libraryManager.getAvailableCopies("book1");
    when(userService.isUserActive(any())).thenReturn(true);
    libraryManager.borrowBook("book1", "user1");
    boolean result = libraryManager.returnBook("book1", "user1");

    assertEquals(true, result);
    assertEquals(book1Quantity, libraryManager.getAvailableCopies("book1"));
  }

  @Test
  void testCalculateDynamicLateFeeNegativeOverdueDays() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, false, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "0, false, false, 0",
    "10, false, false, 5.0",
    "10, true, false, 7.5",
    "10, false, true, 4.0",
    "10, true, true, 6.0",
    "20, false, false, 10.0",
    "20, true, false, 15.0",
    "20, false, true, 8.0",
    "20, true, true, 12.0"
  })
  void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedFee) {
    double actualFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(expectedFee, actualFee);
  }
}
