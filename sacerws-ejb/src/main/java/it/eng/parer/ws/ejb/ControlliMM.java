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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
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
@Stateless(mappedName = "ControlliMM")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliMM {

    private static final Logger log = LoggerFactory.getLogger(ControlliMM.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ConfigurationHelper configurationHelper;

    public enum TipiRootPath {

        In, Out
    }

    public RispostaControlli caricaRootPath(String appVersante, TipiRootPath tipoRootPath) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        try {
            String paramName = StringUtils.EMPTY;

            switch (tipoRootPath) {
            case In:
                paramName = ParametroApplDB.PATH_MM_IN + appVersante;
                break;
            case Out:
                paramName = ParametroApplDB.PATH_MM_OUT + appVersante;
                break;
            default:
                break;
            }
            rispostaControlli.setrString(configurationHelper.getValoreParamApplicByApplic(paramName));
            rispostaControlli.setrBoolean(true);
        } catch (NoResultException e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli
                    .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666, "ControlliMM.caricaRootPath - "
                            + "Applicativo chiamante non correttamente configurato nella tabella AplParamApplic"));
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliMM.caricaRootPath - AplParamApplic: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella AplParamApplic", e);
        }

        return rispostaControlli;
    }

}
