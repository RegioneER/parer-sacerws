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

package it.eng.parer.ws.versamento.ejb;

import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.DateUtilsConverter.format;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.OrgPartition;
import it.eng.parer.entity.OrgPartitionStrut;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExt;

/**
 *
 * @author Fioravanti_F
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "ControlliPartizioni")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliPartizioni {

    private static final Logger log = LoggerFactory.getLogger(ControlliPartizioni.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public RispostaControlli verificaPartizioniBlob(StrutturaVersamento strutV, CSVersatore versatore) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        String queryStr;
        javax.persistence.Query query;

        long idStrutt = strutV.getIdStruttura();
        String descStrutt = versatore.getStruttura();
        CostantiDB.TipoSalvataggioFile tipoSalvataggioFile = strutV.getTipoSalvataggioFile();
        //
        Long periodoDiPartizionamento;
        periodoDiPartizionamento = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(strutV.getDataVersamento());

        try {
            /*
             * verifica della partizione per la tabella dei blob: AROCONTENUTOCOMP -> partizionata per anno + mese
             * versamento, AROFILECOMP -> partizionata per anno + mese versamento, ma se il tpi è abilitato e il
             * versamento è su FILE, il controllo non si fa
             */
            if (tipoSalvataggioFile == CostantiDB.TipoSalvataggioFile.FILE && strutV.isTpiAbilitato()) {
                rispostaControlli.setrBoolean(true);
                return rispostaControlli;
            }

            queryStr = "select count(t) from OrgPartitionStrut t " + "where t.orgStrut.idStrut = :idStrutIn "
                    + "and t.tiPartition = :tiPartitionIn ";
            query = entityManager.createQuery(queryStr);
            query.setParameter("idStrutIn", idStrutt);
            query.setParameter("tiPartitionIn",
                    CostantiDB.TipoPartizione.valueOf(tipoSalvataggioFile.toString()).toString());
            if ((Long) query.getSingleResult() == 0) {
                // La struttura {0} non ha partizioni di database definite per i componenti di tipo {1}
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_002_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_002_001, descStrutt,
                        tipoSalvataggioFile.toString()));
                return rispostaControlli;
            }
            //
            queryStr = "select count(t) from OrgValSubPartition t " + "join t.orgPartition.orgPartitionStruts tps "
                    + "where tps.orgStrut.idStrut = :idStrutIn " + "and tps.tiPartition = :tiPartitionIn "
                    + "and t.cdValSubPartition = :cdValSubPartitionIn";
            query = entityManager.createQuery(queryStr);
            query.setParameter("idStrutIn", idStrutt);
            query.setParameter("tiPartitionIn",
                    CostantiDB.TipoPartizione.valueOf(tipoSalvataggioFile.toString()).toString());
            query.setParameter("cdValSubPartitionIn", periodoDiPartizionamento.toString());
            if ((Long) query.getSingleResult() == 0) {
                // La struttura {0} non ha partizioni di database definite per i componenti di tipo {1} per l''anno {2}
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_002_002);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_002_002, descStrutt,
                        tipoSalvataggioFile.toString(), periodoDiPartizionamento.toString()));
                return rispostaControlli;
            }

            rispostaControlli.setrBoolean(true);
        } catch (Exception ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Errore nella verifica delle partizioni per la strutttura  " + ex.getMessage()));
            log.error("Errore nella verifica delle partizioni per la strutttura ", ex);
        }

        return rispostaControlli;
    }

    public RispostaControlli verificaPartizioniSubStrutt(VersamentoExt versamento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        String queryStr;
        javax.persistence.Query query;

        StrutturaVersamento strutV = versamento.getStrutturaComponenti();

        String descStrutt = versamento.getVersamento().getIntestazione().getVersatore().getStruttura();

        long idSubStrutt = strutV.getIdSubStruttura();
        String descSubStrutt = strutV.getDescSubStruttura();

        Long annoChiave = Long.valueOf(versamento.getVersamento().getIntestazione().getChiave().getAnno());
        //
        try {
            /*
             * verifica della partizione per la tabella AROUNITADOC, partizionata per anno della chiave
             */
            queryStr = "select count(t) from OrgPartitionSubStrut t " + "where t.orgSubStrut.idSubStrut = :idSubStrut "
                    + "and t.tiPartition = :tiPartitionIn ";
            query = entityManager.createQuery(queryStr);
            query.setParameter("idSubStrut", idSubStrutt);
            query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.UNI_DOC_SUB_STRUT.toString());
            if ((Long) query.getSingleResult() == 0) {
                // La substruttura {0} della struttura {1} non ammette versamenti
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_005_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_005_001, descSubStrutt, descStrutt));
                return rispostaControlli;
            }
            //
            queryStr = "select count(t) from OrgValSubPartition t " + "join t.orgPartition.orgPartitionSubStruts tps "
                    + "where tps.orgSubStrut.idSubStrut = :idSubStrut " + "and tps.tiPartition = :tiPartitionIn "
                    + "and t.cdValSubPartition = :cdValSubPartitionIn";
            query = entityManager.createQuery(queryStr);
            query.setParameter("idSubStrut", idSubStrutt);
            query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.UNI_DOC_SUB_STRUT.toString());
            query.setParameter("cdValSubPartitionIn", annoChiave.toString());
            if ((Long) query.getSingleResult() == 0) {
                // La substruttura {0} della struttura {1} non ammette versamenti per l''anno {2}
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_005_002);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_005_002, descSubStrutt,
                        descStrutt, annoChiave.toString()));
                return rispostaControlli;
            }

            rispostaControlli.setrBoolean(true);
        } catch (Exception ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Errore nella verifica delle partizioni per la strutttura  " + ex.getMessage()));
            log.error("Errore nella verifica delle partizioni per la strutttura ", ex);
        }
        return rispostaControlli;
    }

    public RispostaControlli verificaPartizioniVers(StrutturaVersamento strutV, SyncFakeSessn sessione,
            String descStrutt) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        String queryStr;
        javax.persistence.Query query;

        try {
            long idPart = 0;
            if (strutV != null && strutV.getIdStruttura() != 0) {
                queryStr = "select t from OrgPartitionStrut t " + "where t.orgStrut.idStrut = :idStrutIn "
                        + "and t.tiPartition = :tiPartitionIn ";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", strutV.getIdStruttura());
                query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.SES.toString());
                List<OrgPartitionStrut> vsv = query.getResultList();
                if (!vsv.isEmpty()) {
                    idPart = vsv.get(0).getOrgPartition().getIdPartition();
                }
            } else {
                queryStr = "select t from OrgPartition t " + "where t.tiPartition = :tiPartitionIn "
                        + "and not exists (" + "select ti from OrgPartitionStrut ti "
                        + "where ti.tiPartition = :tiPartitionIn " + "and ti.orgPartition.idPartition = t.idPartition)";
                query = entityManager.createQuery(queryStr);
                query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.SES.toString());
                List<OrgPartition> vsv = query.getResultList();
                if (!vsv.isEmpty()) {
                    idPart = vsv.get(0).getIdPartition();
                }
            }
            if (idPart == 0) {
                /*
                 * Non sono definite partizioni db per salvare la sessione di versamento per la struttura {0}
                 */
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_003_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_003_001, descStrutt));
                return rispostaControlli;
            } else {
                queryStr = "select count(t) from OrgVValSubPartition t " + "where t.idPartition = :idPartitionIn "
                        + "and t.cdValSubPartition > :cdValSubPartitionIn";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idPartitionIn", new BigDecimal(idPart));
                query.setParameter("cdValSubPartitionIn", convert(sessione.getTmApertura()));
                if ((Long) query.getSingleResult() == 0) {
                    /*
                     * Non sono definite partizioni db per salvare la sessione di versamento per la struttura {0} per la
                     * data {1}
                     */
                    rispostaControlli.setCodErr(MessaggiWSBundle.PART_003_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_003_002, descStrutt,
                            format(sessione.getTmApertura())));
                    return rispostaControlli;
                }
            }
            //
            idPart = 0;
            if (strutV != null && strutV.getIdStruttura() != 0) {
                queryStr = "select t from OrgPartitionStrut t " + "where t.orgStrut.idStrut = :idStrutIn "
                        + "and t.tiPartition = :tiPartitionIn ";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", strutV.getIdStruttura());
                query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.FILE_SES.toString());
                List<OrgPartitionStrut> vsv = query.getResultList();
                if (!vsv.isEmpty()) {
                    idPart = vsv.get(0).getOrgPartition().getIdPartition();
                }
            } else {
                queryStr = "select t from OrgPartition t " + "where t.tiPartition = :tiPartitionIn "
                        + "and not exists (" + "select ti from OrgPartitionStrut ti "
                        + "where ti.tiPartition = :tiPartitionIn " + "and ti.orgPartition.idPartition = t.idPartition)";
                query = entityManager.createQuery(queryStr);
                query.setParameter("tiPartitionIn", CostantiDB.TipoPartizione.FILE_SES.toString());
                List<OrgPartition> vsv = query.getResultList();
                if (!vsv.isEmpty()) {
                    idPart = vsv.get(0).getIdPartition();
                }
            }
            if (idPart == 0) {
                /*
                 * Non sono definite partizioni db per salvare i file della sessione di versamento per la struttura {0}
                 */
                rispostaControlli.setCodErr(MessaggiWSBundle.PART_004_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_004_001, descStrutt));
                return rispostaControlli;
            } else {
                Long periodoDiPartizionamento;
                periodoDiPartizionamento = MessaggiWSFormat.formattaKeyPartAnnoMeseVers(sessione.getTmApertura());
                queryStr = "select count(t) from OrgValSubPartition t "
                        + "where t.orgPartition.idPartition = :idPartitionIn "
                        + "and t.cdValSubPartition = :cdValSubPartitionIn";
                query = entityManager.createQuery(queryStr);
                query.setParameter("idPartitionIn", idPart);
                query.setParameter("cdValSubPartitionIn", periodoDiPartizionamento.toString());
                if ((Long) query.getSingleResult() == 0) {
                    /*
                     * Non sono definite partizioni db per salvare i file della sessione di versamento per la struttura
                     * {0} per la data {1}
                     */
                    rispostaControlli.setCodErr(MessaggiWSBundle.PART_004_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PART_004_002, descStrutt,
                            format(sessione.getTmApertura())));
                    return rispostaControlli;
                }
            }

            rispostaControlli.setrBoolean(true);
        } catch (Exception ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Errore nella verifica delle partizioni per la sessione di versamento  " + ex.getMessage()));
            log.error("Errore nella verifica delle partizioni per la sessione di versamento ", ex);
        }

        return rispostaControlli;
    }
}
