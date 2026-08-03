package game.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    private Field field;

    @BeforeEach
    void setUp() {
        field = new Field();
    }

    

    @Test
    void testConstructorCreatesField() {
        assertNotNull(field);
    }

    

    @Test
    void testGrowTreeDoesNotThrowException() {
        assertDoesNotThrow(() -> field.growTree());
    }

    @Test
    void testGrowTreeMultipleTimes() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                field.growTree();
            }
        });
    }

    

    @Test
    void testClearForestReturnsZero() {
        int result = field.clearForest();
        assertEquals(0, result);
    }

    @Test
    void testClearForestDoesNotThrowException() {
        assertDoesNotThrow(() -> field.clearForest());
    }

    @Test
    void testClearForestMultipleTimes() {
        for (int i = 0; i < 5; i++) {
            int result = field.clearForest();
            assertEquals(0, result);
        }
    }

    

    @Test
    void testFieldOperationsInSequence() {
        field.growTree();
        int cleared = field.clearForest();
        assertEquals(0, cleared);
        field.growTree();
    }

    @Test
    void testMultipleFieldsIndependent() {
        Field field1 = new Field();
        Field field2 = new Field();

        field1.growTree();
        field2.clearForest();

        
        assertNotSame(field1, field2);
    }
}
