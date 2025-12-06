package main.java;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductStock – Inventory Logic Tests")
@Tag("regression")
class ProductStockTest {

    private ProductStock stock;

    @BeforeAll
    static void beforeAll() {
        System.out.println(">>> Starting ProductStock test suite");
    }

    @AfterAll
    static void afterAll() {
        System.out.println(">>> Finished ProductStock test suite");
    }

    @BeforeEach
    void setUp() {
       
        stock = new ProductStock("P-123", "WH-1-A3", 50, 10, 100);
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test completed. Current state: " + stock);
    }

   
    @Test
    @Tag("sanity")
    @DisplayName("Constructor should create valid ProductStock with correct fields")
    void constructor_validParameters_createsObject() {
        ProductStock ps = new ProductStock("P-001", "LOC-1", 10, 5, 100);

        assertAll("ProductStock initial state",
                () -> assertEquals("P-001", ps.getProductId()),
                () -> assertEquals("LOC-1", ps.getLocation()),
                () -> assertEquals(10, ps.getOnHand()),
                () -> assertEquals(0, ps.getReserved()),
                () -> assertEquals(5, ps.getReorderThreshold()),
                () -> assertEquals(100, ps.getMaxCapacity()),
                () -> assertEquals(10, ps.getAvailable())
        );
    }

