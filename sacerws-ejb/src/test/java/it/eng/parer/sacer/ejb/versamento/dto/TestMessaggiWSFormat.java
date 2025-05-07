/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.sacer.ejb.versamento.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.utils.MessaggiWSFormat;

/**
 *
 * @author Snidero_L
 */
public class TestMessaggiWSFormat {

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFormattaKeyPartAnnoMeseVers() {
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        for (String month : months) {
            Date date = Date.from(Instant.parse("2021-" + month + "-06T10:00:00Z"));
            Long actual = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(date);
            Long expected = Long.parseLong("2021" + month);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testFormattaKeyPartAnnoMeseVersWithTimeZone() {
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        for (String month : months) {
            ZonedDateTime zDate = ZonedDateTime.parse("2021-" + month + "-06T10:00:00Z",
                    DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
            Long actual = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(zDate);
            Long expected = Long.parseLong("2021" + month);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testFormattaSubPathData() {
        Date date = Date.from(Instant.parse("2014-06-21T10:15:00Z"));

        String actual = MessaggiWSFormat.formattaSubPathData(date);
        String expected = "2014_06_21";
        assertEquals(expected, actual);
    }

    @Test
    void testFormattaSubPathDataWithTimeZone() {
        ZonedDateTime zDate = ZonedDateTime.parse("2018-12-30T20:00:00Z",
                DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
        String actual = MessaggiWSFormat.formattaSubPathData(zDate);
        String expected = "2018_12_30";
        assertEquals(expected, actual);
    }

}
