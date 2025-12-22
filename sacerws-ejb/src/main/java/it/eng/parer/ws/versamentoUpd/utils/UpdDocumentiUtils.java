/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;

public class UpdDocumentiUtils {

    public static String buildComponenteKeyCtrl(String categoriaDoc, String ordineComponente,
            int idx) {
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
