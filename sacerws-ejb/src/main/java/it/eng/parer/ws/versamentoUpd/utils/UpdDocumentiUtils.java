/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;

public class UpdDocumentiUtils {

    public static String buildComponenteKeyCtrl(String categoriaDoc, String ordineComponente, int idx) {
        return TipiEntitaSacer.COMP + "_" + categoriaDoc + "_" + ordineComponente + "@" + idx;
    }

    public static Enum<?> convertEnumTiUsoXsd(boolean jpaEnum, String tiUsoModelloXsd) {
        if (jpaEnum) {
            switch (CostantiDB.TiUsoModelloXsd.valueOf(tiUsoModelloXsd)) {
            case MIGRAZ:
                return TiUsoXsdAroUpdDatiSpecUnitaDoc.MIGRAZ;
            default:
                return TiUsoXsdAroUpdDatiSpecUnitaDoc.VERS;
            }
        } else {
            switch (TiUsoXsdAroUpdDatiSpecUnitaDoc.valueOf(tiUsoModelloXsd)) {
            case MIGRAZ:
                return CostantiDB.TiUsoModelloXsd.MIGRAZ;
            default:
                return CostantiDB.TiUsoModelloXsd.VERS;
            }
        }
    }

    public static Enum<?> convertEnumTiEntita(boolean jpaEnum, String tipiEntitaSacer) {
        if (jpaEnum) {
            switch (CostantiDB.TipiEntitaSacer.valueOf(tipiEntitaSacer)) {
            case COMP:
                return TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP;
            case SUB_COMP:
                return TiEntitaAroUpdDatiSpecUnitaDoc.UPD_COMP;
            case DOC:
                return TiEntitaAroUpdDatiSpecUnitaDoc.UPD_DOC;
            default:
                return TiEntitaAroUpdDatiSpecUnitaDoc.UPD_UNI_DOC;
            }
        } else {
            switch (TiEntitaAroUpdDatiSpecUnitaDoc.valueOf(tipiEntitaSacer)) {
            case UPD_COMP:
                return CostantiDB.TipiEntitaSacer.COMP;
            case UPD_DOC:
                return CostantiDB.TipiEntitaSacer.DOC;
            default:
                return CostantiDB.TipiEntitaSacer.UNI_DOC;
            }
        }
    }

    public static Enum<?> convertEnumTipiUsoDatiSpec(boolean jpaEnum, String tipiUsoDatiSpec) {
        if (jpaEnum) {
            switch (CostantiDB.TipiUsoDatiSpec.valueOf(tipiUsoDatiSpec)) {
            case MIGRAZ:
                return TipiUsoDatiSpec.MIGRAZ;
            default:
                return TipiUsoDatiSpec.VERS;
            }
        } else {
            switch (TiUsoXsdAroUpdDatiSpecUnitaDoc.valueOf(tipiUsoDatiSpec)) {
            case MIGRAZ:
                return CostantiDB.TipiUsoDatiSpec.MIGRAZ;
            default:
                return CostantiDB.TipiUsoDatiSpec.VERS;
            }
        }
    }

}