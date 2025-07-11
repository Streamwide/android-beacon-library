/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 4 Nov 2024 14:24:17 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 4 Nov 2024 14:23:41 +0100
 */

package org.altbeacon

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Pattern


class RegExTest {

    //I Pattern
    private val I_PATTERN = Pattern.compile("i\\:(\\d+)\\-(\\d+)([blv]*)")

    @Test
    fun `test pattern with blv`() {
        val matcher = I_PATTERN.matcher("i:123-456b")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("b", matcher.group(3))
    }

    @Test
    fun `test pattern without blv`() {
        val matcher = I_PATTERN.matcher("i:123-456")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("", matcher.group(3)) // Ensure group(3) is empty when no [blv] is present
    }

    @Test
    fun `test pattern with multiple blv`() {
        val matcher = I_PATTERN.matcher("i:123-456blv")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("blv", matcher.group(3))
    }

    @Test
    fun `test pattern invalid format`() {
        val matcher = I_PATTERN.matcher("i:123-abc")
        assertFalse(matcher.matches()) // Ensure invalid format does not match
    }

    // D Pattern

    private val D_PATTERN = Pattern.compile("d\\:(\\d+)\\-(\\d+)([bl]*)")

    @Test
    fun `test pattern with b or l`() {
        val matcher = D_PATTERN.matcher("d:123-456b")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("b", matcher.group(3))
    }

    @Test
    fun `test pattern without b or l`() {
        val matcher = D_PATTERN.matcher("d:123-456")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("", matcher.group(3)) // Ensure group(3) is empty when no [bl] is present
    }

    @Test
    fun `test pattern with multiple bl`() {
        val matcher = D_PATTERN.matcher("d:123-456bl")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals("bl", matcher.group(3))
    }

    @Test
    fun `test pattern with multiple b and l`() {
        val matcher = D_PATTERN.matcher("d:123-456bllb")
        assertTrue(matcher.matches())
        assertEquals("123", matcher.group(1))
        assertEquals("456", matcher.group(2))
        assertEquals(
            "bllb",
            matcher.group(3)
        ) // Check correct capture of mixed "b" and "l" characters
    }

    @Test
    fun `test pattern invalid D format`() {
        val matcher = D_PATTERN.matcher("d:123-abc")
        assertFalse(matcher.matches()) // Ensure invalid format does not match
    }


    //Decimal Pattern
    private val DECIMAL_PATTERN = Pattern.compile("^0|[1-9][0-9]*$")

    @Test
    fun `test single zero`() {
        val matcher = DECIMAL_PATTERN.matcher("0")
        assertTrue(matcher.matches()) // Single "0" should match
    }

    @Test
    fun `test positive number without leading zeros`() {
        val matcher = DECIMAL_PATTERN.matcher("123")
        assertTrue(matcher.matches()) // Positive integer should match
    }

    @Test
    fun `test positive number with leading zero`() {
        val matcher = DECIMAL_PATTERN.matcher("0123")
        assertFalse(matcher.matches()) // Leading zero in a non-zero number should not match
    }

    @Test
    fun `test large positive number`() {
        val matcher = DECIMAL_PATTERN.matcher("9876543210")
        assertTrue(matcher.matches()) // Large positive integer should match
    }

    @Test
    fun `test empty string`() {
        val matcher = DECIMAL_PATTERN.matcher("")
        assertFalse(matcher.matches()) // Empty string should not match
    }

    @Test
    fun `test negative number`() {
        val matcher = DECIMAL_PATTERN.matcher("-123")
        assertFalse(matcher.matches()) // Negative numbers should not match
    }

    @Test
    fun `test decimal number`() {
        val matcher = DECIMAL_PATTERN.matcher("123.45")
        assertFalse(matcher.matches()) // Decimal numbers should not match
    }

    @Test
    fun `test non-numeric characters`() {
        val matcher = DECIMAL_PATTERN.matcher("abc")
        assertFalse(matcher.matches()) // Non-numeric characters should not match
    }

    private val EDDYSTONE_URL_REGEX =
        "^((?i)http|https)://((?i)www\\.)?((?:[0-9a-zA-Z_-]+\\.?)+)(/?)([./0-9a-zA-Z_-]*)"
    private val pattern = Pattern.compile(EDDYSTONE_URL_REGEX)


    @Test
    fun `test valid http URL without www`() {
        val matcher = pattern.matcher("http://example.com")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test valid https URL without www`() {
        val matcher = pattern.matcher("https://example.com")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test valid http URL with www`() {
        val matcher = pattern.matcher("http://www.example.com")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test valid https URL with www`() {
        val matcher = pattern.matcher("https://www.example.com")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test URL with subdomain`() {
        val matcher = pattern.matcher("http://subdomain.example.com")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test URL with path`() {
        val matcher = pattern.matcher("https://example.com/path/to/resource")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test URL with trailing slash`() {
        val matcher = pattern.matcher("https://example.com/")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test URL with valid characters in path`() {
        val matcher = pattern.matcher("https://example.com/path/to/resource.html")
        assertTrue(matcher.matches())
    }

    @Test
    fun `test invalid URL missing scheme`() {
        val matcher = pattern.matcher("www.example.com")
        assertFalse(matcher.matches())
    }

    @Test
    fun `test invalid URL with unsupported scheme`() {
        val matcher = pattern.matcher("ftp://example.com")
        assertFalse(matcher.matches())
    }

    @Test
    fun `test invalid URL with space`() {
        val matcher = pattern.matcher("https://example .com")
        assertFalse(matcher.matches())
    }

    @Test
    fun `test empty string url`() {
        val matcher = pattern.matcher("")
        assertFalse(matcher.matches())
    }

}