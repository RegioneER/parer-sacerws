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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.crypto.verifica;

/**
 *
 * @author Quaranta_M
 */
public class VerFormatiEnums {

    public enum FormatiStandardFirme {

        PDF, P7M, XML, TSR
    }

    public static final String SEPARATORE_FORMATI = ".";

    public enum EsitoControlloFormato {

        POSITIVO("Controllo OK"), NEGATIVO("Controllo fallito"), WARNING("Controllo con Warning"),
        DISABILITATO("Controllo disabilitato");

        private final String message;

        private EsitoControlloFormato() {
            this.message = null;
        }

        private EsitoControlloFormato(final String message) {
            this.message = message;
        }

        public java.lang.String message() {
            return this.message;
        }
    }

    public enum IdoneitaFormato {
        IDONEO, GESTITO, DEPRECATO
    }

    /*
     * Ereditato da "vecchio" modello (vedi CryptoEnums) Vengono riportati alcuni enum legati alla verifica dei formati.
     */

    public static final String FORMATO_SCONOSCIUTO = "???";
    public static final String PDF_MIME = "application/pdf";
    public static final String TEXT_PLAIN_MIME = "text/plain";
    public static final String XML_MIME = "application/xml";
    public static final String OCTET_STREAM_MIME = "application/octet-stream";
    public static final String CSV_MIME = "text/csv";
    public static final String DXF_MIME = "image/vnd.dxf";
}
