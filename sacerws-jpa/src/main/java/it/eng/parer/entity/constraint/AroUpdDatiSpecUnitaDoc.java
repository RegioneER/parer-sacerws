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

package it.eng.parer.entity.constraint;

/**
 * ARO_UPD_DATI_SPEC_UNITA_DOC's constraint
 *
 * @author sinatti_s
 */
public final class AroUpdDatiSpecUnitaDoc {

    private AroUpdDatiSpecUnitaDoc() {
    }

    /**
     * Uso vers. XSD
     * 
     * ti_uso_xsd IN ('MIGRAZ', 'VERS')
     */
    public enum TiUsoXsdAroUpdDatiSpecUnitaDoc {
        MIGRAZ, VERS
    }

    /**
     * Tipo entita SACER
     * 
     * ti_entita_sacer IN ('UPD_COMP', 'UPD_DOC', 'UPD_UNI_DOC')
     */
    public enum TiEntitaAroUpdDatiSpecUnitaDoc {
        UPD_COMP, UPD_DOC, UPD_UNI_DOC
    }
}
