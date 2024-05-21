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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.eng.parer.ws.utils.MessaggiWSFormat;

/**
 *
 * @author Snidero_L
 */
public class TestMessaggiWSFormat {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFormattaKeyPartAnnoMeseVers() {
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        for (String month : months) {
            Date date = Date.from(Instant.parse("2021-" + month + "-06T10:00:00Z"));
            Long actual = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(date);
            Long expected = Long.parseLong("2021" + month);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testFormattaKeyPartAnnoMeseVersWithTimeZone() {
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        for (String month : months) {
            ZonedDateTime zDate = ZonedDateTime.parse("2021-" + month + "-06T10:00:00Z",
                    DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
            Long actual = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(zDate);
            Long expected = Long.parseLong("2021" + month);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testFormattaSubPathData() {
        Date date = Date.from(Instant.parse("2014-06-21T10:15:00Z"));

        String actual = MessaggiWSFormat.formattaSubPathData(date);
        String expected = "2014_06_21";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFormattaSubPathDataWithTimeZone() {
        ZonedDateTime zDate = ZonedDateTime.parse("2018-12-30T20:00:00Z",
                DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
        String actual = MessaggiWSFormat.formattaSubPathData(zDate);
        String expected = "2018_12_30";
        Assert.assertEquals(expected, actual);
    }

}
