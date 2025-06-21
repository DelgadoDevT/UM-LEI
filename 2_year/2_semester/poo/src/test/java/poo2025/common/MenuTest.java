/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for the Menu class, specifically focusing on the applyColorAndStyle method.
 * This class contains unit tests that verify the correct application of colors and styles
 * to text strings using ANSI escape codes.
 *
 * @see Menu
 * @see Menu.ColorStyle
 */
class MenuTest {

    /**
     * Tests for applyColorAndStyle method in Menu class.
     * <p>
     * The applyColorAndStyle method takes a text string and applies
     * a combination of color and style (if provided), returning the
     * formatted string. If the text is null, it throws an IllegalArgumentException.
     */

    /**
     * Tests the applyColorAndStyle method when both color and style parameters are provided.
     * Verifies that both the color and style codes are correctly applied to the text.
     */
    @Test
    void testApplyColorAndStyle_WithBothColorAndStyle() {
        String text = "Hello, World!";
        Menu.ColorStyle color = Menu.ColorStyle.RED;
        Menu.ColorStyle style = Menu.ColorStyle.BOLD;

        String result = Menu.applyColorAndStyle(text, color, style);

        String expected = style.getCode() + color.getCode() + text + Menu.ColorStyle.RESET.getCode();
        assertEquals(expected, result);
    }

    /**
     * Tests the applyColorAndStyle method when only the color parameter is provided.
     * Verifies that the color code is correctly applied to the text when style is null.
     */
    @Test
    void testApplyColorAndStyle_WithOnlyColor() {
        String text = "Hello, World!";
        Menu.ColorStyle color = Menu.ColorStyle.GREEN;

        String result = Menu.applyColorAndStyle(text, color, null);

        String expected = color.getCode() + text + Menu.ColorStyle.RESET.getCode();
        assertEquals(expected, result);
    }

    /**
     * Tests the applyColorAndStyle method when only the style parameter is provided.
     * Verifies that the style code is correctly applied to the text when color is null.
     */
    @Test
    void testApplyColorAndStyle_WithOnlyStyle() {
        String text = "Hello, World!";
        Menu.ColorStyle style = Menu.ColorStyle.UNDERLINE;

        String result = Menu.applyColorAndStyle(text, null, style);

        String expected = style.getCode() + text + Menu.ColorStyle.RESET.getCode();
        assertEquals(expected, result);
    }

    /**
     * Tests the applyColorAndStyle method when neither color nor style parameters are provided.
     * Verifies that only the reset code is appended to the text when both parameters are null.
     */
    @Test
    void testApplyColorAndStyle_WithNoColorOrStyle() {
        String text = "Hello, World!";

        String result = Menu.applyColorAndStyle(text, null, null);

        String expected = text + Menu.ColorStyle.RESET.getCode();
        assertEquals(expected, result);
    }

    /**
     * Tests the applyColorAndStyle method with null text parameter.
     * Verifies that an IllegalArgumentException is thrown when text is null.
     */
    @Test
    void testApplyColorAndStyle_WithNullText() {
        assertThrows(IllegalArgumentException.class, () -> Menu.applyColorAndStyle(null, Menu.ColorStyle.BLUE, Menu.ColorStyle.BOLD));
    }

    /**
     * Tests the applyColorAndStyle method with an empty text string.
     * Verifies that color codes are correctly applied to an empty string.
     */
    @Test
    void testApplyColorAndStyle_WithEmptyText() {
        String text = "";
        Menu.ColorStyle color = Menu.ColorStyle.MAGENTA;

        String result = Menu.applyColorAndStyle(text, color, null);

        String expected = color.getCode() + text + Menu.ColorStyle.RESET.getCode();
        assertEquals(expected, result);
    }
}