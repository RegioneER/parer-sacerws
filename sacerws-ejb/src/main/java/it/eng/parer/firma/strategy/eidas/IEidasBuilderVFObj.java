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
package it.eng.parer.firma.strategy.eidas;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;

public interface IEidasBuilderVFObj<T extends Object> {

    /**
     * Main method per la generazione del dto trattatot nel singolo strategy (vedi {@link EidasFirmaBuilder}
     * {@link EidasMarcaBuilder}), viene restituito un oggetto di tipo {@link VFFirmaCompType} / {@link VFMarcaCompType}
     *
     * @param eidasReportsDto
     *            dto contentenete la risposta del servizio di verifica firma EIDAS
     * @param vfWrapper
     *            wrapper finale che conterrà i report EIDAS mappati opportunamente
     * @param signatureW
     *            wrapper firma EIDAS
     * @param timestampW
     *            timestamp wrapper EIDAS
     * @param dataDiRiferimento
     *            data di verifica (esplicitata su metadati o sysdate)
     * @param pgs
     *            array di progressivi busta/firma/marca
     *
     * @return restituitisce un oggetto di tipo {@link VFFirmaCompType} o {@link VFMarcaCompType}
     *
     * @throws InvocationTargetException
     *             eccezione generica
     * @throws IllegalArgumentException
     *             eccezione generica
     * @throws IllegalAccessException
     *             eccezione generica
     * @throws NoSuchMethodException
     *             eccezione generica
     *
     */
    public T build(EidasWSReportsDTOTree eidasReportsDto, VerificaFirmaWrapper vfWrapper, SignatureWrapper signatureW,
            Optional<TimestampWrapper> timestampW, ZonedDateTime dataDiRiferimento, BigDecimal[] pgs)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