    @Test
    @DisplayName("Constructor should reject null or blank productId")
    void constructor_invalidProductId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock(null, "LOC", 10, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("   ", "LOC", 10, 0, 100));
    }

    @Test
    @DisplayName("Constructor should reject null or blank location")
    void constructor_invalidLocation_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", null, 10, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", "   ", 10, 0, 100));
    }

    @Test
    @DisplayName("Constructor should validate numeric parameters")
    void constructor_invalidNumericValues_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", "LOC", -1, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", "LOC", 0, -1, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", "LOC", 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("P-1", "LOC", 200, 0, 100));
    }

  

    @Test
    @DisplayName("changeLocation should update location when valid")
    void changeLocation_valid_updatesLocation() {
        stock.changeLocation("WH-2-B4");
        assertEquals("WH-2-B4", stock.getLocation());
    }

    @Test
    @DisplayName("changeLocation should reject null or blank locations")
    void changeLocation_invalid_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.changeLocation(null));
        assertThrows(IllegalArgumentException.class,
                () -> stock.changeLocation("   "));
    }

    
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 25})
    @DisplayName("addStock should increase onHand by given positive amount (parameterized)")
    void addStock_validAmounts_increaseOnHand(int amountToAdd) {
        int before = stock.getOnHand();
        stock.addStock(amountToAdd);
        assertEquals(before + amountToAdd, stock.getOnHand());
    }

    @Test
    @DisplayName("addStock at boundary should fill up to maxCapacity exactly")
    void addStock_boundary_reachesMaxCapacity() {
        stock.addStock(50); // 50 + 50 = 100
        assertEquals(100, stock.getOnHand());
    }

    @Test
    @DisplayName("addStock should reject non-positive amounts")
    void addStock_nonPositive_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(-5));
    }

    @Test
    @DisplayName("addStock should reject when exceeding maxCapacity")
    void addStock_exceedsMaxCapacity_throwsException() {
        assertThrows(IllegalStateException.class, () -> stock.addStock(60));
    }

  
    @Test
    @DisplayName("removeDamaged should decrease onHand when amount is valid")
    void removeDamaged_valid_decreasesOnHand() {
        stock.removeDamaged(5);
        assertEquals(45, stock.getOnHand());
        assertEquals(0, stock.getReserved());
    }

    @Test
    @DisplayName("removeDamaged should adjust reserved if it becomes greater than onHand")
    void removeDamaged_adjustsReservedIfNeeded() {
        stock.reserve(40);
        stock.removeDamaged(20); // onHand 50→30, reserved 40→30
        assertEquals(30, stock.getOnHand());
        assertEquals(30, stock.getReserved());
    }

    @Test
    @DisplayName("removeDamaged should reject non-positive amount")
    void removeDamaged_nonPositive_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> stock.removeDamaged(0));
        assertThrows(IllegalArgumentException.class, () -> stock.removeDamaged(-3));
    }

    @Test
    @DisplayName("removeDamaged should reject removing more than onHand")
    void removeDamaged_moreThanOnHand_throwsException() {
        assertThrows(IllegalStateException.class, () -> stock.removeDamaged(1000));
    }

   

    @Test
    @DisplayName("reserve should increase reserved and decrease available")
    void reserve_valid_increasesReserved() {
        int initialAvailable = stock.getAvailable();
        stock.reserve(20);

        assertAll(
                () -> assertEquals(20, stock.getReserved()),
                () -> assertEquals(initialAvailable - 20, stock.getAvailable())
        );
    }

    @Test
    @DisplayName("reserve should allow reserving exactly the available amount")
    void reserve_exactAvailable_boundary() {
        stock.reserve(stock.getAvailable());
        assertEquals(stock.getOnHand(), stock.getReserved());
        assertEquals(0, stock.getAvailable());
    }

    @Test
    @DisplayName("reserve should reject non-positive amount")
    void reserve_nonPositive_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(0));
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(-1));
    }

    @Test
    @DisplayName("reserve should reject reserving more than available")
    void reserve_moreThanAvailable_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> stock.reserve(stock.getAvailable() + 1));
    }

   

    @Test
    @DisplayName("releaseReservation should decrease reserved")
    void releaseReservation_valid_decreasesReserved() {
        stock.reserve(20);
        stock.releaseReservation(5);
        assertEquals(15, stock.getReserved());
        assertEquals(50, stock.getOnHand());
    }

    @Test
    @DisplayName("releaseReservation can release all reserved")
    void releaseReservation_releaseAll_boundary() {
        stock.reserve(10);
        stock.releaseReservation(10);
        assertEquals(0, stock.getReserved());
    }

    @Test
    @DisplayName("releaseReservation should reject non-positive amount")
    void releaseReservation_nonPositive_throwsException() {
        stock.reserve(5);
        assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(0));
        assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(-2));
    }

    @Test
    @DisplayName("releaseReservation should reject releasing more than reserved")
    void releaseReservation_moreThanReserved_throwsException() {
        stock.reserve(5);
        assertThrows(IllegalStateException.class, () -> stock.releaseReservation(10));
    }

  
    @Test
    @DisplayName("shipReserved should remove units from both onHand and reserved")
    void shipReserved_valid_decreasesOnHandAndReserved() {
        stock.reserve(20);
        stock.shipReserved(10);

        assertAll(
                () -> assertEquals(40, stock.getOnHand()),
                () -> assertEquals(10, stock.getReserved()),
                () -> assertEquals(30, stock.getAvailable())
        );
    }

    @Test
    @DisplayName("shipReserved can ship all reserved amount")
    void shipReserved_shipAllReserved() {
        stock.reserve(15);
        stock.shipReserved(15);

        assertEquals(35, stock.getOnHand());
        assertEquals(0, stock.getReserved());
    }

    @Test
    @DisplayName("shipReserved should reject non-positive amount")
    void shipReserved_nonPositive_throwsException() {
        stock.reserve(10);
        assertThrows(IllegalArgumentException.class, () -> stock.shipReserved(0));
        assertThrows(IllegalArgumentException.class, () -> stock.shipReserved(-1));
    }

    @Test
    @DisplayName("shipReserved should reject shipping more than reserved")
    void shipReserved_moreThanReserved_throwsException() {
        stock.reserve(10);
        assertThrows(IllegalStateException.class, () -> stock.shipReserved(20));
    }

 

    @Test
    @DisplayName("isReorderNeeded returns true when available < threshold")
    void isReorderNeeded_true_whenBelowThreshold() {
        stock.reserve(45);
        assertTrue(stock.isReorderNeeded());
    }

    @Test
    @DisplayName("isReorderNeeded returns false when available == threshold")
    void isReorderNeeded_false_whenEqualThreshold() {
        stock.reserve(40);
        assertFalse(stock.isReorderNeeded());
    }

    @Test
    @DisplayName("isReorderNeeded returns false when available > threshold")
    void isReorderNeeded_false_whenAboveThreshold() {
        assertFalse(stock.isReorderNeeded());
    }

    // -----------------------------------------------------------------
    // updateReorderThreshold
    // -----------------------------------------------------------------

    @Test
    @DisplayName("updateReorderThreshold accepts values between 0 and maxCapacity")
    void updateReorderThreshold_validValues() {
        stock.updateReorderThreshold(0);
        assertEquals(0, stock.getReorderThreshold());

        stock.updateReorderThreshold(stock.getMaxCapacity());
        assertEquals(stock.getMaxCapacity(), stock.getReorderThreshold());

        stock.updateReorderThreshold(20);
        assertEquals(20, stock.getReorderThreshold());
    }

    @Test
    @DisplayName("updateReorderThreshold rejects negative values")
    void updateReorderThreshold_negative_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.updateReorderThreshold(-1));
    }

    @Test
    @DisplayName("updateReorderThreshold rejects values larger than maxCapacity")
    void updateReorderThreshold_aboveCapacity_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.updateReorderThreshold(stock.getMaxCapacity() + 1));
    }

  

    @Test
    @DisplayName("updateMaxCapacity should update capacity when new value is valid")
    void updateMaxCapacity_valid_updatesCapacity() {
        stock.updateMaxCapacity(200);
        assertEquals(200, stock.getMaxCapacity());
    }

    @Test
    @DisplayName("updateMaxCapacity should reject non-positive values")
    void updateMaxCapacity_nonPositive_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> stock.updateMaxCapacity(0));
        assertThrows(IllegalArgumentException.class,
                () -> stock.updateMaxCapacity(-10));
    }

    @Test
    @DisplayName("updateMaxCapacity should reject values less than current onHand")
    void updateMaxCapacity_lessThanOnHand_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> stock.updateMaxCapacity(40));
    }

    @Test
    @DisplayName("updateMaxCapacity should adjust reorderThreshold if it exceeds new capacity")
    void updateMaxCapacity_adjustsReorderThreshold() {
        stock = new ProductStock("P-777", "LOC-X", 10, 80, 100);
        stock.updateMaxCapacity(50);

        assertAll(
                () -> assertEquals(50, stock.getMaxCapacity()),
                () -> assertEquals(50, stock.getReorderThreshold())
        );
    }

   
    @Nested
    @DisplayName("When stock is freshly initialized")
    class WhenFreshStock {

        @Test
        @DisplayName("Available should equal onHand and reserved should be zero")
        void initialState_checks() {
            assertAll(
                    () -> assertEquals(50, stock.getOnHand()),
                    () -> assertEquals(0, stock.getReserved()),
                    () -> assertEquals(50, stock.getAvailable())
            );
        }
    }


    @Test
    @Timeout(1)
    @DisplayName("Simple timeout example for ProductStock operations")
    void timeout_example() {
        stock.addStock(10);     // 50 → 60
        stock.reserve(5);       // reserved 0 → 5
        stock.releaseReservation(5); // reserved 5 → 0
        stock.removeDamaged(5); // 60 → 55

       
        assertEquals(55, stock.getOnHand());
    }

    @Test
    @Disabled("Backorder feature not implemented yet")
    @DisplayName("Future feature: backorder when stock is negative")
    void futureFeature_backorder_notImplementedYet() {
       
    }
}
