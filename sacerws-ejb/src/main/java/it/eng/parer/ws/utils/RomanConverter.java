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

package it.eng.parer.ws.utils;

/**
 *
 * @author Fioravanti_F
 */
public class RomanConverter {

    /*
     * Francesco Fioravanti 2011
     */

    private int num; // l'istanza del numero
    private boolean strict = false; // se attivato, la conversione genera un'eccezione per numeri superiori a 3999
    // 2 array con i numeri romani ed i corrispondenti indici
    private static int[] numbers = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
    private static String[] letters = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

    public RomanConverter() {
        num = 0;
    }

    public RomanConverter(int arabicNum) {
        this.setInt(arabicNum);
    }

    public RomanConverter(String roman) {
        this.setRoman(roman);
    }

    //
    @Override
    public String toString() {
        String roman = ""; // il numero romano
        // N rappresenta quella parte del numero che deve ancora essere convertita
        int N = num;
        for (int i = 0; i < numbers.length; i++) {
            while (N >= numbers[i]) {
                roman += letters[i];
                N -= numbers[i];
            }
        }
        return roman;
    }

    public int toInt() {
        return num;
    }

    //
    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    //
    public void fromIntValue(int arabicNum) {
        this.setInt(arabicNum);
    }

    public void fromRomanValue(String roman) {
        this.setRoman(roman);
    }

    //
    private void setInt(int arabicNum) {
        if (arabicNum < 1) {
            throw new NumberFormatException("Il numero deve essere positivo.");
        }
        if (this.strict && arabicNum > 3999) {
            throw new NumberFormatException("Il numero deve essere inferiore a 4000.");
        }

        num = arabicNum;
    }

    private void setRoman(String roman) {
        if (roman.length() == 0) {
            throw new NumberFormatException("Una stringa vuota non definisce un numero romano.");
        }

        roman = roman.toUpperCase();
        int i = 0;
        int arabic = 0;

        while (i < roman.length()) {
            char letter = roman.charAt(i);
            int number = RomanToArabic(letter);
            if (number < 0) {
                throw new NumberFormatException("Carattere illegale: \"" + letter
                        + "\" in un numero romano; è impossibile determinarne il valore.");
            }
            i++;
            if (i == roman.length()) {
                arabic += number;
            } else {
                // se la lettera successiva ha un valore maggiore di quella appena letta, allora vengono lette insieme
                // questo permette di interpretare IX, IV, XC, ecc
                int nextNumber = RomanToArabic(roman.charAt(i));

                if (nextNumber > number) {
                    // combina le 2 lettere ed avanza di una poszione
                    arabic += (nextNumber - number);
                    i++;
                } else {
                    arabic += number;
                }
            }
        }
        if (this.strict && arabic > 3999) {
            throw new NumberFormatException("Il numero deve essere inferiore a 4000.");
        }

        num = arabic;
    }

    private int RomanToArabic(char letter) {
        switch (letter) {
        case 'I':
            return 1;
        case 'V':
            return 5;
        case 'X':
            return 10;
        case 'L':
            return 50;
        case 'C':
            return 100;
        case 'D':
            return 500;
        case 'M':
            return 1000;
        default:
            return -1;
        }
    }
}
