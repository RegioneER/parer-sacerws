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

package it.eng.parer.firma.strategy.eidas.factory;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.strategy.eidas.EidasMarcaBuilder;
import it.eng.parer.firma.strategy.eidas.IEidasBuilderVFObj;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.utils.Costanti;

public abstract class EidasAbsBuilderFactory<V extends Object> {

    /**
     *
     * Factory creazione e build del dto di tipo "verifica firma", che ospiterà firma o marca.
     *
     * @param controlliAbilitati
     *            controlli abilitati recuperti a sistema
     * @param isDataDiRiferimentoOnCompVers
     *            se data riferimento per verifica esplicita
     * @param dataDiRiferimento
     *            data di verifica (esplicitata su metadati o sysdate)
     * @param modificatoriWSCalc
     *            modificatori ws (dipendono dalla versione)
     * @param eidasReportsDto
     *            dto contentenete la risposta del servizio di verifica firma EIDAS
     * @param vfWrapper
     *            wrapper finale che conterrà i report EIDAS mappati opportunamente
     * @param signatureW
     *            wrapper firma EIDAS
     * @param timestampW
     *            timestamp wrapper EIDAS
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
     */
    public V create(Map<String, Boolean> controlliAbilitati, boolean isDataDiRiferimentoOnCompVers,
            ZonedDateTime dataDiRiferimento, Set<Costanti.ModificatoriWS> modificatoriWSCalc,
            EidasWSReportsDTOTree eidasReportsDto, VerificaFirmaWrapper vfWrapper, SignatureWrapper signatureW,
            Optional<TimestampWrapper> timestampW, BigDecimal[] pgs)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        IEidasBuilderVFObj<V> buider = init(controlliAbilitati, isDataDiRiferimentoOnCompVers, modificatoriWSCalc);

        return buider.build(eidasReportsDto, vfWrapper, signatureW, timestampW, dataDiRiferimento, pgs);
    }

    /**
     * Creazione strategy ({@link EidasVFFirmaCompBuilder} e {@link EidasMarcaBuilder})
     *
     * @param controlliAbilitati
     *            controlli abilitati recuperti a sistema
     * @param isDataDiRiferimentoOnCompVers
     *            data di verifica (esplicitata su metadati o sysdate)
     * @param modificatoriWSCalc
     *            modificatori ws (dipendono dalla versione)
     *
     * @return implementazione stratey effettivo
     */
    protected abstract IEidasBuilderVFObj<V> init(Map<String, Boolean> controlliAbilitati,
            boolean isDataDiRiferimentoOnCompVers, Set<Costanti.ModificatoriWS> modificatoriWSCalc);
}
