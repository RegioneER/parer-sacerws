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
package it.eng.parer.ws.ejb;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliTpi")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliTpi {

    private static final Logger log = LoggerFactory.getLogger(ControlliTpi.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ConfigurationHelper configurationHelper;

    public RispostaControlli caricaRootPath() {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        try {
            rispostaControlli
                    .setrString(configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.TPI_ROOT_SACER));
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliTpi.caricaRootPath - AplParamApplic: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella AplParamApplic ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli verificaAbilitazioneTpi() {
        /*
         * se il TPI non è stato installato, vuol dire che tutta la gestione asincrona del versamento basata su TIVOLI è
         * inutilizabile. In questo caso lo storage dei documenti avviene su una tabella di blob dedicata chiamata
         * ARO_FILE_COMP con struttura identica a ARO_CONTENUTO_COMP
         */
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        boolean isTpiEnable = Boolean
                .parseBoolean(configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.TPI_ENABLE));
        rispostaControlli.setrBoolean(isTpiEnable);

        return rispostaControlli;
    }
}
